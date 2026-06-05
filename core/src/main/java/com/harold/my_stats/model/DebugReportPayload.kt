package com.harold.my_stats.model

import kotlinx.serialization.Serializable

@Serializable
data class DebugReportPayload(
    val app_install_id: Int,
    val device_id: Int,
    val battery: Int,
    val imei: String,
    val category: String,
    val description: String,
    val severity: String,
    val stacktrace: String,
    val extra_json: String,
    val git_branch: String,
    val debug_notes: String,
    val created_at: String
)
