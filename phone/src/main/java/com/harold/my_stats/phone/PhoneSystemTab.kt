package com.harold.my_stats.phone

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import com.harold.my_stats.util.JsonUtils
import kotlinx.serialization.json.JsonObject

@Composable
internal fun SystemTab(state: DashboardState) {
    val snap = state.snapshot
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("OVERVIEW", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
        MetricGrid(metricsFor(snap))

        SectionCard("CPU Details", "Public, non-privileged CPU and runtime data", "▣", Color(0xFF2F8DFF)) {
            StatusRow("Runtime processors", snap.obj("cpu")?.int("processorCount")?.toString() ?: "--", Color(0xFF8CC7FF))
            PermissionAwareStatusRow("CPU cores from /proc/stat", permissionSensitiveValue(snap.obj("cpu")?.string("procStatSummary"), snap.obj("cpu")?.int("cpuCoreCountFromProcStat")?.toString() ?: "--"), "cat /proc/stat")
            StatusRow("Running process count", processLabel(snap), Color(0xFFB7C8DA))
            PermissionAwareStatusRow("Load average", snap.obj("cpu")?.string("loadAvg")?.take(48) ?: "--", "cat /proc/loadavg")
            PermissionAwareStatusRow("/proc uptime", snap.obj("cpu")?.string("uptime")?.take(40) ?: "--", "cat /proc/uptime")
            StatusRow("JVM used memory", memoryLabel(snap), Color(0xFFB7C8DA))
            StatusRow("JVM free memory", bytesToMb(snap.obj("cpu")?.long("jvmFreeMemoryBytes")), Color(0xFFB7C8DA))
            StatusRow("JVM total memory", bytesToMb(snap.obj("cpu")?.long("jvmTotalMemoryBytes")), Color(0xFFB7C8DA))
            StatusRow("JVM max memory", bytesToMb(snap.obj("cpu")?.long("jvmMaxMemoryBytes")), Color(0xFFB7C8DA))
        }

        SectionCard("Battery Details", "Charge, health, voltage, current, and drain", "▯", Color(0xFF67E08E)) {
            StatusRow("Level", batteryLabel(snap), Color(0xFF9ADBC9))
            StatusRow("Raw level / scale", "${snap.obj("battery")?.int("rawLevel") ?: "--"} / ${snap.obj("battery")?.int("scale") ?: "--"}", Color(0xFFB7C8DA))
            StatusRow("Status", batteryStatusLabel(snap), Color(0xFFB7C8DA))
            StatusRow("Plugged", pluggedLabel(snap.obj("battery")?.int("plugged")), Color(0xFFB7C8DA))
            StatusRow("Health", healthLabel(snap.obj("battery")?.int("health")), Color(0xFFB7C8DA))
            StatusRow("Temperature", tempLabel(snap), Color(0xFFB7C8DA))
            StatusRow("Voltage", snap.obj("battery")?.int("voltageMv")?.let { "${it} mV" } ?: "--", Color(0xFFB7C8DA))
            StatusRow("Current now", snap.obj("battery")?.int("currentNowUa")?.let { "${it} µA" } ?: "--", Color(0xFFB7C8DA))
            StatusRow("Current average", snap.obj("battery")?.int("currentAvgUa")?.let { "${it} µA" } ?: "--", Color(0xFFB7C8DA))
            StatusRow("Charge counter", snap.obj("battery")?.int("chargeCounterUah")?.let { "${it} µAh" } ?: "--", Color(0xFFB7C8DA))
            StatusRow("Energy counter", snap.obj("battery")?.long("energyCounterNwh")?.let { "${it} nWh" } ?: "--", Color(0xFFB7C8DA))
            StatusRow("Discharge rate", dischargeLabel(snap), Color(0xFFB7C8DA))
        }


        SectionCard("Storage", "Public app-visible storage capacity and space", "▤", Color(0xFFFFC107)) {
            StatusRow("Used", storageUsedLabel(snap), Color(0xFFB7C8DA))
            StatusRow("Free", bytesReadable(snap.obj("storage")?.long("freeBytes")), Color(0xFF9ADBC9))
            StatusRow("Total", bytesReadable(snap.obj("storage")?.long("totalBytes")), Color(0xFFB7C8DA))
            StatusRow("Scope", "Public Storage", Color(0xFFB7C8DA))
            StatusRow("Source", snap.obj("storage")?.string("source") ?: "Public Storage", Color(0xFFB7C8DA))
        }

        SectionCard("Connectivity", "Wi‑Fi, cellular, Bluetooth, NFC, and internet state", "≋", Color(0xFF00B8A9)) {
            StatusRow("Wi‑Fi transport", boolLabel(snap.obj("connectivity")?.bool("activeTransportWifi")), boolColor(snap.obj("connectivity")?.bool("activeTransportWifi")))
            StatusRow("Wi‑Fi enabled", boolLabel(snap.obj("connectivity")?.bool("wifiEnabled")), boolColor(snap.obj("connectivity")?.bool("wifiEnabled")))
            StatusRow("Cellular transport", boolLabel(snap.obj("connectivity")?.bool("activeTransportCellular")), boolColor(snap.obj("connectivity")?.bool("activeTransportCellular")))
            StatusRow("Bluetooth transport", boolLabel(snap.obj("connectivity")?.bool("activeTransportBluetooth")), boolColor(snap.obj("connectivity")?.bool("activeTransportBluetooth")))
            StatusRow("Bluetooth enabled", boolLabel(snap.obj("connectivity")?.bool("bluetoothEnabled")), boolColor(snap.obj("connectivity")?.bool("bluetoothEnabled")))
            StatusRow("Bluetooth permission", boolLabel(snap.obj("connectivity")?.bool("bluetoothConnectPermissionGranted")), boolColor(snap.obj("connectivity")?.bool("bluetoothConnectPermissionGranted")))
            StatusRow("NFC available", boolLabel(snap.obj("connectivity")?.bool("nfcAvailable")), boolColor(snap.obj("connectivity")?.bool("nfcAvailable")))
            StatusRow("NFC enabled", boolLabel(snap.obj("connectivity")?.bool("nfcEnabled")), boolColor(snap.obj("connectivity")?.bool("nfcEnabled")))
            StatusRow("Internet capability", boolLabel(snap.obj("connectivity")?.bool("internetCapability")), boolColor(snap.obj("connectivity")?.bool("internetCapability")))
            StatusRow("Internet validated", boolLabel(snap.obj("connectivity")?.bool("validatedCapability")), boolColor(snap.obj("connectivity")?.bool("validatedCapability")))
        }

        SectionCard("Boot Details", "Startup, shutdown, uptime, and usage-state signals", "⏱", Color(0xFFFFA726)) {
            StatusRow("Estimated boot", epochLabel(snap.obj("build")?.long("estimatedBootEpochMs")), Color(0xFFB7C8DA))
            StatusRow("Elapsed realtime", durationLabel(snap.obj("build")?.long("bootElapsedRealtimeMs")), Color(0xFFB7C8DA))
            StatusRow("Uptime", durationLabel(snap.obj("build")?.long("uptimeMs")), Color(0xFFB7C8DA))
            StatusRow("Last usage startup", epochLabel(snap.obj("usage")?.long("lastDeviceStartupEpochMs")), Color(0xFFB7C8DA))
            StatusRow("Last usage shutdown", epochLabel(snap.obj("usage")?.long("lastDeviceShutdownEpochMs")), Color(0xFFB7C8DA))
            StatusRow("Last graceful shutdown", epochLabel(snap.obj("usage")?.long("lastGracefulShutdownFromReceiverEpochMs")), Color(0xFFB7C8DA))
            StatusRow("Ungraceful shutdown", boolLabel(snap.obj("usage")?.bool("inferredUngracefulShutdown")), if (snap.obj("usage")?.bool("inferredUngracefulShutdown") == true) Color(0xFFFFB4AB) else Color(0xFF9ADBC9))
            StatusRow("Usage stats permission", boolLabel(snap.obj("usage")?.bool("permissionGranted")), boolColor(snap.obj("usage")?.bool("permissionGranted")))
        }

        SectionCard("Power Details", "Interactive state, battery optimization, thermal headroom", "⚡", Color(0xFFFFD166)) {
            StatusRow("Interactive", boolLabel(snap.obj("power")?.bool("interactive")), boolColor(snap.obj("power")?.bool("interactive")))
            StatusRow("Power save", boolLabel(snap.obj("power")?.bool("powerSaveMode")), boolColor(snap.obj("power")?.bool("powerSaveMode")))
            StatusRow("Ignoring optimizations", boolLabel(snap.obj("power")?.bool("ignoringBatteryOptimizations")), boolColor(snap.obj("power")?.bool("ignoringBatteryOptimizations")))
            StatusRow("Device idle", boolLabel(snap.obj("power")?.bool("deviceIdleMode")), boolColor(snap.obj("power")?.bool("deviceIdleMode")))
            StatusRow("Location power mode", snap.obj("power")?.int("locationPowerSaveMode")?.toString() ?: "--", Color(0xFFB7C8DA))
            StatusRow("Thermal headroom", snap.obj("power")?.double("thermalHeadroom30s")?.let { String.format(java.util.Locale.US, "%.2f", it) } ?: "--", Color(0xFFB7C8DA))
        }

        SectionCard("Screen Details", "Brightness, interactivity, timeout, and display power", "◐", Color(0xFFB36BFF)) {
            StatusRow("Interactive", boolLabel(snap.obj("screen")?.bool("interactive")), boolColor(snap.obj("screen")?.bool("interactive")))
            StatusRow("Brightness", brightnessLabel(snap), Color(0xFFB7C8DA))
            StatusRow("Screen timeout", durationLabel(snap.obj("screen")?.int("screenOffTimeoutMs")?.toLong()), Color(0xFFB7C8DA))
            StatusRow("Power save", boolLabel(snap.obj("screen")?.bool("powerSaveMode")), boolColor(snap.obj("screen")?.bool("powerSaveMode")))
        }

        SectionCard("Device Build", "Hardware and Android software identity", "▤", Color(0xFFFFC107)) {
            StatusRow("Manufacturer", snap.obj("build")?.string("manufacturer") ?: "--", Color(0xFFB7C8DA))
            StatusRow("Model", snap.obj("build")?.string("model") ?: "--", Color(0xFFB7C8DA))
            StatusRow("Android", snap.obj("build")?.string("release") ?: "--", Color(0xFFB7C8DA))
            StatusRow("SDK", snap.obj("build")?.int("sdkInt")?.toString() ?: "--", Color(0xFFB7C8DA))
        }

    }
}

