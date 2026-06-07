package com.harold.my_stats.wearable

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.tooling.preview.Preview
import androidx.wear.compose.material.Card
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text

@Composable
internal fun WearHeader(battery: String, subtitle: String, isRunning: Boolean) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.padding(top = 10.dp, bottom = 6.dp)) {
        Text("10:30", style = MaterialTheme.typography.caption1, color = Color.White, textAlign = TextAlign.Center)
        Text("My Stats", style = MaterialTheme.typography.title2, fontWeight = FontWeight.Bold, color = Color(0xFF4DA3FF), textAlign = TextAlign.Center)
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (isRunning) {
                RunningDot(Color(0xFF22D35F))
                Spacer(Modifier.width(6.dp))
            }
            Text(if (isRunning) "Running • $battery" else "Stopped", style = MaterialTheme.typography.caption1, color = if (isRunning) Color(0xFF22D35F) else Color(0xFFFFD166), textAlign = TextAlign.Center, maxLines = 1)
        }
        Text(subtitle, style = MaterialTheme.typography.caption2, color = Color(0xFFC7D0D9), textAlign = TextAlign.Center, maxLines = 1)
    }
}


@Composable
internal fun WearCollectionCounters(collected: Long, collectedDataSizeBytes: Long, sent: Long, failed: Long) {
    Card(onClick = {}, modifier = Modifier.fillMaxWidth(0.94f).padding(vertical = 4.dp), backgroundPainter = androidx.wear.compose.material.CardDefaults.cardBackgroundPainter(startBackgroundColor = Color(0xFF101820), endBackgroundColor = Color(0xFF19232D)), shape = RoundedCornerShape(22.dp)) {
        Column(Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 6.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            WearCounterRow("Total Collection:", collected.toString())
            WearCounterRow("Local Data:", formatByteSize(collectedDataSizeBytes))
            WearCounterRow("Collections Sent:", sent.toString())
            WearCounterRow("Failed to send:", failed.toString())
        }
    }
}

@Composable
private fun WearCounterRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Text(label, style = MaterialTheme.typography.caption2, color = Color(0xFFC7D0D9), maxLines = 2, modifier = Modifier.weight(1f))
        Text(value, style = MaterialTheme.typography.caption1, color = Color.White, fontWeight = FontWeight.Bold, maxLines = 1, textAlign = TextAlign.End)
    }
}

@Composable
internal fun WearStoppedCard() {
    Card(onClick = {}, modifier = Modifier.fillMaxWidth(0.9f).padding(vertical = 4.dp), backgroundPainter = androidx.wear.compose.material.CardDefaults.cardBackgroundPainter(startBackgroundColor = Color(0xFF101820), endBackgroundColor = Color(0xFF19232D)), shape = RoundedCornerShape(22.dp)) {
        Column(Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            IconBadge("▶", Color(0xFF4DA3FF), Modifier.size(12.dp))
            Spacer(Modifier.height(8.dp))
            Text("Start the service to show collected stats.", color = Color(0xFFC7D0D9), textAlign = TextAlign.Center, style = MaterialTheme.typography.caption1)
        }
    }
}

@Composable
internal fun WearMetricCard(row: WearMetric) {
    var selectedInfo by remember { mutableStateOf<String?>(null) }
    Card(
        onClick = { row.infoDescription?.let { selectedInfo = it } },
        modifier = Modifier.fillMaxWidth(0.94f).padding(vertical = 4.dp),
        backgroundPainter = androidx.wear.compose.material.CardDefaults.cardBackgroundPainter(startBackgroundColor = Color(0xFF101820), endBackgroundColor = Color(0xFF19232D)),
        shape = RoundedCornerShape(22.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            IconBadge(row.icon, row.color, Modifier.size(12.dp))
            Spacer(Modifier.width(4.dp))
            Column(Modifier.weight(1f)) {
                Text(row.title, style = MaterialTheme.typography.caption1, color = Color.White, fontWeight = FontWeight.Bold, maxLines = 2)
                Text(row.hint, style = MaterialTheme.typography.caption2, color = Color(0xFF8B99A5), maxLines = 2)
            }
            Spacer(Modifier.width(3.dp))
            Text(row.value, style = MaterialTheme.typography.caption1, fontWeight = FontWeight.Bold, color = Color(0xFFE6EDF3), maxLines = 2, textAlign = TextAlign.End)
        }
    }
    selectedInfo?.let { info ->
        WearMetricInfoDialog(title = row.title, description = info, onDismiss = { selectedInfo = null })
    }
}

