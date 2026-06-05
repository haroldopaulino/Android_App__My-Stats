package com.harold.my_stats.phone

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
internal fun AppHeader(
    model: String,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 68.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(end = 12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = "My Stats",
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Black,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = model.ifBlank { "Android Phone" },
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        SettingsHeaderButton(onClick = onOpenSettings)
    }
}

@Composable
internal fun SettingsHeaderButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        modifier = modifier.size(44.dp),
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        contentColor = MaterialTheme.colorScheme.onSurface,
        shadowElevation = 0.dp
    ) {
        CogIcon(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun CogIcon(
    modifier: Modifier = Modifier,
    color: Color = Color.White
) {
    Canvas(modifier = modifier) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val stroke = 2.4.dp.toPx()
        val outerRadius = size.minDimension * 0.43f
        val rootRadius = size.minDimension * 0.34f
        val path = Path()
        val points = 32

        for (index in 0 until points) {
            val phase = index % 4
            val radius = if (phase == 0 || phase == 1) outerRadius else rootRadius
            val angle = -PI.toFloat() / 2f + (PI.toFloat() * 2f * index / points)
            val point = Offset(
                x = center.x + cos(angle) * radius,
                y = center.y + sin(angle) * radius
            )
            if (index == 0) {
                path.moveTo(point.x, point.y)
            } else {
                path.lineTo(point.x, point.y)
            }
        }

        path.close()

        drawPath(
            path = path,
            color = color,
            style = Stroke(
                width = stroke,
                cap = StrokeCap.Round,
                join = StrokeJoin.Round
            )
        )
        drawCircle(
            color = color,
            radius = size.minDimension * 0.13f,
            center = center,
            style = Stroke(width = stroke, cap = StrokeCap.Round)
        )
    }
}

@Composable
internal fun ServiceHeroCard(
    isRunning: Boolean,
    collectedCount: Long,
    sentEndpointCount: Long,
    failedEndpointCount: Long,
    collectedDataSizeBytes: Long,
    uploadIntervalMs: Long,
    refreshedAt: String,
    onToggleService: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        modifier = modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .background(Brush.linearGradient(listOf(Color(0xFF0B4D36), Color(0xFF10212B))))
                .padding(20.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconBadge("↻", if (isRunning) Color(0xFF67E08E) else Color(0xFF9E9E9E), Modifier.size(58.dp))
                    Spacer(Modifier.width(14.dp))
                    Column(Modifier.weight(1f)) {
                        Text("Collection Service", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Color.White)
                        Text(if (isRunning) "Collecting system data every 10 seconds" else "Stopped — data cards are hidden", color = Color(0xFFD6E6DF), style = MaterialTheme.typography.bodyMedium)
                    }
                    RunningStatusChip(isRunning)
                }
                if (isRunning) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.fillMaxWidth()) {
                        CollectionCounterRow("Total Data Collection:", collectedCount.toString())
                        CollectionCounterRow("Locally Stored Data Size:", formatByteSize(collectedDataSizeBytes))
                        CollectionCounterRow("Total Collections Sent to Endpoint:", sentEndpointCount.toString())
                        CollectionCounterRow("Failed sending to endpoint:", failedEndpointCount.toString())
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                    MiniInfo("Upload interval", uploadIntervalLabel(uploadIntervalMs), Modifier.weight(1f))
                    MiniInfo("Refreshed", refreshedAt, Modifier.weight(1f))
                }
                Button(
                    onClick = onToggleService,
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = if (isRunning) Color(0xFF3A151B) else Color(0xFF4DA3FF), contentColor = if (isRunning) Color(0xFFFF6B75) else Color.White),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (isRunning) {
                        RunningDot(Color(0xFFFF6B75))
                        Spacer(Modifier.width(8.dp))
                    }
                    Text(if (isRunning) "Stop Collection Service" else "Start Collection Service", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
internal fun CollectionCounterRow(label: String, value: String, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0x552A3D4D))
            .padding(horizontal = 12.dp, vertical = 5.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, color = Color(0xFFB7C8DA), style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
        Text(value, color = Color.White, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
internal fun MetricGrid(metrics: List<MetricSpec>) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        metrics.chunked(2).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                row.forEach { metric -> DashboardMetricCard(metric, Modifier.weight(1f)) }
                if (row.size == 1) Spacer(Modifier.weight(1f))
            }
        }
    }
}

