package com.harold.my_stats.phone

import androidx.compose.ui.graphics.Color
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.longOrNull

internal fun isPermissionDenied(text: String): Boolean = text.contains("permission denied", ignoreCase = true)

internal fun cpuDeniedCommand(snapshot: JsonObject?): String? {
    val cpu = snapshot.obj("cpu")
    return when {
        isPermissionDenied(cpu?.string("loadAvg").orEmpty()) -> "cat /proc/loadavg"
        isPermissionDenied(cpu?.string("procStatSummary").orEmpty()) -> "cat /proc/stat"
        isPermissionDenied(cpu?.string("uptime").orEmpty()) -> "cat /proc/uptime"
        else -> null
    }
}

internal fun cpuDeniedOutput(snapshot: JsonObject?): String? {
    val cpu = snapshot.obj("cpu")
    return when {
        isPermissionDenied(cpu?.string("loadAvg").orEmpty()) -> cpu?.string("loadAvg")
        isPermissionDenied(cpu?.string("procStatSummary").orEmpty()) -> cpu?.string("procStatSummary")
        isPermissionDenied(cpu?.string("uptime").orEmpty()) -> cpu?.string("uptime")
        else -> null
    }
}
internal fun permissionSensitiveValue(raw: String?, normal: String): String = if (raw != null && isPermissionDenied(raw)) raw else normal


internal fun metricsFor(snap: JsonObject?): List<MetricSpec> = listOf(
    MetricSpec(
        "CPU",
        if (cpuDeniedCommand(snap) != null) "info" else cpuLabel(snap),
        cpuSubtitle(snap),
        "Load average",
        "▣",
        Color(0xFF2F8DFF),
        permissionDeniedCommand = cpuDeniedCommand(snap),
        permissionDeniedOutput = cpuDeniedOutput(snap)
    ),
    MetricSpec("Memory", memoryLabel(snap), bytesToMb(snap.obj("cpu")?.long("jvmTotalMemoryBytes")) + " total", "Runtime heap", "▥", Color(0xFF7E46E8)),
    MetricSpec("Battery", batteryLabel(snap), batteryStatusLabel(snap), tempLabel(snap), "▯", Color(0xFF67E08E)),
    MetricSpec("Storage", storageUsedLabel(snap), bytesReadable(snap.obj("storage")?.long("freeBytes")), "Public Storage", "▤", Color(0xFFFFC107)),
    MetricSpec("Wi‑Fi", boolLabel(snap.obj("connectivity")?.bool("wifiEnabled")), "Transport ${boolLabel(snap.obj("connectivity")?.bool("activeTransportWifi"))}", "Validated ${boolLabel(snap.obj("connectivity")?.bool("validatedCapability"))}", "≋", Color(0xFF00B8A9)),
    MetricSpec("Bluetooth", boolLabel(snap.obj("connectivity")?.bool("bluetoothEnabled")), "Transport ${boolLabel(snap.obj("connectivity")?.bool("activeTransportBluetooth"))}", "State ${snap.obj("connectivity")?.int("bluetoothState") ?: "--"}", "ᛒ", Color(0xFF1E88E5)),
    MetricSpec("Cellular", boolLabel(snap.obj("connectivity")?.bool("activeTransportCellular")), snap.obj("telephony")?.string("networkOperatorName") ?: "Operator unavailable", "Data ${snap.obj("telephony")?.int("dataState") ?: "--"}", "▥", Color(0xFFFF8A3D)),
    MetricSpec("NFC", boolLabel(snap.obj("connectivity")?.bool("nfcEnabled")), "Available ${boolLabel(snap.obj("connectivity")?.bool("nfcAvailable"))}", "Adapter status", "N", Color(0xFFFFD166)),
    MetricSpec("Screen", screenLabel(snap), "Brightness ${brightnessLabel(snap)}", "Timeout ${durationLabel(snap.obj("screen")?.int("screenOffTimeoutMs")?.toLong())}", "◐", Color(0xFFB36BFF)),
    MetricSpec("Boot", durationLabel(snap.obj("build")?.long("uptimeMs")), "Estimated ${epochLabel(snap.obj("build")?.long("estimatedBootEpochMs"))}", "Usage permission ${boolLabel(snap.obj("usage")?.bool("permissionGranted"))}", "⏱", Color(0xFFFFA726))
)

