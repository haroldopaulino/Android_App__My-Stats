package com.harold.my_stats.wearable

import androidx.compose.ui.graphics.Color

internal data class WearMetric(val title: String, val value: String, val hint: String, val icon: String, val color: Color)
internal data class WearState(
    val battery: String = "--",
    val subtitle: String = "Loading...",
    val collectedCount: Long = 0,
    val collectedDataSizeBytes: Long = 0,
    val sentEndpointCount: Long = 0,
    val failedEndpointCount: Long = 0,
    val rows: List<WearMetric> = emptyList()
)
internal data class WearUiState(val state: WearState = WearState(subtitle = "Stopped"), val isRunning: Boolean = false)
