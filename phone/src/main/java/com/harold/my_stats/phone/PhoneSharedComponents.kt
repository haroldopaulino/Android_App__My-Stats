package com.harold.my_stats.phone

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview

@Composable
internal fun StartPromptCard(modifier: Modifier = Modifier) {
    SectionCard("Collection service is stopped", "Start the service to begin collecting live diagnostics.", "▶", Color(0xFF4DA3FF), modifier) {
        Text("Live diagnostics are hidden while the collection service is stopped.", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
internal fun RunningStatusChip(isRunning: Boolean) {
    Row(modifier = Modifier.clip(RoundedCornerShape(50)).background(if (isRunning) Color(0xFF133D25) else Color(0xFF3A3214)).padding(horizontal = 10.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
        if (isRunning) {
            RunningDot(Color(0xFF67E08E))
            Spacer(Modifier.width(6.dp))
        }
        Text(if (isRunning) "Running" else "Stopped", color = if (isRunning) Color(0xFF67E08E) else Color(0xFFFFD166), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)
    }
}

@Composable
internal fun RunningDot(color: Color) {
    val transition = rememberInfiniteTransition(label = "collector-running")
    val alpha by transition.animateFloat(0.35f, 1f, infiniteRepeatable(animation = tween(650), repeatMode = RepeatMode.Reverse), label = "collector-running-alpha")
    Box(modifier = Modifier.size(10.dp).clip(CircleShape).alpha(alpha).background(color))
}

@Composable
internal fun SectionCard(title: String, subtitle: String, icon: String, accent: Color, modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Card(shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), modifier = modifier.fillMaxWidth()) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconBadge(icon, accent, Modifier.size(44.dp))
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(subtitle, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall, maxLines = 2, overflow = TextOverflow.Ellipsis)
                }
            }
            Divider(color = Color(0xFF26323E))
            content()
        }
    }
}

@Composable
internal fun CommandCard(title: String, icon: String, accent: Color, command: String, output: String) {
    SectionCard(title, "Command-style detail output", icon, accent) {
        Text(command, color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
        PermissionAwareCodeBlock(command = command, output = output.ifBlank { "No output returned by command." })
    }
}

@Composable
internal fun PermissionAwareStatusRow(label: String, value: String, command: String) {
    val denied = isPermissionDenied(value)
    var showDialog by remember { mutableStateOf(false) }
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis)
        if (denied) {
            Text(
                text = "info",
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                textDecoration = TextDecoration.Underline,
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(Color(0xFF10233A))
                    .clickable { showDialog = true }
                    .padding(horizontal = 12.dp, vertical = 7.dp)
            )
        } else {
            StatusChip(value.take(48), Color(0xFFB7C8DA))
        }
    }
    if (showDialog) {
        PermissionDeniedDialog(command = command, output = value, onDismiss = { showDialog = false })
    }
}

@Composable
internal fun PermissionAwareCodeBlock(command: String, output: String) {
    var showDialog by remember { mutableStateOf(false) }
    if (isPermissionDenied(output)) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFF071018))
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("Permission denied while running command", color = Color(0xFFFFB4AB), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
            Text(
                text = "info",
                color = MaterialTheme.colorScheme.primary,
                textDecoration = TextDecoration.Underline,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable { showDialog = true }
            )
        }
    } else {
        CodeBlock(output)
    }
    if (showDialog) {
        PermissionDeniedDialog(command = command, output = output, onDismiss = { showDialog = false })
    }
}

@Composable
internal fun PermissionDeniedDialog(command: String, output: String, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = { TextButton(onClick = onDismiss) { Text("OK") } },
        title = { Text("Command Permission Denied") },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Full command", fontWeight = FontWeight.Bold)
                CodeBlock(command)
                Text("Command output", fontWeight = FontWeight.Bold)
                CodeBlock(output.ifBlank { "No output returned." })
                Text("Permission was denied by Android for this non-privileged app.", color = Color(0xFFFFB4AB), fontWeight = FontWeight.Bold)
            }
        }
    )
}

@Composable
internal fun StatusRow(label: String, value: String, color: Color) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis)
        StatusChip(value, color)
    }
}

@Composable
internal fun StatusChip(text: String, color: Color) {
    Text(text = text, color = Color(0xFF071018), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.clip(RoundedCornerShape(50)).background(color).padding(horizontal = 10.dp, vertical = 6.dp))
}

@Composable
internal fun CodeBlock(text: String) {
    Text(text = text, style = MaterialTheme.typography.bodySmall, color = Color(0xFFD6DEE7), modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(Color(0xFF071018)).padding(14.dp))
}


@Preview(name = "Phone Start Prompt Card", showBackground = true, widthDp = 390, heightDp = 220)
@Composable
fun PhoneSharedStartPromptCardPreview() {
    PhonePreviewSurface {
        StartPromptCard(modifier = Modifier.padding(18.dp))
    }
}

@Preview(name = "Phone Section Card", showBackground = true, widthDp = 390, heightDp = 240)
@Composable
fun PhoneSharedSectionCardPreview() {
    PhonePreviewSurface {
        SectionCard(
            title = "Upload Endpoint",
            subtitle = "Saved JSON reports are posted every 10 minutes.",
            icon = "↗",
            accent = Color(0xFF00B8A9),
            modifier = Modifier.padding(18.dp)
        ) {
            Text("Collection runs every 10 seconds and retains up to 8,640 local records.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Preview(name = "Phone Command Card", showBackground = true, widthDp = 390, heightDp = 280)
@Composable
fun PhoneSharedCommandCardPreview() {
    PhonePreviewSurface {
        CommandCard(
            title = "CPU Details",
            icon = "▣",
            accent = Color(0xFF2F8DFF),
            command = "cat /proc/loadavg",
            output = "1.32 0.98 0.74"
        )
    }
}

@Preview(name = "Phone Status Rows", showBackground = true, widthDp = 390, heightDp = 160)
@Composable
fun PhoneSharedStatusRowsPreview() {
    PhonePreviewSurface {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            StatusRow("Battery", "86%", Color(0xFF67E08E))
            PermissionAwareStatusRow("CPU cores", "8", "cat /proc/stat")
            RunningStatusChip(isRunning = true)
        }
    }
}