internal fun JsonObject?.obj(key: String): JsonObject? = this?.get(key)?.jsonObject
internal fun JsonObject.string(key: String): String? = this[key]?.jsonPrimitive?.content
internal fun JsonObject.int(key: String): Int? = this[key]?.jsonPrimitive?.intOrNull
internal fun JsonObject.long(key: String): Long? = this[key]?.jsonPrimitive?.longOrNull
internal fun JsonObject.double(key: String): Double? = this[key]?.jsonPrimitive?.doubleOrNull
internal fun JsonObject.bool(key: String): Boolean? = this[key]?.jsonPrimitive?.booleanOrNull
internal fun batteryLabel(snapshot: JsonObject?): String = snapshot.obj("battery")?.int("levelPct")?.let { "$it%" } ?: "--"
internal fun tempLabel(snapshot: JsonObject?): String = snapshot.obj("battery")?.double("temperatureC")?.let { String.format(java.util.Locale.US, "%.1f°C", it) } ?: "--"
internal fun dischargeLabel(snapshot: JsonObject?): String = snapshot.obj("battery")?.double("dischargeRatePctPerHour")?.let { String.format(java.util.Locale.US, "%.2f%%/hr", it) } ?: "--"
internal fun brightnessLabel(snapshot: JsonObject?): String = snapshot.obj("screen")?.int("screenBrightness")?.toString() ?: "--"
internal fun cpuLabel(snapshot: JsonObject?): String = snapshot.obj("cpu")?.string("loadAvg")?.split(" ")?.take(3)?.joinToString(" / ") ?: "--"
internal fun cpuSubtitle(snapshot: JsonObject?): String = "${snapshot.obj("cpu")?.int("processorCount") ?: "--"} processors"
internal fun memoryLabel(snapshot: JsonObject?): String { val total = snapshot.obj("cpu")?.long("jvmTotalMemoryBytes") ?: return "--"; val free = snapshot.obj("cpu")?.long("jvmFreeMemoryBytes") ?: 0L; return "${(total - free) / (1024 * 1024)} MB" }
internal fun processLabel(snapshot: JsonObject?): String = snapshot.obj("cpu")?.int("runningProcessCountFromProc")?.let { "$it" } ?: "--"
internal fun screenLabel(snapshot: JsonObject?): String = if (snapshot.obj("screen")?.bool("interactive") == true) "Awake" else "Idle"
internal fun batteryStatusLabel(snapshot: JsonObject?): String = when (snapshot.obj("battery")?.int("status")) { 2 -> "Charging"; 3 -> "Discharging"; 4 -> "Not charging"; 5 -> "Full"; else -> "Battery sensor" }
internal fun boolLabel(value: Boolean?): String = when (value) { true -> "Yes"; false -> "No"; null -> "--" }
internal fun boolColor(value: Boolean?): Color = when (value) { true -> Color(0xFF9ADBC9); false -> Color(0xFFFFD166); null -> Color(0xFFB7C8DA) }
internal fun epochLabel(value: Long?): String { if (value == null || value <= 0L) return "--"; return java.text.SimpleDateFormat("MM/dd hh:mm a", java.util.Locale.US).format(java.util.Date(value)) }
internal fun bytesToMb(value: Long?): String = value?.let { "${it / (1024 * 1024)} MB" } ?: "--"
internal fun bytesReadable(value: Long?): String {
    if (value == null || value < 0L) return "--"
    val gb = value / (1024.0 * 1024.0 * 1024.0)
    val mb = value / (1024.0 * 1024.0)
    return if (gb >= 1.0) String.format(java.util.Locale.US, "%.1f GB", gb) else String.format(java.util.Locale.US, "%.0f MB", mb)
}
internal fun storageUsedLabel(snapshot: JsonObject?): String {
    val used = snapshot.obj("storage")?.double("usedPercent") ?: return "--"
    return String.format(java.util.Locale.US, "%.0f%% used", used)
}
internal fun durationLabel(value: Long?): String { if (value == null || value < 0L) return "--"; val totalSeconds = value / 1000; val hours = totalSeconds / 3600; val minutes = (totalSeconds % 3600) / 60; val seconds = totalSeconds % 60; return when { hours > 0 -> "${hours}h ${minutes}m"; minutes > 0 -> "${minutes}m ${seconds}s"; else -> "${seconds}s" } }
internal fun pluggedLabel(value: Int?): String = when (value) { 0 -> "Unplugged"; 1 -> "AC"; 2 -> "USB"; 4 -> "Wireless"; else -> "--" }
internal fun healthLabel(value: Int?): String = when (value) { 1 -> "Unknown"; 2 -> "Good"; 3 -> "Overheat"; 4 -> "Dead"; 5 -> "Over voltage"; 6 -> "Failure"; 7 -> "Cold"; else -> "--" }