@Composable
internal fun DashboardMetricCard(metric: MetricSpec, modifier: Modifier = Modifier) {
    val denied = metric.permissionDeniedCommand != null && isPermissionDenied(metric.permissionDeniedOutput.orEmpty())
    var showDialog by remember { mutableStateOf(false) }
    Card(
        modifier = modifier.height(136.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconBadge(metric.iconText, metric.accent, Modifier.size(46.dp))
                Spacer(Modifier.width(10.dp))
                Text(metric.title, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            if (denied) {
                Text(
                    text = "info",
                    color = MaterialTheme.colorScheme.primary,
                    textDecoration = TextDecoration.Underline,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(Color(0xFF10233A))
                        .clickable { showDialog = true }
                        .padding(horizontal = 12.dp, vertical = 7.dp)
                )
            } else {
                Text(metric.value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            Text(metric.line1, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(metric.line2, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
    if (showDialog && metric.permissionDeniedCommand != null) {
        PermissionDeniedDialog(
            command = metric.permissionDeniedCommand,
            output = metric.permissionDeniedOutput.orEmpty(),
            onDismiss = { showDialog = false }
        )
    }
}

@Composable
internal fun IconBadge(text: String, accent: Color, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(Brush.radialGradient(listOf(accent, accent.copy(alpha = 0.35f))))
            .padding(1.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = Color.White,
            fontWeight = FontWeight.Black,
            fontSize = 34.sp,
            lineHeight = 34.sp,
            maxLines = 1
        )
    }
}

@Composable
internal fun HamburgerIcon(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.onSurface
) {
    CogIcon(modifier = modifier, color = color)
}

@Composable
internal fun MiniInfo(title: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier.clip(RoundedCornerShape(16.dp)).background(Color(0x552A3D4D)).padding(12.dp)) {
        Text(title, color = Color(0xFFB7C8DA), style = MaterialTheme.typography.labelMedium)
        Text(value, color = Color.White, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
internal fun DarkTabs(titles: List<String>, selectedIndex: Int, onSelected: (Int) -> Unit) {
    ScrollableTabRow(selectedTabIndex = selectedIndex, edgePadding = 18.dp, containerColor = MaterialTheme.colorScheme.background, contentColor = MaterialTheme.colorScheme.primary) {
        titles.forEachIndexed { index, title ->
            Tab(selected = selectedIndex == index, onClick = { onSelected(index) }, text = { Text(title, fontWeight = if (selectedIndex == index) FontWeight.Bold else FontWeight.Medium) })
        }
    }
}

private fun previewMetricSpecList(): List<MetricSpec> = listOf(
    MetricSpec(
        title = "Battery",
        value = "86%",
        line1 = "31.4°C · 3942 mV",
        line2 = "Discharging · Healthy",
        iconText = "↯",
        accent = Color(0xFF67E08E)
    ),
    MetricSpec(
        title = "CPU",
        value = "8 cores",
        line1 = "Load 1.32 0.98 0.74",
        line2 = "147 processes",
        iconText = "▣",
        accent = Color(0xFF4DA3FF)
    ),
    MetricSpec(
        title = "Storage",
        value = "39.2 GB",
        line1 = "Free of 120 GB",
        line2 = "Public storage",
        iconText = "◫",
        accent = Color(0xFFFFB74D)
    ),
    MetricSpec(
        title = "Permission",
        value = "Denied",
        line1 = "Tap info for command output",
        line2 = "Requires elevated access",
        iconText = "!",
        accent = Color(0xFFFF6B75),
        permissionDeniedCommand = "dumpsys batterystats",
        permissionDeniedOutput = "Permission Denial: requires android.permission.BATTERY_STATS"
    )
)

@Preview(name = "Phone App Header Running", showBackground = true, widthDp = 390)
@Composable
fun PhoneHomeComponentsAppHeaderRunningPreview() {
    PhoneTheme(isDarkTheme = true) {
        Surface(color = MaterialTheme.colorScheme.background) {
            AppHeader(
                model = "Pixel 9 Pro",
                onOpenSettings = {},
                modifier = Modifier.padding(18.dp)
            )
        }
    }
}

@Preview(name = "Phone App Header Stopped", showBackground = true, widthDp = 390)
@Composable
fun PhoneHomeComponentsAppHeaderStoppedPreview() {
    PhoneTheme(isDarkTheme = true) {
        Surface(color = MaterialTheme.colorScheme.background) {
            AppHeader(
                model = "Android Phone",
                onOpenSettings = {},
                modifier = Modifier.padding(18.dp)
            )
        }
    }
}


@Preview(name = "Phone App Header Light Theme", showBackground = true, widthDp = 390)
@Composable
fun PhoneHomeComponentsAppHeaderLightPreview() {
    PhoneTheme(isDarkTheme = false) {
        Surface(color = MaterialTheme.colorScheme.background) {
            AppHeader(
                model = "Pixel 9 Pro",
                onOpenSettings = {},
                modifier = Modifier.padding(18.dp)
            )
        }
    }
}

@Preview(name = "Phone Settings Header Button Light Theme", showBackground = true, widthDp = 120, heightDp = 120)
@Composable
fun PhoneHomeComponentsSettingsHeaderButtonLightPreview() {
    PhoneTheme(isDarkTheme = false) {
        Box(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            SettingsHeaderButton(onClick = {})
        }
    }
}

@Preview(name = "Phone Settings Header Button", showBackground = true, widthDp = 120, heightDp = 120)
@Composable
fun PhoneHomeComponentsSettingsHeaderButtonPreview() {
    PhoneTheme(isDarkTheme = true) {
        Box(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            SettingsHeaderButton(onClick = {})
        }
    }
}

@Preview(name = "Phone Header And Service Running", showBackground = true, widthDp = 390, heightDp = 330)
@Composable
fun PhoneHomeComponentsHeaderAndServiceRunningPreview() {
    PhoneTheme(isDarkTheme = true) {
        Surface(color = MaterialTheme.colorScheme.background) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                AppHeader(
                    model = "Pixel 9 Pro",
                    onOpenSettings = {},
                    modifier = Modifier.padding(start = 18.dp, top = 18.dp, end = 18.dp, bottom = 8.dp)
                )
                ServiceHeroCard(
                    isRunning = true,
                    collectedCount = 145,
                    sentEndpointCount = 120,
                    failedEndpointCount = 2,
                    collectedDataSizeBytes = 284_672,
                    uploadIntervalMs = 10 * 60 * 1000L,
                    refreshedAt = "08:42 AM",
                    onToggleService = {},
                    modifier = Modifier.padding(horizontal = 18.dp)
                )
            }
        }
    }
}

@Preview(name = "Phone Header And Service Stopped", showBackground = true, widthDp = 390, heightDp = 330)
@Composable
fun PhoneHomeComponentsHeaderAndServiceStoppedPreview() {
    PhoneTheme(isDarkTheme = true) {
        Surface(color = MaterialTheme.colorScheme.background) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                AppHeader(
                    model = "Android Phone",
                    onOpenSettings = {},
                    modifier = Modifier.padding(start = 18.dp, top = 18.dp, end = 18.dp, bottom = 8.dp)
                )
                ServiceHeroCard(
                    isRunning = false,
                    collectedCount = 145,
                    sentEndpointCount = 120,
                    failedEndpointCount = 2,
                    collectedDataSizeBytes = 284_672,
                    uploadIntervalMs = 10 * 60 * 1000L,
                    refreshedAt = "Not running",
                    onToggleService = {},
                    modifier = Modifier.padding(horizontal = 18.dp)
                )
            }
        }
    }
}

@Preview(name = "Phone Metric Grid", showBackground = true, widthDp = 390, heightDp = 360)
@Composable
fun PhoneHomeComponentsMetricGridPreview() {
    PhoneTheme(isDarkTheme = true) {
        Surface(color = MaterialTheme.colorScheme.background) {
            MetricGrid(
                metrics = previewMetricSpecList(),
            )
        }
    }
}

@Preview(name = "Phone Metric Card", showBackground = true, widthDp = 210, heightDp = 180)
@Composable
fun PhoneHomeComponentsMetricCardPreview() {
    PhoneTheme(isDarkTheme = true) {
        Surface(color = MaterialTheme.colorScheme.background) {
            DashboardMetricCard(
                metric = previewMetricSpecList().first(),
                modifier = Modifier.padding(18.dp)
            )
        }
    }
}

@Preview(name = "Phone Permission Metric Card", showBackground = true, widthDp = 210, heightDp = 180)
@Composable
fun PhoneHomeComponentsPermissionMetricCardPreview() {
    PhoneTheme(isDarkTheme = true) {
        Surface(color = MaterialTheme.colorScheme.background) {
            DashboardMetricCard(
                metric = previewMetricSpecList().last(),
                modifier = Modifier.padding(18.dp)
            )
        }
    }
}

@Preview(name = "Phone Icon Badge", showBackground = true, widthDp = 120, heightDp = 120)
@Composable
fun PhoneHomeComponentsIconBadgePreview() {
    PhoneTheme(isDarkTheme = true) {
        Box(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            IconBadge("↯", Color(0xFF67E08E), Modifier.size(58.dp))
        }
    }
}

@Preview(name = "Phone Mini Info", showBackground = true, widthDp = 190)
@Composable
fun PhoneHomeComponentsMiniInfoPreview() {
    PhoneTheme(isDarkTheme = true) {
        Surface(color = MaterialTheme.colorScheme.background) {
            MiniInfo(
                title = "Refreshed",
                value = "08:42 AM",
                modifier = Modifier.padding(18.dp)
            )
        }
    }
}

@Preview(name = "Phone Dark Tabs", showBackground = true, widthDp = 390)
@Composable
fun PhoneHomeComponentsDarkTabsPreview() {
    PhoneTheme(isDarkTheme = true) {
        Surface(color = MaterialTheme.colorScheme.background) {
            DarkTabs(
                titles = listOf("System", "Top Details", "Raw Data"),
                selectedIndex = 0,
                onSelected = {}
            )
        }
    }
}

