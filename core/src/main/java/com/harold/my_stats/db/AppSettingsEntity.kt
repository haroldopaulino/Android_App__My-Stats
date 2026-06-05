package com.harold.my_stats.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "app_settings")
data class AppSettingsEntity(
    @PrimaryKey val id: Int = 1,
    val endpointUrl: String = DEFAULT_ENDPOINT_URL,
    val darkTheme: Boolean = true,
    val uploadIntervalMs: Long = DEFAULT_UPLOAD_INTERVAL_MS,
    val updatedAtEpochMs: Long = System.currentTimeMillis()
) {
    companion object {
        const val DEFAULT_ENDPOINT_URL = "https://sparqm.com/web/gabb/debug/debug_report_post.php"
        const val DEFAULT_UPLOAD_INTERVAL_MS = 10 * 60 * 1000L
    }
}