@Composable
internal fun RawDataTab(state: DashboardState) {
    val snap = state.snapshot
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        SectionCard("Latest Collected JSON", "Raw snapshot saved and uploaded by the collector", "{ }", Color(0xFF78909C)) {
            CodeBlock(snap?.let { JsonUtils.encodeElement(it) } ?: "Loading...")
        }
        SectionCard("Latest Saved Payload", "Most recent Room database report payload", "DB", Color(0xFF607D8B)) {
            CodeBlock(state.latestPayload)
        }
    }
}

@Composable
internal fun TopDetailsTab(snapshot: JsonObject?, selectedIndex: Int, onSelected: (Int) -> Unit) {
    Column(modifier = Modifier.fillMaxSize()) {
        DarkTabs(titles = listOf("CPU & Memory", "Resource", "Battery"), selectedIndex = selectedIndex, onSelected = onSelected)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            val topDetails = snapshot.obj("topDetails")
            when (selectedIndex) {
                0 -> CommandCard("CPU & Memory", "▣", Color(0xFF2F8DFF), topDetails?.string("topCommand") ?: "top -m 40 -n 1 -o %CPU,%MEM,RES,NAME", topDetails?.string("topProcesses") ?: "Waiting for collector...")
                1 -> CommandCard("Resource", "≡", Color(0xFF00B8A9), topDetails?.string("cpuInfoCommand") ?: "dumpsys cpuinfo | head -n 40", topDetails?.string("cpuInfoTop") ?: "Waiting for collector...")
                else -> CommandCard("Battery", "▯", Color(0xFF67E08E), topDetails?.string("batteryStatsCommand") ?: "dumpsys batterystats | grep -A 40 \"Estimated power use\"", topDetails?.string("batteryStatsPower") ?: "Waiting for collector...")
            }
            SectionCard("Collection Notes", "Command output availability depends on Android build and shell restrictions", "i", Color(0xFF8CC7FF)) {
                Text(topDetails?.string("note") ?: "These details are collected from public app-process shell commands when available.", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}


@Preview(name = "Phone System Tab", showBackground = true, widthDp = 390, heightDp = 900)
@Composable
fun PhoneSystemTabInFilePreview() {
    PhonePreviewSurface {
        SystemTab(previewDashboardState())
    }
}

@Preview(name = "Phone Top Details Tab", showBackground = true, widthDp = 390, heightDp = 900)
@Composable
fun PhoneTopDetailsTabInFilePreview() {
    PhonePreviewSurface {
        TopDetailsTab(
            snapshot = previewDashboardState().snapshot,
            selectedIndex = 0,
            onSelected = {}
        )
    }
}

@Preview(name = "Phone Raw Data Tab", showBackground = true, widthDp = 390, heightDp = 900)
@Composable
fun PhoneRawDataTabInFilePreview() {
    PhonePreviewSurface {
        RawDataTab(previewDashboardState())
    }
}
