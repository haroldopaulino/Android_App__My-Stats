package com.harold.my_stats.wearable

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Preview(name = "Wear Full Running Preview", showBackground = true, widthDp = 220, heightDp = 220)
@Composable
fun WearFullRunningPreview() {
    WearPreviewScreen(state = previewWearState(), isRunning = true)
}

@Preview(name = "Wear Full Stopped Preview", showBackground = true, widthDp = 220, heightDp = 220)
@Composable
fun WearFullStoppedPreview() {
    WearPreviewScreen(state = previewWearState(), isRunning = false)
}

@Preview(name = "Wear Header Running", showBackground = true, widthDp = 220, heightDp = 110)
@Composable
fun WearHeaderRunningPreview() {
    WearPreviewSurface {
        WearHeader(battery = "86%", subtitle = "Pixel Watch Preview", isRunning = true)
    }
}

@Preview(name = "Wear Header Stopped", showBackground = true, widthDp = 220, heightDp = 110)
@Composable
fun WearHeaderStoppedPreview() {
    WearPreviewSurface {
        WearHeader(battery = "--", subtitle = "Stopped", isRunning = false)
    }
}

@Preview(name = "Wear Stopped Card", showBackground = true, widthDp = 220, heightDp = 150)
@Composable
fun WearStoppedCardPreview() {
    WearPreviewSurface {
        WearStoppedCard()
    }
}

@Preview(name = "Wear Battery Metric", showBackground = true, widthDp = 220, heightDp = 130)
@Composable
fun WearBatteryMetricPreview() {
    WearPreviewSurface {
        WearMetricCard(
            WearMetric(
                title = "Memory",
                value = "7 MB",
                hint = "4 processors running",
                icon = "▥",
                color = Color(0xFF7E46E8)
            )
        )
    }
}

@Preview(name = "Wear Icon Badge", showBackground = true, widthDp = 120, heightDp = 120)
@Composable
fun WearIconBadgePreview() {
    WearPreviewSurface {
        IconBadge(
            text = "▯",
            accent = Color(0xFF67E08E),
            modifier = Modifier.padding(54.dp)
        )
    }
}