@Composable
internal fun WearMetricInfoDialog(title: String, description: String, onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            onClick = {},
            modifier = Modifier.fillMaxWidth(0.94f),
            backgroundPainter = androidx.wear.compose.material.CardDefaults.cardBackgroundPainter(startBackgroundColor = Color(0xFF101820), endBackgroundColor = Color(0xFF19232D)),
            shape = RoundedCornerShape(22.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 10.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(title, style = MaterialTheme.typography.caption1, color = Color.White, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, maxLines = 2)
                Spacer(Modifier.height(6.dp))
                Text(
                    description,
                    modifier = Modifier.fillMaxWidth().height(96.dp).verticalScroll(rememberScrollState()),
                    style = MaterialTheme.typography.caption2,
                    color = Color(0xFFC7D0D9),
                    textAlign = TextAlign.Start
                )
                Spacer(Modifier.height(8.dp))
                androidx.wear.compose.material.Button(onClick = onDismiss, modifier = Modifier.fillMaxWidth(0.72f)) {
                    Text("Dismiss", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
internal fun IconBadge(text: String, accent: Color, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(4.dp))
            .background(Brush.radialGradient(listOf(accent, accent.copy(alpha = 0.35f))))
            .padding(1.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = Color.White,
            fontWeight = FontWeight.Black,
            fontSize = 8.sp,
            lineHeight = 8.sp,
            maxLines = 1
        )
    }
}

@Composable
internal fun RunningDot(color: Color) {
    val transition = rememberInfiniteTransition(label = "wear-running")
    val alpha by transition.animateFloat(0.35f, 1f, infiniteRepeatable(animation = tween(650), repeatMode = RepeatMode.Reverse), label = "wear-running-alpha")
    Box(modifier = Modifier.size(10.dp).clip(CircleShape).alpha(alpha).background(color))
}


@Preview(name = "Wear Header Running", showBackground = true, widthDp = 220, heightDp = 220)
@Composable
fun WearComponentsHeaderRunningPreview() {
    MaterialTheme(colors = MaterialTheme.colors.copy(background = Color.Black, surface = Color(0xFF101820), primary = Color(0xFF4DA3FF))) {
        Box(modifier = Modifier.fillMaxSize().background(Color.Black), contentAlignment = Alignment.Center) {
            WearHeader(battery = "86%", subtitle = "Collected 145 • Sent 120", isRunning = true)
        }
    }
}

@Preview(name = "Wear Header Stopped", showBackground = true, widthDp = 220, heightDp = 220)
@Composable
fun WearComponentsHeaderStoppedPreview() {
    MaterialTheme(colors = MaterialTheme.colors.copy(background = Color.Black, surface = Color(0xFF101820), primary = Color(0xFF4DA3FF))) {
        Box(modifier = Modifier.fillMaxSize().background(Color.Black), contentAlignment = Alignment.Center) {
            WearHeader(battery = "--", subtitle = "Stopped", isRunning = false)
        }
    }
}

@Preview(name = "Wear Metric Card", showBackground = true, widthDp = 220, heightDp = 120)
@Composable
fun WearComponentsMetricCardPreview() {
    MaterialTheme(colors = MaterialTheme.colors.copy(background = Color.Black, surface = Color(0xFF101820), primary = Color(0xFF4DA3FF))) {
        Box(modifier = Modifier.fillMaxSize().background(Color.Black), contentAlignment = Alignment.Center) {
            WearMetricCard(WearMetric("Memory", "7 MB", "4 processors running", "▥", Color(0xFF7E46E8)))
        }
    }
}


@Preview(name = "Wear Metric Info Card", showBackground = true, widthDp = 220, heightDp = 120)
@Composable
fun WearComponentsMetricInfoCardPreview() {
    MaterialTheme(colors = MaterialTheme.colors.copy(background = Color.Black, surface = Color(0xFF101820), primary = Color(0xFF4DA3FF))) {
        Box(modifier = Modifier.fillMaxSize().background(Color.Black), contentAlignment = Alignment.Center) {
            WearMetricCard(WearMetric("CPU", "info", "Tap for details", "▣", Color(0xFF2F8DFF), "cat: /proc/loadavg: Permission denied"))
        }
    }
}

@Preview(name = "Wear Stopped Card", showBackground = true, widthDp = 220, heightDp = 160)
@Composable
fun WearComponentsStoppedCardPreview() {
    MaterialTheme(colors = MaterialTheme.colors.copy(background = Color.Black, surface = Color(0xFF101820), primary = Color(0xFF4DA3FF))) {
        Box(modifier = Modifier.fillMaxSize().background(Color.Black), contentAlignment = Alignment.Center) {
            WearStoppedCard()
        }
    }
}


@Preview(name = "Wear Collection Counters", showBackground = true, widthDp = 220, heightDp = 160)
@Composable
fun WearComponentsCollectionCountersPreview() {
    MaterialTheme(colors = MaterialTheme.colors.copy(background = Color.Black, surface = Color(0xFF101820), primary = Color(0xFF4DA3FF))) {
        Box(modifier = Modifier.fillMaxSize().background(Color.Black), contentAlignment = Alignment.Center) {
            WearCollectionCounters(collected = 145, collectedDataSizeBytes = 284_672, sent = 120, failed = 2)
        }
    }
}
