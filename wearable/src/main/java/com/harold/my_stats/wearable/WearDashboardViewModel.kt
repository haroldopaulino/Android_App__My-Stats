package com.harold.my_stats.wearable

import android.app.Application
import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.harold.my_stats.collector.DeviceSnapshotCollector
import com.harold.my_stats.db.MyStatsDatabase
import com.harold.my_stats.model.DebugReportPayload
import com.harold.my_stats.network.ReportUploader
import com.harold.my_stats.service.CollectorService
import com.harold.my_stats.util.CompressedJsonStrings
import com.harold.my_stats.util.Prefs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.decodeFromString

internal class WearDashboardViewModel(application: Application) : AndroidViewModel(application) {
    private val prefs = Prefs(application)
    private val wearSchedulerPrefs = application.getSharedPreferences("my_stats_wear_upload_scheduler", Context.MODE_PRIVATE)
    private val _uiState = MutableStateFlow(WearUiState(isRunning = prefs.isCollectorRunning()))
    val uiState: StateFlow<WearUiState> = _uiState.asStateFlow()

    init { if (_uiState.value.isRunning) refresh() }

    fun refresh() {
        viewModelScope.launch {
            val next = withContext(Dispatchers.IO) {
                val snap = DeviceSnapshotCollector(getApplication<Application>(), prefs).collect(System.currentTimeMillis()).snapshotJson
                val database = MyStatsDatabase.get()
                val reportDao = database.reportDao()
                val localPayloads = reportDao.localPayloads()
                val localCount = reportDao.localCount()
                val pendingStoredPayloadSizeBytes = pendingLocalDataSizeBytes(localPayloads, localCount)
                val settings = database.appSettingsDao().get()
                val sentCount = settings?.totalSentEndpointCount ?: prefs.getSentEndpointCount()
                val failedCount = settings?.totalFailedEndpointCount ?: prefs.getFailedEndpointCount()
                val cpuValue = cpuDisplayValue(snap)
                WearState(
                    battery = batteryLabel(snap),
                    subtitle = "Collected ${prefs.getCollectedCount()} • Sent $sentCount",
                    collectedCount = prefs.getCollectedCount(),
                    collectedDataSizeBytes = pendingStoredPayloadSizeBytes,
                    sentEndpointCount = sentCount,
                    failedEndpointCount = failedCount,
                    rows = listOf(
                        WearMetric("CPU", cpuValue.value, if (cpuValue.description == null) cpuSubtitle(snap) else "Tap for details", "▣", Color(0xFF2F8DFF), cpuValue.description),
                        WearMetric("Memory", memoryLabel(snap), "Runtime heap", "▥", Color(0xFF7E46E8)),
                        WearMetric("Battery", batteryLabel(snap), "${batteryStatusLabel(snap)} • ${tempLabel(snap)}", "▯", Color(0xFF67E08E)),
                        WearMetric("Drain", dischargeLabel(snap), "Battery per hour", "↘", Color(0xFF67E08E)),
                        WearMetric("Wi‑Fi", boolLabel(snap.obj("connectivity")?.bool("wifiEnabled")), "Validated ${boolLabel(snap.obj("connectivity")?.bool("validatedCapability"))}", "≋", Color(0xFF00B8A9)),
                        WearMetric("Bluetooth", boolLabel(snap.obj("connectivity")?.bool("bluetoothEnabled")), "State ${snap.obj("connectivity")?.int("bluetoothState") ?: "--"}", "ᛒ", Color(0xFF1E88E5)),
                        WearMetric("Cellular", boolLabel(snap.obj("connectivity")?.bool("activeTransportCellular")), "Telephony ${if (snap.obj("telephony")?.bool("available") == true) "available" else "unavailable"}", "▥", Color(0xFFFF8A3D)),
                        WearMetric("NFC", boolLabel(snap.obj("connectivity")?.bool("nfcEnabled")), "Available ${boolLabel(snap.obj("connectivity")?.bool("nfcAvailable"))}", "N", Color(0xFFFFD166)),
                        WearMetric("Screen", screenLabel(snap), "Brightness ${brightnessLabel(snap)}", "◐", Color(0xFFB36BFF)),
                        WearMetric("Power", boolLabel(snap.obj("power")?.bool("powerSaveMode")), "Power save", "⚡", Color(0xFFFFD166)),
                        WearMetric("Boot", durationLabel(snap.obj("build")?.long("uptimeMs")), "Estimated ${epochLabel(snap.obj("build")?.long("estimatedBootEpochMs"))}", "⏱", Color(0xFFFFA726)),
                        WearMetric("Processes", processLabel(snap), "Public /proc count", "≡", Color(0xFF78909C))
                    )
                )
            }
            _uiState.update { it.copy(state = next) }
        }
    }

