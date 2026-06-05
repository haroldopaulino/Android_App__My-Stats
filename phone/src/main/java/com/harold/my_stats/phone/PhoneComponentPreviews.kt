package com.harold.my_stats.phone

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Preview(name = "App Header Running", showBackground = true, widthDp = 390, heightDp = 250)
@Composable
fun AppHeaderRunningPreview() {
    PhonePreviewSurface {
        AppHeader(
                model = "Pixel 9 Pro",
                onOpenSettings = {},
                modifier = Modifier.padding(18.dp)
            )
    }
}

@Preview(name = "App Header Stopped", showBackground = true, widthDp = 390, heightDp = 250)
@Composable
fun AppHeaderStoppedPreview() {
    PhonePreviewSurface {
        AppHeader(
                model = "Android Phone",
                onOpenSettings = {},
                modifier = Modifier.padding(18.dp)
            )
    }
}

@Preview(name = "Settings Header Button", showBackground = true, widthDp = 120, heightDp = 120)
@Composable
fun SettingsHeaderButtonPreview() {
    PhonePreviewSurface {
        Surface(modifier = Modifier.padding(32.dp)) {
            SettingsHeaderButton(onClick = {})
        }
    }
}

@Preview(name = "Full App Header Running", showBackground = true, widthDp = 390, heightDp = 250)
@Composable
fun FullAppHeaderRunningPreview() {
    PhonePreviewSurface {
        AppHeader(
                model = "Pixel 9 Pro",
                onOpenSettings = {},
                modifier = Modifier.padding(18.dp)
            )
    }
}

@Preview(name = "Full App Header Stopped", showBackground = true, widthDp = 390, heightDp = 250)
@Composable
fun FullAppHeaderStoppedPreview() {
    PhonePreviewSurface {
        AppHeader(
                model = "Android Phone",
                onOpenSettings = {},
                modifier = Modifier.padding(18.dp)
            )
    }
}

@Preview(name = "Metric Grid", showBackground = true, widthDp = 390, heightDp = 520)
@Composable
fun MetricGridPreview() {
    PhonePreviewSurface {
        Column(modifier = Modifier.padding(18.dp)) {
            MetricGrid(metricsFor(previewDashboardState().snapshot))
        }
    }
}

@Preview(name = "Dashboard Metric Card", showBackground = true, widthDp = 210, heightDp = 180)
@Composable
fun DashboardMetricCardPreview() {
    PhonePreviewSurface {
        DashboardMetricCard(
            metric = MetricSpec(
                title = "Battery",
                value = "86%",
                line1 = "31.4°C",
                line2 = "2.35%/hr",
                iconText = "▯",
                accent = Color(0xFF67E08E)
            ),
            modifier = Modifier.padding(18.dp).fillMaxWidth()
        )
    }
}

@Preview(name = "Start Prompt Card", showBackground = true, widthDp = 390, heightDp = 170)
@Composable
fun StartPromptCardPreview() {
    PhonePreviewSurface {
        StartPromptCard(modifier = Modifier.padding(18.dp))
    }
}

@Preview(name = "Dark Tabs", showBackground = true, widthDp = 390, heightDp = 90)
@Composable
fun DarkTabsPreview() {
    PhonePreviewSurface {
        DarkTabs(
            titles = listOf("System", "Top Details", "Raw Data"),
            selectedIndex = 0,
            onSelected = {}
        )
    }
}

@Preview(name = "Section Card", showBackground = true, widthDp = 390, heightDp = 210)
@Composable
fun SectionCardPreview() {
    PhonePreviewSurface {
        SectionCard(
            title = "Battery Details",
            subtitle = "Charge, health, voltage, current, and drain",
            icon = "▯",
            accent = Color(0xFF67E08E),
            modifier = Modifier.padding(18.dp)
        ) {
            StatusRow("Level", "86%", Color(0xFF9ADBC9))
            StatusRow("Status", "Discharging", Color(0xFFB7C8DA))
            StatusRow("Voltage", "3942 mV", Color(0xFFB7C8DA))
        }
    }
}

@Preview(name = "Code Block", showBackground = true, widthDp = 390, heightDp = 220)
@Composable
fun CodeBlockPreview() {
    PhonePreviewSurface {
        CodeBlock(
            text = "{\n  \"battery\": 86,\n  \"model\": \"Pixel 9 Pro\"\n}"
        )
    }
}

@Preview(name = "Settings Navigation Button", showBackground = true, widthDp = 390, heightDp = 130)
@Composable
fun SettingsNavButtonPreview() {
    PhonePreviewSurface {
        SettingsNavButton(
            title = "Privacy Policy",
            subtitle = "Review what this app collects",
            onClick = {}
        )
    }
}

@Preview(name = "Phone Full Home Preview", showBackground = true, widthDp = 390, heightDp = 900)
@Composable
fun PhoneFullHomePreview() {
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
