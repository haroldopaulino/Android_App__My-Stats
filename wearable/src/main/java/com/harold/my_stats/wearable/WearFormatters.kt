package com.harold.my_stats.wearable

import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.longOrNull

internal fun JsonObject?.obj(key: String): JsonObject? = this?.get(key)?.jsonObject
internal fun JsonObject.string(key: String): String? = this[key]?.jsonPrimitive?.content
internal fun JsonObject.int(key: String): Int? = this[key]?.jsonPrimitive?.intOrNull
internal fun JsonObject.long(key: String): Long? = this[key]?.jsonPrimitive?.longOrNull
internal fun JsonObject.double(key: String): Double? = this[key]?.jsonPrimitive?.doubleOrNull
internal fun JsonObject.bool(key: String): Boolean? = this[key]?.jsonPrimitive?.booleanOrNull
internal fun batteryLabel(snapshot: JsonObject?): String = snapshot.obj("battery")?.int("levelPct")?.let { "$it%" } ?: "--"
internal fun tempLabel(snapshot: JsonObject?): String = snapshot.obj("battery")?.double("temperatureC")?.let { String.format(java.util.Locale.US, "%.1f°", it) } ?: "--"
internal fun dischargeLabel(snapshot: JsonObject?): String = snapshot.obj("battery")?.double("dischargeRatePctPerHour")?.let { String.format(java.util.Locale.US, "%.1f%%", it) } ?: "--"
internal fun cpuLabel(snapshot: JsonObject?): String = snapshot.obj("cpu")?.string("loadAvg")?.split(" ")?.firstOrNull() ?: "--"
internal fun cpuSubtitle(snapshot: JsonObject?): String = "${snapshot.obj("cpu")?.int("processorCount") ?: "--"} processors"
internal fun memoryLabel(snapshot: JsonObject?): String { val total = snapshot.obj("cpu")?.long("jvmTotalMemoryBytes") ?: return "--"; val free = snapshot.obj("cpu")?.long("jvmFreeMemoryBytes") ?: 0L; return "${(total - free) / (1024 * 1024)} MB" }
internal fun processLabel(snapshot: JsonObject?): String = snapshot.obj("cpu")?.int("runningProcessCountFromProc")?.let { "$it" } ?: "--"
internal fun brightnessLabel(snapshot: JsonObject?): String = snapshot.obj("screen")?.int("screenBrightness")?.toString() ?: "--"
internal fun screenLabel(snapshot: JsonObject?): String = if (snapshot.obj("screen")?.bool("interactive") == true) "Awake" else "Idle"
internal fun batteryStatusLabel(snapshot: JsonObject?): String = when (snapshot.obj("battery")?.int("status")) { 2 -> "Charging"; 3 -> "Discharging"; 4 -> "Not charging"; 5 -> "Full"; else -> "Battery" }
internal fun boolLabel(value: Boolean?): String = when (value) { true -> "On"; false -> "Off"; null -> "--" }
internal fun epochLabel(value: Long?): String { if (value == null || value <= 0L) return "--"; return java.text.SimpleDateFormat("MM/dd", java.util.Locale.US).format(java.util.Date(value)) }
internal fun durationLabel(value: Long?): String { if (value == null || value < 0L) return "--"; val totalSeconds = value / 1000; val hours = totalSeconds / 3600; val minutes = (totalSeconds % 3600) / 60; return if (hours > 0) "${hours}h ${minutes}m" else "${minutes}m" }
internal fun formatByteSize(bytes: Long): String {
    if (bytes < 1024L) return "$bytes bytes"
    val units = listOf("KB", "MB", "GB", "TB")
    var value = bytes.toDouble() / 1024.0
    var index = 0
    while (value >= 1024.0 && index < units.lastIndex) {
        value /= 1024.0
        index++
    }
    return if (value >= 10.0) "%.1f %s".format(value, units[index]) else "%.2f %s".format(value, units[index])
}
