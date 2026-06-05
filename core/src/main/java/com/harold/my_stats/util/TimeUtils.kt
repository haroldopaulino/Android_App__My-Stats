package com.harold.my_stats.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

object TimeUtils {
    private val endpointFormat = SimpleDateFormat("MM/dd/yyyy hh:mm:ss a", Locale.US)
    private val isoFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX", Locale.US)
    init {
        endpointFormat.timeZone = TimeZone.getDefault()
        isoFormat.timeZone = TimeZone.getDefault()
    }
    fun nowEndpointString(): String = endpointFormat.format(Date())
    fun formatIso(epochMs: Long): String = isoFormat.format(Date(epochMs))
}
