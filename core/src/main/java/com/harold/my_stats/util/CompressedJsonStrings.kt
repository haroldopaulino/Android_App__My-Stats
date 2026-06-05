package com.harold.my_stats.util

import android.util.Base64
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream

object CompressedJsonStrings {
    private const val PREFIX = "gzip-base64:"

    fun compress(json: String): String {
        val output = ByteArrayOutputStream()
        GZIPOutputStream(output).use { gzip ->
            gzip.write(json.toByteArray(Charsets.UTF_8))
        }
        return PREFIX + Base64.encodeToString(output.toByteArray(), Base64.NO_WRAP)
    }

    fun decompress(value: String): String {
        if (!value.startsWith(PREFIX)) return value
        val bytes = Base64.decode(value.removePrefix(PREFIX), Base64.NO_WRAP)
        return GZIPInputStream(ByteArrayInputStream(bytes)).use { gzip ->
            gzip.readBytes().toString(Charsets.UTF_8)
        }
    }
}
