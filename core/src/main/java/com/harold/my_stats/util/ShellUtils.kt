package com.harold.my_stats.util

import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.concurrent.TimeUnit

object ShellUtils {
    fun run(command: String, timeoutSeconds: Long = 10): String = try {
        val process = ProcessBuilder("sh", "-c", command).redirectErrorStream(true).start()
        process.waitFor(timeoutSeconds, TimeUnit.SECONDS)
        BufferedReader(InputStreamReader(process.inputStream)).use { it.readText() }.trim()
    } catch (t: Throwable) {
        "ERROR: ${t.javaClass.simpleName}: ${t.message}"
    }

    fun safeTrim(text: String, maxChars: Int = 12000): String =
        if (text.length <= maxChars) text else text.take(maxChars) + "\n...[trimmed]"
}
