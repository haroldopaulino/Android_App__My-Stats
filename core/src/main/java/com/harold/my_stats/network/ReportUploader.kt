package com.harold.my_stats.network

import com.harold.my_stats.model.DebugReportPayload
import com.harold.my_stats.util.JsonUtils
import java.io.BufferedWriter
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import kotlinx.serialization.encodeToString

object ReportUploader {
    fun upload(payload: DebugReportPayload, endpointUrl: String) {
        val json = JsonUtils.json.encodeToString(payload)
        val conn = (URL(endpointUrl).openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            connectTimeout = 15000
            readTimeout = 15000
            doInput = true
            doOutput = true
            setRequestProperty("Content-Type", "application/json; charset=utf-8")
            setRequestProperty("Accept", "application/json")
        }
        try {
            BufferedWriter(OutputStreamWriter(conn.outputStream, Charsets.UTF_8)).use { it.write(json) }
            val code = conn.responseCode
            if (code !in 200..299) {
                val errorBody = conn.errorStream?.bufferedReader()?.use { it.readText() }
                throw IllegalStateException("HTTP $code ${errorBody ?: ""}".trim())
            }
        } finally {
            conn.disconnect()
        }
    }
}
