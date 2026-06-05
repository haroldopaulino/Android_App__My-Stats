package com.harold.my_stats.phone

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject

private val previewSnapshot: JsonObject
    get() = Json.parseToJsonElement(
        """
        {
          "battery": {
            "levelPct": 86,
            "temperatureC": 31.4,
            "dischargeRatePctPerHour": 2.35,
            "rawLevel": 86,
            "scale": 100,
            "status": 3,
            "plugged": 0,
            "health": 2,
            "voltageMv": 3942,
            "currentNowUa": -228000,
            "currentAvgUa": -180000,
            "chargeCounterUah": 4120000,
            "energyCounterNwh": 15200000
          },
          "cpu": {
            "processorCount": 8,
            "cpuCoreCountFromProcStat": 8,
            "runningProcessCountFromProc": 147,
            "loadAvg": "1.32 0.98 0.74",
            "uptime": "34978.43 89320.12",
            "jvmTotalMemoryBytes": 268435456,
            "jvmFreeMemoryBytes": 89128960,
            "jvmMaxMemoryBytes": 536870912,
            "procStatSummary": "8 cores visible"
          },
          "screen": {
            "interactive": true,
            "screenBrightness": 178,
            "screenOffTimeoutMs": 30000,
            "powerSaveMode": false
          },
          "storage": {
            "freeBytes": 42152755200,
            "totalBytes": 128849018880,
            "source": "Public Storage"
          },
          "connectivity": {
            "activeTransportWifi": true,
            "wifiEnabled": true,
            "activeTransportCellular": false,
            "activeTransportBluetooth": true,
            "bluetoothEnabled": true,
            "bluetoothConnectPermissionGranted": true,
            "nfcAvailable": true,
            "nfcEnabled": false,
            "internetCapability": true,
            "validatedCapability": true
          },
          "build": {
            "manufacturer": "Google",
            "model": "Pixel 9 Pro",
            "release": "16",
            "sdkInt": 36,
            "estimatedBootEpochMs": 1780418400000,
            "bootElapsedRealtimeMs": 17800000,
            "uptimeMs": 17650000
          },
          "usage": {
            "lastDeviceStartupEpochMs": 1780418400000,
            "lastDeviceShutdownEpochMs": 1780332000000,
            "lastGracefulShutdownFromReceiverEpochMs": 1780331980000,
            "inferredUngracefulShutdown": false,
            "permissionGranted": true
          },
          "power": {
            "interactive": true,
            "powerSaveMode": false,
            "ignoringBatteryOptimizations": true,
            "deviceIdleMode": false,
            "locationPowerSaveMode": 0,
            "thermalHeadroom30s": 0.82
          },
          "topDetails": {
            "topCommand": "top -m 40 -n 1 -o %CPU,%MEM,RES,NAME",
            "topProcesses": "CPU% MEM% RES NAME\n12.1 4.2 180M system_server\n7.4 2.1 94M com.harold.my_stats.phone\n3.2 1.5 64M surfaceflinger",
            "cpuInfoCommand": "dumpsys cpuinfo | head -n 40",
            "cpuInfoTop": "12% system_server\n7% com.harold.my_stats.phone\n3% surfaceflinger",
            "batteryStatsCommand": "dumpsys batterystats | grep -A 40 Estimated power use",
            "batteryStatsPower": "Estimated power use\nScreen: 124 mAh\nWi-Fi: 31 mAh\nMy Stats: 9 mAh",
            "note": "Preview data only. Live values come from the collector service."
          }
        }
        """.trimIndent()
    ).jsonObject

internal fun previewDashboardState(): DashboardState = DashboardState(
    snapshot = previewSnapshot,
    pendingCount = 2,
    latestPayload = "{\n  \"category\": \"diagnostics\",\n  \"battery\": 86,\n  \"model\": \"Pixel 9 Pro\"\n}",
    refreshedAt = "08:42 AM",
    collectedCount = 145,
    collectedDataSizeBytes = 284_672,
    sentEndpointCount = 120,
    failedEndpointCount = 2
)

@Composable
internal fun PhonePreviewSurface(content: @Composable () -> Unit) {
    PhoneTheme(isDarkTheme = true) {
        Surface(modifier = Modifier.fillMaxSize(), color = androidx.compose.material3.MaterialTheme.colorScheme.background) {
            content()
        }
    }
}

@Preview(name = "Phone Home Running", showBackground = true, widthDp = 390, heightDp = 900)
@Composable
fun PhoneHomeRunningPreview() {
    val state = previewDashboardState()
    PhonePreviewSurface {
        PhoneHomeScreen(
            uiState = PhoneUiState(
                dashboard = state,
                isRunning = true,
                phoneName = "Pixel 9 Pro"
            ),
            state = state,
            onToggleService = {},
            onOpenSettings = {},
            onMainTabSelected = {},
            onDetailTabSelected = {}
        )
    }
}

@Preview(name = "Phone Home Stopped", showBackground = true, widthDp = 390, heightDp = 720)
@Composable
fun PhoneHomeStoppedPreview() {
    PhonePreviewSurface {
        PhoneHomeScreen(
            uiState = PhoneUiState(
                isRunning = false,
                phoneName = "Pixel 9 Pro"
            ),
            state = DashboardState(),
            onToggleService = {},
            onOpenSettings = {},
            onMainTabSelected = {},
            onDetailTabSelected = {}
        )
    }
}

@Preview(name = "Phone Settings", showBackground = true, widthDp = 390, heightDp = 900)
@Composable
fun PhoneSettingsPreview() {
    PhonePreviewSurface {
        SettingsScreen(
            isDarkTheme = true,
            endpointUrl = "https://example.com/api/debug-reports",
            uploadIntervalMs = 10 * 60 * 1000L,
            onToggleTheme = {},
            onEndpointChange = {},
            onUploadIntervalChange = {},
            onPrivacy = {},
            onAbout = {},
            onBack = {}
        )
    }
}

@Preview(name = "Phone Privacy", showBackground = true, widthDp = 390, heightDp = 900)
@Composable
fun PhonePrivacyPreview() {
    PhonePreviewSurface {
        TextScreen(title = "Privacy Policy", text = privacyPolicyText(), onBack = {})
    }
}

@Preview(name = "Phone About", showBackground = true, widthDp = 390, heightDp = 720)
@Composable
fun PhoneAboutPreview() {
    PhonePreviewSurface {
        TextScreen(title = "About", text = aboutText(), onBack = {})
    }
}

@Preview(name = "Phone System Tab", showBackground = true, widthDp = 390, heightDp = 900)
@Composable
fun PhoneSystemTabPreview() {
    PhonePreviewSurface {
        SystemTab(previewDashboardState())
    }
}

@Preview(name = "Phone Top Details Tab", showBackground = true, widthDp = 390, heightDp = 900)
@Composable
fun PhoneTopDetailsTabPreview() {
    PhonePreviewSurface {
        TopDetailsTab(snapshot = previewSnapshot, selectedIndex = 0, onSelected = {})
    }
}

@Preview(name = "Phone Raw Data Tab", showBackground = true, widthDp = 390, heightDp = 900)
@Composable
fun PhoneRawDataTabPreview() {
    PhonePreviewSurface {
        RawDataTab(previewDashboardState())
    }
}
