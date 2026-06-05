package com.harold.my_stats.phone

import androidx.compose.ui.graphics.Color
import kotlinx.serialization.json.JsonObject

internal data class DashboardState(
    val snapshot: JsonObject? = null,
    val pendingCount: Int = 0,
    val latestPayload: String = "Waiting for collector...",
    val refreshedAt: String = "--",
    val collectedCount: Long = 0,
    val sentEndpointCount: Long = 0,
    val failedEndpointCount: Long = 0,
    val collectedDataSizeBytes: Long = 0
)

internal data class MetricSpec(
    val title: String,
    val value: String,
    val line1: String,
    val line2: String,
    val iconText: String,
    val accent: Color,
    val permissionDeniedCommand: String? = null,
    val permissionDeniedOutput: String? = null
)

internal data class PhoneUiState(
    val dashboard: DashboardState = DashboardState(),
    val mainTab: Int = 0,
    val detailTab: Int = 0,
    val isRunning: Boolean = false,
    val isDarkTheme: Boolean = true,
    val endpointUrl: String = com.harold.my_stats.db.AppSettingsEntity.DEFAULT_ENDPOINT_URL,
    val uploadIntervalMs: Long = com.harold.my_stats.db.AppSettingsEntity.DEFAULT_UPLOAD_INTERVAL_MS,
    val screen: String = "home",
    val phoneName: String = "Android Phone"
)
