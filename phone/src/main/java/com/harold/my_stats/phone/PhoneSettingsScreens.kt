package com.harold.my_stats.phone

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
internal fun SettingsScreen(
    isDarkTheme: Boolean,
    endpointUrl: String,
    uploadIntervalMs: Long,
    onToggleTheme: () -> Unit,
    onEndpointChange: (String) -> Unit,
    onUploadIntervalChange: (Long) -> Unit,
    onPrivacy: () -> Unit,
    onAbout: () -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(start = 18.dp, top = 26.dp, end = 18.dp, bottom = 18.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        ScreenHeader("Settings", onBack)
        SectionCard("Appearance", "Choose the dashboard theme. Dark is the default.", "◐", Color(0xFF4DA3FF)) {
            Button(
                onClick = onToggleTheme,
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                Text(if (isDarkTheme) "Current Theme: Dark" else "Current Theme: Light", fontWeight = FontWeight.Bold)
            }
        }
        SectionCard("Upload Interval", "Choose how often saved reports are sent to the endpoint.", "⏱", Color(0xFFFFB74D)) {
            UploadIntervalSelector(
                selectedIntervalMs = uploadIntervalMs,
                onSelected = onUploadIntervalChange
            )
            Text(
                "Changing this value is saved locally and immediately triggers an endpoint send attempt while collection is running.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall
            )
        }
        SectionCard("Upload Endpoint", "The collector posts saved JSON reports to this endpoint.", "↗", Color(0xFF00B8A9)) {
            OutlinedTextField(
                value = endpointUrl,
                onValueChange = onEndpointChange,
                singleLine = false,
                minLines = 2,
                label = { Text("Endpoint URL") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "Saved locally in the app database and used by the background collection service.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall
            )
        }
        SettingsNavButton("Privacy Policy", "Read how diagnostics are collected and handled.", onPrivacy)
        SettingsNavButton("About", "Version and copyright information.", onAbout)
    }
}

@Composable
internal fun UploadIntervalSelector(
    selectedIntervalMs: Long,
    onSelected: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    Box(modifier = modifier.fillMaxWidth()) {
        Button(
            onClick = { expanded = true },
            shape = RoundedCornerShape(18.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                contentColor = MaterialTheme.colorScheme.onSurface
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(uploadIntervalLabel(selectedIntervalMs), fontWeight = FontWeight.Bold)
                Text("⌄", fontWeight = FontWeight.Black)
            }
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.fillMaxWidth(0.86f)
        ) {
            uploadIntervalOptions.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option.label) },
                    onClick = {
                        expanded = false
                        onSelected(option.millis)
                    }
                )
            }
        }
    }
}

@Composable
internal fun SettingsNavButton(title: String, subtitle: String, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(Modifier.padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(subtitle, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
            }
            Text("›", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        }
    }
}

@Composable
internal fun TextScreen(title: String, text: String, onBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(start = 18.dp, top = 26.dp, end = 18.dp, bottom = 18.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        ScreenHeader(title, onBack)
        Card(
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = text,
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(18.dp)
            )
        }
    }
}

@Composable
internal fun ScreenHeader(title: String, onBack: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        Button(
            onClick = onBack,
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                contentColor = MaterialTheme.colorScheme.onSurface
            ),
            modifier = Modifier.size(48.dp)
        ) {
            Text("‹", fontSize = 26.sp, fontWeight = FontWeight.Black)
        }
        Spacer(Modifier.width(14.dp))
        Text(title, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black)
    }
}

