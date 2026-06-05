package com.harold.my_stats.phone

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkScheme = darkColorScheme(
    primary = Color(0xFF4DA3FF),
    secondary = Color(0xFFB7C8DA),
    tertiary = Color(0xFF67E08E),
    background = Color(0xFF050A10),
    surface = Color(0xFF0D151D),
    surfaceVariant = Color(0xFF141D26),
    onPrimary = Color(0xFF031625),
    onSecondary = Color(0xFF10202D),
    onBackground = Color(0xFFEAF1F8),
    onSurface = Color(0xFFEAF1F8),
    onSurfaceVariant = Color(0xFFC7D0D9)
)

private val LightScheme = lightColorScheme(
    primary = Color(0xFF0B5EA8),
    secondary = Color(0xFF416174),
    tertiary = Color(0xFF1D6F5D),
    background = Color(0xFFF6F9FC),
    surface = Color(0xFFFFFFFF),
    surfaceVariant = Color(0xFFE7EEF5),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = Color(0xFF121A21),
    onSurface = Color(0xFF121A21),
    onSurfaceVariant = Color(0xFF485762)
)

@Composable
internal fun PhoneTheme(isDarkTheme: Boolean, content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = if (isDarkTheme) DarkScheme else LightScheme, content = content)
}
