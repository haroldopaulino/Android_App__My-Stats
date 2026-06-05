package com.harold.my_stats.util

import android.content.Context

class Prefs(context: Context) {
    private val sp = context.getSharedPreferences("my_stats_prefs", Context.MODE_PRIVATE)
    fun setLastGracefulShutdownEpochMs(value: Long) = sp.edit().putLong("last_graceful_shutdown_epoch_ms", value).apply()
    fun getLastGracefulShutdownEpochMs(): Long = sp.getLong("last_graceful_shutdown_epoch_ms", 0L)
    fun setLastHeartbeatEpochMs(value: Long) = sp.edit().putLong("last_heartbeat_epoch_ms", value).apply()
    fun setCollectorRunning(value: Boolean) = sp.edit().putBoolean("collector_running", value).apply()
    fun isCollectorRunning(): Boolean = sp.getBoolean("collector_running", false)
    fun setLastBatteryLevel(value: Int) = sp.edit().putInt("last_battery_level", value).apply()
    fun getLastBatteryLevel(): Int? = if (sp.contains("last_battery_level")) sp.getInt("last_battery_level", 0) else null
    fun setLastBatterySampleEpochMs(value: Long) = sp.edit().putLong("last_battery_sample_epoch_ms", value).apply()
    fun getLastBatterySampleEpochMs(): Long = sp.getLong("last_battery_sample_epoch_ms", 0L)
    fun incrementCollectedCount() = sp.edit().putLong("collected_count", getCollectedCount() + 1L).apply()
    fun getCollectedCount(): Long = sp.getLong("collected_count", 0L)
    fun incrementSentEndpointCount() = sp.edit().putLong("sent_endpoint_count", getSentEndpointCount() + 1L).apply()
    fun getSentEndpointCount(): Long = sp.getLong("sent_endpoint_count", 0L)
    fun incrementFailedEndpointCount() = sp.edit().putLong("failed_endpoint_count", getFailedEndpointCount() + 1L).apply()
    fun getFailedEndpointCount(): Long = sp.getLong("failed_endpoint_count", 0L)
}