internal fun privacyPolicyText(): String = """
My Stats Privacy Policy

Effective Date: ${SimpleDateFormat("MMMM d, yyyy", Locale.US).format(Date())}

My Stats is a free Android diagnostics application for phones and wearable devices. Anyone may use the app free of charge for personal, development, validation, troubleshooting, and learning purposes.

My Stats collects device diagnostics only while the collection service is running. The collected information may include battery level, charging state, voltage, current readings where available, CPU and memory details exposed by Android, storage status, display and brightness information, boot and uptime information, connectivity status for Wi‑Fi, cellular, Bluetooth, and NFC where available, recent app-generated diagnostic data, and command output available to a normal Android application.

My Stats does not collect passwords, private messages, contacts, photos, videos, files, precise personal location, payment information, financial data, biometric data, or personal conversations. My Stats is intended to collect technical device-health information, not personal content.

Diagnostic snapshots are stored locally on the device using the app database. The endpoint configured in Settings is also stored locally in the app database. If an endpoint is configured and upload is available, My Stats may send diagnostic reports to that endpoint for troubleshooting or validation. Users and deployers are responsible for choosing an endpoint they control and trust.

My Stats does not sell personal data. My Stats does not use diagnostics for advertising. My Stats does not share diagnostics with advertising networks, data brokers, or analytics companies.

Some Android features may require runtime permissions or may be unavailable depending on the device model, Android version, manufacturer restrictions, phone/wearable capabilities, and user settings. When a value is unavailable, the app should show it as unavailable instead of attempting privileged access.

Users can stop diagnostic collection at any time using the Stop Collection Service button. When the service is stopped, live data cards and raw data views are hidden until collection is started again. Locally stored diagnostics may remain on the device until the app is cleared, uninstalled, or updated to delete older records.

This policy may be updated as the app evolves. Continued use of My Stats after updates means the current policy applies to the app version being used.
""".trimIndent()

internal fun aboutText(): String {
    val year = Calendar.getInstance().get(Calendar.YEAR)
    return """
My Stats

Developed by Harold Paulino

Copyright © $year Harold Paulino. All rights reserved.

My Stats is a free diagnostics dashboard for Android phones and wearable devices.
""".trimIndent()
}

@Preview(name = "Phone Settings Screen Dark", showBackground = true, widthDp = 390, heightDp = 900)
@Composable
fun PhoneSettingsScreenDarkPreview() {
    PhonePreviewSurface {
        SettingsScreen(
            isDarkTheme = true,
            endpointUrl = "https://sparqm.com/web/gabb/debug/debug_report_post.php",
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

@Preview(name = "Phone Settings Screen Light", showBackground = true, widthDp = 390, heightDp = 900)
@Composable
fun PhoneSettingsScreenLightPreview() {
    PhoneTheme(isDarkTheme = false) {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            SettingsScreen(
                isDarkTheme = false,
                endpointUrl = "https://sparqm.com/web/gabb/debug/debug_report_post.php",
                uploadIntervalMs = 5 * 60 * 1000L,
                onToggleTheme = {},
                onEndpointChange = {},
                onUploadIntervalChange = {},
                onPrivacy = {},
                onAbout = {},
                onBack = {}
            )
        }
    }
}

@Preview(name = "Phone Upload Interval Selector", showBackground = true, widthDp = 390, heightDp = 160)
@Composable
fun PhoneUploadIntervalSelectorPreview() {
    PhonePreviewSurface {
        UploadIntervalSelector(
            selectedIntervalMs = 10 * 60 * 1000L,
            onSelected = {},
            modifier = Modifier.padding(18.dp)
        )
    }
}

@Preview(name = "Phone Privacy Screen", showBackground = true, widthDp = 390, heightDp = 900)
@Composable
fun PhonePrivacyTextScreenPreview() {
    PhonePreviewSurface {
        TextScreen(
            title = "Privacy Policy",
            text = privacyPolicyText(),
            onBack = {}
        )
    }
}

@Preview(name = "Phone About Screen", showBackground = true, widthDp = 390, heightDp = 640)
@Composable
fun PhoneAboutTextScreenPreview() {
    PhonePreviewSurface {
        TextScreen(
            title = "About",
            text = aboutText(),
            onBack = {}
        )
    }
}
