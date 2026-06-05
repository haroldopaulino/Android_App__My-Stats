package com.harold.my_stats.wearable

import android.app.Application
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.harold.my_stats.collector.DeviceSnapshotCollector
import com.harold.my_stats.db.MyStatsDatabase
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

internal class WearDashboardViewModel(application: Application) : AndroidViewModel(application) {
    private val prefs = Prefs(application)
    private val _uiState = MutableStateFlow(WearUiState(isRunning = prefs.isCollectorRunning()))
    val uiState: StateFlow<WearUiState> = _uiState.asStateFlow()

    init { if (_uiState.value.isRunning) refresh() }

    fun refresh() {
        viewModelScope.launch {
            val next = withContext(Dispatchers.IO) {
                val snap = DeviceSnapshotCollector(getApplication<Application>(), prefs).collect(System.currentTimeMillis()).snapshotJson
                val pendingPayloads = MyStatsDatabase.get().reportDao().pendingPayloads()
                WearState(
                    battery = batteryLabel(snap),
                    subtitle = "Collected ${prefs.getCollectedCount()} • Sent ${prefs.getSentEndpointCount()}",
                    collectedCount = prefs.getCollectedCount(),
                    collectedDataSizeBytes = pendingPayloads.sumOf { pendingJsonSizeBytes(it) },
                    sentEndpointCount = prefs.getSentEndpointCount(),
                    failedEndpointCount = prefs.getFailedEndpointCount(),
                    rows = listOf(
                        WearMetric("CPU", cpuLabel(snap), cpuSubtitle(snap), "▣", Color(0xFF2F8DFF)),
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

    private fun pendingJsonSizeBytes(storedJson: String): Long = runCatching {
        CompressedJsonStrings.decompress(storedJson).toByteArray(Charsets.UTF_8).size.toLong()
    }.getOrElse { storedJson.toByteArray(Charsets.UTF_8).size.toLong() }

    fun toggleCollector() {
        val context = getApplication<Application>()
        if (_uiState.value.isRunning) {
            CollectorService.stop(context)
            _uiState.update { it.copy(isRunning = false, state = WearState(subtitle = "Stopped")) }
        } else {
            CollectorService.start(context)
            _uiState.update { it.copy(isRunning = true) }
            refresh()
        }
    }
}
