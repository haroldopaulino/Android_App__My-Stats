package com.harold.my_stats.collector

import android.Manifest
import android.app.ActivityManager
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.wifi.WifiManager
import android.nfc.NfcAdapter
import android.content.pm.PackageManager
import android.os.BatteryManager
import android.os.Build
import android.os.Environment
import android.os.PowerManager
import android.os.StatFs
import android.os.SystemClock
import android.provider.Settings
import android.telephony.TelephonyManager
import android.text.format.DateUtils
import com.harold.my_stats.util.Prefs
import com.harold.my_stats.util.ShellUtils
import com.harold.my_stats.util.TimeUtils
import kotlin.math.max
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class DeviceSnapshotCollector(private val context: Context, private val prefs: Prefs) {
    private fun hasPermission(permission: String): Boolean =
        context.checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED

    private fun hasFeature(feature: String): Boolean =
        context.packageManager.hasSystemFeature(feature)

    private fun safeJsonObject(section: String, block: () -> JsonObject): JsonObject =
        runCatching(block).getOrElse { throwable ->
            buildJsonObject {
                put("available", false)
                put("errorSection", section)
                put("errorType", throwable::class.java.simpleName)
                put("errorMessage", throwable.message ?: "")
            }
        }

    fun collect(nowMs: Long): SnapshotResult {
        val batteryObject = safeJsonObject("battery") { collectBattery(nowMs) }
        val crashes = runCatching { collectCrashes(nowMs) }.getOrElse { safeJsonObject("crashes") { throw it } to "" }
        val snapshot = buildJsonObject {
            put("collectedAtEpochMs", nowMs)
            put("collectedAtDisplay", TimeUtils.formatIso(nowMs))
            put("build", collectBuild())
            put("battery", batteryObject)
            put("screen", safeJsonObject("screen") { collectScreen() })
            put("storage", safeJsonObject("storage") { collectStorage() })
            put("cpu", safeJsonObject("cpu") { collectCpu() })
            put("processes", safeJsonObject("processes") { collectProcesses() })
            put("crashes", crashes.first)
            put("connectivity", safeJsonObject("connectivity") { collectConnectivity() })
            put("usage", safeJsonObject("usage") { collectUsage(nowMs) })
            put("telephony", safeJsonObject("telephony") { collectTelephony() })
            put("power", safeJsonObject("power") { collectPower() })
            put("topDetails", safeJsonObject("topDetails") { collectTopDetails() })
            put("shell", safeJsonObject("shell") { collectShellBestEffort() })
            put("notes", buildJsonArray {
                add(JsonPrimitive("Global key/touch timestamps are unavailable in the non-privileged build."))
                add(JsonPrimitive("Per-radio power requires privileged or shell access and is unavailable in this build."))
            })
        }
        return SnapshotResult(snapshot, crashes.second)
    }

    private fun collectBuild(): JsonObject = buildJsonObject {
        put("manufacturer", Build.MANUFACTURER)
        put("brand", Build.BRAND)
        put("device", Build.DEVICE)
        put("model", Build.MODEL)
        put("product", Build.PRODUCT)
        put("hardware", Build.HARDWARE)
        put("fingerprint", Build.FINGERPRINT)
        put("sdkInt", Build.VERSION.SDK_INT)
        put("release", Build.VERSION.RELEASE ?: "")
        put("incremental", Build.VERSION.INCREMENTAL ?: "")
        put("bootElapsedRealtimeMs", SystemClock.elapsedRealtime())
        put("uptimeMs", SystemClock.uptimeMillis())
        put("estimatedBootEpochMs", System.currentTimeMillis() - SystemClock.elapsedRealtime())
    }

    private fun collectBattery(nowMs: Long): JsonObject {
        val bm = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
        val intent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val level = intent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale = intent?.getIntExtra(BatteryManager.EXTRA_SCALE, 100) ?: 100
        val pct = if (level >= 0 && scale > 0) ((level * 100f) / scale).toInt() else -1
        val temperatureTenths = intent?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, Int.MIN_VALUE)
        val voltageMv = intent?.getIntExtra(BatteryManager.EXTRA_VOLTAGE, Int.MIN_VALUE)
        val status = intent?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
        val plugged = intent?.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1) ?: -1
        val health = intent?.getIntExtra(BatteryManager.EXTRA_HEALTH, -1) ?: -1
        val currentNowUa = bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW)
        val currentAvgUa = bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_AVERAGE)
        val chargeCounterUah = bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CHARGE_COUNTER)
        val capacityPct = bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
        val energyNwh = bm.getLongProperty(BatteryManager.BATTERY_PROPERTY_ENERGY_COUNTER)
        val prevLevel = prefs.getLastBatteryLevel()
        val prevMs = prefs.getLastBatterySampleEpochMs()
        val deltaPct = if (prevLevel != null) pct - prevLevel else 0
        val deltaMinutes = if (prevMs > 0) max(1.0, (nowMs - prevMs) / 60000.0) else 0.0
        val dischargeRatePctPerHour = if (prevLevel != null && deltaMinutes > 0) (deltaPct / deltaMinutes) * 60.0 else 0.0
        prefs.setLastBatteryLevel(pct)
        prefs.setLastBatterySampleEpochMs(nowMs)
        return buildJsonObject {
            put("levelPct", pct)
            put("rawLevel", level)
            put("scale", scale)
            put("status", status)
            put("plugged", plugged)
            put("health", health)
            put("temperatureC", if (temperatureTenths != null && temperatureTenths != Int.MIN_VALUE) temperatureTenths / 10.0 else -1.0)
            put("voltageMv", voltageMv ?: -1)
            put("currentNowUa", currentNowUa)
            put("currentAvgUa", currentAvgUa)
            put("chargeCounterUah", chargeCounterUah)
            put("capacityPctProperty", capacityPct)
            put("energyCounterNwh", energyNwh)
            put("previousLevelPct", prevLevel ?: -1)
            put("previousSampleEpochMs", prevMs)
            put("deltaPctSincePrevious", deltaPct)
            put("dischargeRatePctPerHour", dischargeRatePctPerHour)
        }
    }

    private fun collectScreen(): JsonObject {
        val pm = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        val brightness = runCatching { Settings.System.getInt(context.contentResolver, Settings.System.SCREEN_BRIGHTNESS) }.getOrDefault(-1)
        val timeoutMs = runCatching { Settings.System.getInt(context.contentResolver, Settings.System.SCREEN_OFF_TIMEOUT) }.getOrDefault(-1)
        return buildJsonObject {
            put("interactive", pm.isInteractive)
            put("powerSaveMode", pm.isPowerSaveMode)
            put("screenBrightness", brightness)
            put("screenOffTimeoutMs", timeoutMs)
        }
    }


    private fun collectStorage(): JsonObject {
        val dir = context.getExternalFilesDir(null) ?: context.filesDir
        val stat = StatFs(dir.absolutePath)
        val total = stat.blockCountLong * stat.blockSizeLong
        val free = stat.availableBlocksLong * stat.blockSizeLong
        val used = (total - free).coerceAtLeast(0L)
        val usedPct = if (total > 0L) used * 100.0 / total else 0.0
        return buildJsonObject {
            put("available", true)
            put("source", "Public Storage")
            put("path", dir.absolutePath)
            put("totalBytes", total)
            put("freeBytes", free)
            put("usedBytes", used)
            put("usedPercent", usedPct)
            put("externalStorageState", Environment.getExternalStorageState())
        }
    }

    private fun collectCpu(): JsonObject = buildJsonObject {
        val procStat = ShellUtils.run("cat /proc/stat")
        val loadAvg = ShellUtils.run("cat /proc/loadavg")
        val uptime = ShellUtils.run("cat /proc/uptime")
        val cpuInfo = ShellUtils.safeTrim(ShellUtils.run("cat /proc/cpuinfo"), 4000)
        val runtime = Runtime.getRuntime()
        val cpuCoreCount = procStat.lineSequence()
            .map { it.trim() }
            .count { it.matches(Regex("cpu\\d+.*")) }
        val processCount = runCatching {
            java.io.File("/proc").listFiles()?.count { it.isDirectory && it.name.all(Char::isDigit) } ?: -1
        }.getOrDefault(-1)
        put("available", true)
        put("privilegedRequired", false)
        put("source", "public /proc and Runtime APIs")
        put("processorCount", runtime.availableProcessors())
        put("cpuCoreCountFromProcStat", cpuCoreCount)
        put("runningProcessCountFromProc", processCount)
        put("loadAvg", loadAvg)
        put("uptime", uptime)
        put("jvmFreeMemoryBytes", runtime.freeMemory())
        put("jvmTotalMemoryBytes", runtime.totalMemory())
        put("jvmMaxMemoryBytes", runtime.maxMemory())
        put("procStatSummary", ShellUtils.safeTrim(procStat.lineSequence().take(12).joinToString("\n"), 4000))
        put("cpuInfoExcerpt", cpuInfo)
        put("topCpuHeader", ShellUtils.safeTrim(ShellUtils.run("top -b -n 1 | head -n 10"), 4000))
        put("note", "Per-app CPU attribution is restricted on modern Android. This non-privileged build shows public aggregate CPU and best-effort process data.")
    }

    private fun collectProcesses(): JsonObject = buildJsonObject {
        put("topExcerpt", ShellUtils.safeTrim(ShellUtils.run("top -b -n 1 | head -n 40"), 8000))
        put("psExcerpt", ShellUtils.safeTrim(ShellUtils.run("ps -A -o PID,PPID,USER,NAME,ARGS | head -n 50"), 8000))
    }

    private fun collectCrashes(nowMs: Long): Pair<JsonObject, String> {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val packageManager = context.packageManager
        val packages = packageManager.getInstalledApplications(0).sortedBy { it.packageName }.take(40)
        val crashEntries = buildJsonArray {
            packages.forEach { app ->
                runCatching {
                    val exits = activityManager.getHistoricalProcessExitReasons(app.packageName, 0, 3)
                    exits.forEach { exit ->
                        add(buildJsonObject {
                            put("packageName", app.packageName)
                            put("reason", exit.reason)
                            put("status", exit.status)
                            put("importance", exit.importance)
                            put("timestamp", exit.timestamp)
                            put("description", exit.description ?: "")
                            put("processName", exit.processName ?: "")
                            put("pss", exit.pss)
                            put("rss", exit.rss)
                            val trace = runCatching { exit.traceInputStream?.bufferedReader()?.use { it.readText() }.orEmpty() }.getOrDefault("")
                            put("trace", ShellUtils.safeTrim(trace, 4000))
                        })
                    }
                }
            }
        }
        val summary = crashEntries.take(3).joinToString("\n\n") { elem ->
            val obj = elem as JsonObject
            "${obj["packageName"]}: reason=${obj["reason"]} description=${obj["description"]}\n${obj["trace"]}"
        }
        return buildJsonObject {
            put("recentExitReasons", crashEntries)
            put("logcatCrashes", "unavailable in non-privileged build")
            put("anrAndFatalExcerpt", "unavailable in non-privileged build")
        } to summary
    }

    private fun collectConnectivity(): JsonObject {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val wifi = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        val bluetoothAdapter = runCatching { BluetoothAdapter.getDefaultAdapter() }.getOrNull()
        val nfcAdapter = runCatching { NfcAdapter.getDefaultAdapter(context) }.getOrNull()
        val active = runCatching { cm.activeNetwork }.getOrNull()
        val caps = runCatching { cm.getNetworkCapabilities(active) }.getOrNull()
        val wifiInfo = if (hasPermission(Manifest.permission.ACCESS_WIFI_STATE)) {
            runCatching { wifi.connectionInfo?.toString().orEmpty() }.getOrDefault("")
        } else {
            "permission denied: ACCESS_WIFI_STATE"
        }
        return buildJsonObject {
            put("activeTransportWifi", caps?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true)
            put("activeTransportCellular", caps?.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) == true)
            put("activeTransportBluetooth", caps?.hasTransport(NetworkCapabilities.TRANSPORT_BLUETOOTH) == true)
            put("internetCapability", caps?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true)
            put("validatedCapability", caps?.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED) == true)
            put("wifiPermissionGranted", hasPermission(Manifest.permission.ACCESS_WIFI_STATE))
            put("wifiEnabled", runCatching { wifi.isWifiEnabled }.getOrDefault(false))
            put("wifiInfo", wifiInfo)
            put("bluetoothConnectPermissionGranted", hasPermission(Manifest.permission.BLUETOOTH_CONNECT))
            put("bluetoothState", if (hasPermission(Manifest.permission.BLUETOOTH_CONNECT)) runCatching { bluetoothAdapter?.state ?: -1 }.getOrDefault(-1) else -1)
            put("bluetoothEnabled", if (hasPermission(Manifest.permission.BLUETOOTH_CONNECT)) runCatching { bluetoothAdapter?.isEnabled == true }.getOrDefault(false) else false)
            put("bluetoothName", if (hasPermission(Manifest.permission.BLUETOOTH_CONNECT)) runCatching { bluetoothAdapter?.name ?: "" }.getOrDefault("") else "permission denied: BLUETOOTH_CONNECT")
            put("bluetoothAddress", if (hasPermission(Manifest.permission.BLUETOOTH_CONNECT)) runCatching { bluetoothAdapter?.address ?: "" }.getOrDefault("") else "permission denied: BLUETOOTH_CONNECT")
            put("nfcAvailable", nfcAdapter != null)
            put("nfcEnabled", runCatching { nfcAdapter?.isEnabled == true }.getOrDefault(false))
            put("dumpsysConnectivity", "unavailable in non-privileged build")
        }
    }

    private fun collectUsage(nowMs: Long): JsonObject {
        val usagePermissionGranted = hasPermission(Manifest.permission.PACKAGE_USAGE_STATS)
        if (!usagePermissionGranted) {
            return buildJsonObject {
                put("available", false)
                put("permissionGranted", false)
                put("permission", Manifest.permission.PACKAGE_USAGE_STATS)
                put("lastGracefulShutdownFromReceiverEpochMs", prefs.getLastGracefulShutdownEpochMs())
                put("inputLogHints", "unavailable in non-privileged build")
            }
        }
        val usm = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val since = nowMs - DateUtils.DAY_IN_MILLIS
        val events = runCatching { usm.queryEvents(since, nowMs) }.getOrNull()
        val recentEvents = mutableListOf<JsonObject>()
        var lastStartup = 0L
        var lastShutdown = 0L
        if (events != null) {
            val event = UsageEvents.Event()
            while (events.hasNextEvent()) {
                events.getNextEvent(event)
                when (event.eventType) {
                    UsageEvents.Event.DEVICE_STARTUP -> lastStartup = max(lastStartup, event.timeStamp)
                    UsageEvents.Event.DEVICE_SHUTDOWN -> lastShutdown = max(lastShutdown, event.timeStamp)
                }
                if (recentEvents.size < 50) {
                    recentEvents += buildJsonObject {
                        put("packageName", event.packageName ?: "")
                        put("className", event.className ?: "")
                        put("eventType", event.eventType)
                        put("timeStamp", event.timeStamp)
                    }
                }
            }
        }
        val lastGracefulShutdown = prefs.getLastGracefulShutdownEpochMs()
        return buildJsonObject {
            put("permissionGranted", true)
            put("lastDeviceStartupEpochMs", lastStartup)
            put("lastDeviceShutdownEpochMs", lastShutdown)
            put("lastGracefulShutdownFromReceiverEpochMs", lastGracefulShutdown)
            put("inferredUngracefulShutdown", lastStartup > 0 && lastStartup > lastGracefulShutdown)
            put("recentUsageEvents", JsonArray(recentEvents))
            put("inputLogHints", "unavailable in non-privileged build")
        }
    }

    private fun collectTelephony(): JsonObject {
        val hasTelephony = hasFeature(PackageManager.FEATURE_TELEPHONY)
        val readPhoneStateGranted = hasPermission(Manifest.permission.READ_PHONE_STATE)
        val readCallLogGranted = hasPermission(Manifest.permission.READ_CALL_LOG)
        if (!hasTelephony) {
            return buildJsonObject {
                put("available", false)
                put("reason", "Device does not report FEATURE_TELEPHONY")
                put("readPhoneStatePermissionGranted", readPhoneStateGranted)
                put("readCallLogPermissionGranted", readCallLogGranted)
            }
        }
        val tm = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        val imei = if (readPhoneStateGranted) runCatching { tm.imei ?: "" }.getOrDefault("") else ""
        val callState = if (readPhoneStateGranted) runCatching { tm.callState }.getOrDefault(-1) else -1
        val dataState = if (readPhoneStateGranted) runCatching { tm.dataState }.getOrDefault(-1) else -1
        val dataNetworkType = if (readPhoneStateGranted) runCatching { tm.dataNetworkType }.getOrDefault(-1) else -1
        val voiceNetworkType = if (readPhoneStateGranted) runCatching { tm.voiceNetworkType }.getOrDefault(-1) else -1
        val lastCall = if (readCallLogGranted) {
            ShellUtils.run("content query --uri content://call_log/calls --projection date:number:type:duration --sort \"date DESC\" --limit 1")
        } else {
            "permission denied: READ_CALL_LOG"
        }
        return buildJsonObject {
            put("available", true)
            put("readPhoneStatePermissionGranted", readPhoneStateGranted)
            put("readCallLogPermissionGranted", readCallLogGranted)
            put("imei", imei)
            put("callState", callState)
            put("networkOperatorName", runCatching { tm.networkOperatorName ?: "" }.getOrDefault(""))
            put("simOperatorName", runCatching { tm.simOperatorName ?: "" }.getOrDefault(""))
            put("dataState", dataState)
            put("dataNetworkType", dataNetworkType)
            put("voiceNetworkType", voiceNetworkType)
            put("lastCallLogEntry", lastCall)
        }
    }

    private fun collectTopDetails(): JsonObject = buildJsonObject {
        val topCommand = "top -m 40 -n 1 -o %CPU,%MEM,RES,NAME"
        val cpuInfoCommand = "dumpsys cpuinfo | head -n 40"
        val batteryStatsCommand = "dumpsys batterystats | grep -A 40 \"Estimated power use\""
        put("available", true)
        put("privilegedRequired", false)
        put("topCommand", topCommand)
        put("cpuInfoCommand", cpuInfoCommand)
        put("batteryStatsCommand", batteryStatsCommand)
        put("topProcesses", ShellUtils.safeTrim(ShellUtils.run(topCommand), 16000))
        put("cpuInfoTop", ShellUtils.safeTrim(ShellUtils.run(cpuInfoCommand), 16000))
        put("batteryStatsPower", ShellUtils.safeTrim(ShellUtils.run(batteryStatsCommand), 18000))
        put("note", "These commands are executed from the app process without privileged access. Some devices may return permission-denied or partial output for dumpsys sections.")
    }

    private fun collectPower(): JsonObject = buildJsonObject {
        val pm = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        put("available", true)
        put("privilegedRequired", false)
        put("interactive", pm.isInteractive)
        put("powerSaveMode", pm.isPowerSaveMode)
        put("ignoringBatteryOptimizations", runCatching { pm.isIgnoringBatteryOptimizations(context.packageName) }.getOrDefault(false))
        put("locationPowerSaveMode", if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) runCatching { pm.locationPowerSaveMode }.getOrDefault(-1) else -1)
        put("deviceIdleMode", if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) runCatching { pm.isDeviceIdleMode }.getOrDefault(false) else false)
        val thermalHeadroom30s = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            runCatching { pm.getThermalHeadroom(30).toDouble() }.getOrDefault(Double.NaN)
        } else {
            Double.NaN
        }
        put(
            "thermalHeadroom30s",
            if (thermalHeadroom30s.isNaN() || thermalHeadroom30s.isInfinite()) JsonNull else JsonPrimitive(thermalHeadroom30s)
        )
        put("radioPowerNote", "Per-radio power requires batterystats shell/privileged access. Best-effort power state is exposed through public PowerManager APIs.")
    }

    private fun collectShellBestEffort(): JsonObject = buildJsonObject {
        put("available", false)
        put("reason", "Shell logcat/dropbox/dumpsys diagnostics removed for non-privileged build.")
        put("shutdownMarker", prefs.getLastGracefulShutdownEpochMs())
    }

    data class SnapshotResult(val snapshotJson: JsonObject, val crashStackSummary: String)
}