    private fun pendingLocalDataSizeBytes(localPayloads: List<String>, localCount: Int): Long {
        val compressedBytes = localPayloads.sumOf { it.toByteArray(Charsets.UTF_8).size.toLong() }
        val decompressedBytes = localPayloads.sumOf { storedJson ->
            runCatching {
                CompressedJsonStrings.decompress(storedJson).toByteArray(Charsets.UTF_8).size.toLong()
            }.getOrElse { storedJson.toByteArray(Charsets.UTF_8).size.toLong() }
        }
        return maxOf(compressedBytes, decompressedBytes, if (localCount > 0) localCount.toLong() else 0L)
    }

    fun uploadPendingIfDue() {
        if (!_uiState.value.isRunning) return
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val database = MyStatsDatabase.get()
                val settingsDao = database.appSettingsDao()
                val settings = settingsDao.get() ?: com.harold.my_stats.db.AppSettingsEntity().also { settingsDao.save(it) }
                val intervalMs = settings.uploadIntervalMs.coerceAtLeast(10_000L)
                val now = System.currentTimeMillis()
                val lastAttempt = wearSchedulerPrefs.getLong(KEY_LAST_WEAR_UPLOAD_ATTEMPT_MS, 0L)
                if (lastAttempt > 0L && now - lastAttempt < intervalMs) return@withContext
                if (database.reportDao().pendingCount() <= 0) {
                    wearSchedulerPrefs.edit().putLong(KEY_LAST_WEAR_UPLOAD_ATTEMPT_MS, now).apply()
                    return@withContext
                }
                wearSchedulerPrefs.edit().putLong(KEY_LAST_WEAR_UPLOAD_ATTEMPT_MS, now).apply()
                uploadPendingFromWearable(database, settings.endpointUrl)
            }
            refresh()
        }
    }

    private suspend fun uploadPendingFromWearable(database: MyStatsDatabase, endpointUrl: String) {
        val reportDao = database.reportDao()
        val settingsDao = database.appSettingsDao()
        reportDao.getPending(MAX_WEAR_UPLOAD_BATCH).forEach { entity ->
            runCatching {
                val json = CompressedJsonStrings.decompress(entity.payloadJson)
                val payload = com.harold.my_stats.util.JsonUtils.json.decodeFromString<DebugReportPayload>(json)
                ReportUploader.upload(payload, endpointUrl)
                reportDao.deleteById(entity.localId)
                prefs.incrementSentEndpointCount()
                settingsDao.incrementSentEndpointCount()
            }.onFailure {
                reportDao.markFailed(entity.localId, it.message)
                prefs.incrementFailedEndpointCount()
                settingsDao.incrementFailedEndpointCount()
            }
        }
    }

    fun toggleCollector() {
        val context = getApplication<Application>()
        if (_uiState.value.isRunning) {
            CollectorService.stop(context)
            _uiState.update { it.copy(isRunning = false, state = WearState(subtitle = "Stopped")) }
        } else {
            wearSchedulerPrefs.edit().putLong(KEY_LAST_WEAR_UPLOAD_ATTEMPT_MS, System.currentTimeMillis()).apply()
            CollectorService.start(context)
            _uiState.update { it.copy(isRunning = true) }
            refresh()
        }
    }

    companion object {
        private const val KEY_LAST_WEAR_UPLOAD_ATTEMPT_MS = "last_wear_upload_attempt_ms"
        private const val MAX_WEAR_UPLOAD_BATCH = 8_640
    }
}
