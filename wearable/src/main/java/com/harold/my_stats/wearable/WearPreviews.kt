package com.harold.my_stats.wearable

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.items
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.ButtonDefaults
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Scaffold
import androidx.wear.compose.material.Text

internal fun previewWearState(): WearState = WearState(
    battery = "86%",
    subtitle = "Pixel Watch Preview",
    rows = listOf(
        WearMetric("Battery", "86%", "Discharging", "▯", Color(0xFF67E08E)),
        WearMetric("Temp", "31.4°", "Battery sensor", "℃", Color(0xFFFFC107)),
        WearMetric("Drain", "2.4%", "Per hour", "↘", Color(0xFFFFA726)),
        WearMetric("CPU", "1.32", "8 processors", "▣", Color(0xFF2F8DFF)),
        WearMetric("Memory", "171 MB", "JVM used", "≡", Color(0xFF00B8A9)),
        WearMetric("Screen", "Awake", "Interactive", "◐", Color(0xFFB36BFF))
    )
)

@Composable
internal fun WearPreviewSurface(content: @Composable () -> Unit) {
    MaterialTheme(colors = MaterialTheme.colors.copy(background = Color.Black, surface = Color(0xFF101820), primary = Color(0xFF4DA3FF))) {
        content()
    }
}

@Composable
internal fun WearPreviewScreen(state: WearState, isRunning: Boolean) {
    WearPreviewSurface {
        Scaffold {
            ScalingLazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
                    .padding(horizontal = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                item { WearHeader(if (isRunning) state.battery else "--", if (isRunning) state.subtitle else "Stopped", isRunning) }
                item {
                    Button(
                        onClick = {},
                        colors = ButtonDefaults.buttonColors(backgroundColor = if (isRunning) Color(0xFF3A151B) else Color(0xFF4DA3FF)),
                        modifier = Modifier.fillMaxWidth(0.78f)
                    ) {
                        Text(if (isRunning) "Stop Service" else "Start Service", color = if (isRunning) Color(0xFFFF6B75) else Color.White, fontWeight = FontWeight.Bold)
                    }
                }
                if (isRunning) {
                    items(state.rows) { row -> WearMetricCard(row) }
                } else {
                    item { WearStoppedCard() }
                }
                item { Spacer(Modifier.height(20.dp)) }
            }
        }
    }
}

@Preview(name = "Wear Running", showBackground = true, widthDp = 220, heightDp = 220)
@Composable
fun WearRunningPreview() {
    WearPreviewScreen(state = previewWearState(), isRunning = true)
}

@Preview(name = "Wear Stopped", showBackground = true, widthDp = 220, heightDp = 220)
@Composable
fun WearStoppedPreview() {
    WearPreviewScreen(state = previewWearState(), isRunning = false)
}

@Preview(name = "Wear Header", showBackground = true, widthDp = 220, heightDp = 120)
@Composable
fun WearHeaderPreview() {
    WearPreviewSurface {
        WearHeader(battery = "86%", subtitle = "Pixel Watch Preview", isRunning = true)
    }
}

@Preview(name = "Wear Metric Card", showBackground = true, widthDp = 220, heightDp = 120)
@Composable
fun WearMetricCardPreview() {
    WearPreviewSurface {
        WearMetricCard(WearMetric("Battery", "86%", "Discharging", "▯", Color(0xFF67E08E)))
    }
}
