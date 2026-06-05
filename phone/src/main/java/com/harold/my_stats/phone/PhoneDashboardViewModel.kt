package com.harold.my_stats.phone

import android.app.Application
import android.os.Build
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.harold.my_stats.collector.DeviceSnapshotCollector
import com.harold.my_stats.db.AppSettingsEntity
import com.harold.my_stats.db.MyStatsDatabase
import com.harold.my_stats.service.CollectorService
import com.harold.my_stats.util.CompressedJsonStrings
import com.harold.my_stats.util.Prefs
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

internal class PhoneDashboardViewModel(application: Application) : AndroidViewModel(application) {
    private val prefs = Prefs(application)
    private val _uiState = MutableStateFlow(
        PhoneUiState(
            isRunning = prefs.isCollectorRunning(),
            phoneName = listOf(Build.MANUFACTURER, Build.MODEL)
                .filter { it.isNotBlank() }
                .joinToString(" ")
                .ifBlank { "Android Phone" }
        )
    )
    val uiState: StateFlow<PhoneUiState> = _uiState.asStateFlow()

    init {
        loadSettings()
        if (_uiState.value.isRunning) refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            val next = withContext(Dispatchers.IO) {
                val result = DeviceSnapshotCollector(getApplication<Application>(), prefs).collect(System.currentTimeMillis())
                val reportDao = MyStatsDatabase.get().reportDao()
                val latest = reportDao.latest()
                val pendingPayloads = reportDao.pendingPayloads()
                DashboardState(
                    snapshot = result.snapshotJson,
                    pendingCount = reportDao.pendingCount(),
                    latestPayload = latest?.payloadJson?.let { CompressedJsonStrings.decompress(it) } ?: "No saved report yet.",
                    refreshedAt = SimpleDateFormat("hh:mm:ss a", Locale.US).format(Date()),
                    collectedCount = prefs.getCollectedCount(),
                    sentEndpointCount = prefs.getSentEndpointCount(),
                    failedEndpointCount = prefs.getFailedEndpointCount(),
                    collectedDataSizeBytes = pendingPayloads.sumOf { pendingJsonSizeBytes(it) }
                )
            }
            _uiState.update { it.copy(dashboard = next) }
        }
    }

    fun toggleCollector() {
        val context = getApplication<Application>()
        if (_uiState.value.isRunning) {
            CollectorService.stop(context)
            _uiState.update { it.copy(isRunning = false, dashboard = DashboardState()) }
        } else {
            CollectorService.start(context)
            _uiState.update { it.copy(isRunning = true) }
            refresh()
        }
    }

    fun toggleTheme() {
        val next = !_uiState.value.isDarkTheme
        _uiState.update { it.copy(isDarkTheme = next) }
        viewModelScope.launch(Dispatchers.IO) { MyStatsDatabase.get().appSettingsDao().updateTheme(next) }
    }

    fun updateEndpoint(next: String) {
        _uiState.update { it.copy(endpointUrl = next) }
        viewModelScope.launch(Dispatchers.IO) { MyStatsDatabase.get().appSettingsDao().updateEndpoint(next.trim()) }
    }

    fun updateUploadInterval(next: Long) {
        _uiState.update { it.copy(uploadIntervalMs = next) }
        viewModelScope.launch(Dispatchers.IO) {
            MyStatsDatabase.get().appSettingsDao().updateUploadInterval(next)
            if (prefs.isCollectorRunning()) {
                CollectorService.flushNow(getApplication())
            }
        }
    }

    fun openScreen(screen: String) { _uiState.update { it.copy(screen = screen) } }
    fun selectMainTab(tab: Int) { _uiState.update { it.copy(mainTab = tab) } }
    fun selectDetailTab(tab: Int) { _uiState.update { it.copy(detailTab = tab) } }

    fun onBack() {
        _uiState.update { state ->
            state.copy(screen = if (state.screen == "privacy" || state.screen == "about") "settings" else "home")
        }
    }

    private fun pendingJsonSizeBytes(storedJson: String): Long = runCatching {
        CompressedJsonStrings.decompress(storedJson).toByteArray(Charsets.UTF_8).size.toLong()
    }.getOrElse { storedJson.toByteArray(Charsets.UTF_8).size.toLong() }

    private fun loadSettings() {
        viewModelScope.launch {
            val settings = withContext(Dispatchers.IO) {
                val dao = MyStatsDatabase.get().appSettingsDao()
                dao.get() ?: AppSettingsEntity().also { dao.save(it) }
            }
            _uiState.update {
                it.copy(
                    isDarkTheme = settings.darkTheme,
                    endpointUrl = settings.endpointUrl,
                    uploadIntervalMs = settings.uploadIntervalMs
                )
            }
        }
    }
}
