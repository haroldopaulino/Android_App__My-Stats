package com.harold.my_stats.phone

internal data class UploadIntervalOption(
    val label: String,
    val millis: Long
)

internal val uploadIntervalOptions = listOf(
    UploadIntervalOption("1 minute", 1 * 60 * 1000L),
    UploadIntervalOption("5 minutes", 5 * 60 * 1000L),
    UploadIntervalOption("10 minutes", 10 * 60 * 1000L),
    UploadIntervalOption("15 minutes", 15 * 60 * 1000L),
    UploadIntervalOption("30 minutes", 30 * 60 * 1000L),
    UploadIntervalOption("45 minutes", 45 * 60 * 1000L),
    UploadIntervalOption("1 hour", 60 * 60 * 1000L),
    UploadIntervalOption("2 hours", 2 * 60 * 60 * 1000L),
    UploadIntervalOption("3 hours", 3 * 60 * 60 * 1000L),
    UploadIntervalOption("5 hours", 5 * 60 * 60 * 1000L),
    UploadIntervalOption("12 hours", 12 * 60 * 60 * 1000L),
    UploadIntervalOption("1 day", 24 * 60 * 60 * 1000L),
    UploadIntervalOption("2 days", 2 * 24 * 60 * 60 * 1000L),
    UploadIntervalOption("3 days", 3 * 24 * 60 * 60 * 1000L),
    UploadIntervalOption("5 days", 5 * 24 * 60 * 60 * 1000L),
    UploadIntervalOption("15 days", 15 * 24 * 60 * 60 * 1000L),
    UploadIntervalOption("1 month", 30 * 24 * 60 * 60 * 1000L)
)

internal fun uploadIntervalLabel(millis: Long): String = uploadIntervalOptions
    .firstOrNull { it.millis == millis }
    ?.label
    ?: "10 minutes"

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
