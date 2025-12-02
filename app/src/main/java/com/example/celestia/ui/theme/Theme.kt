package com.example.celestia.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = CelestiaBlue,
    secondary = CelestiaPurple,
    tertiary = CelestiaOrange,
    background = DeepSpace,
    surface = DarkSurface,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
)

private val LightColorScheme = lightColorScheme(
    primary = CelestiaBlue,
    secondary = CelestiaPurple,
    tertiary = CelestiaOrange,

    background = Color(0xFFF7F8FB),
    surface = Color.White,

    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,

    onBackground = Color(0xFF0D0D10),
    onSurface = Color(0xFF0D0D10)
)

@Composable
fun CelestiaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    textSize: Int = 1,
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) DarkColorScheme else LightColorScheme

    val scale = when (textSize) {
        0 -> 0.90f  // Small
        1 -> 1.20f  // Medium (default)
        2 -> 1.45f  // Large
        else -> 1.00f
    }

    val scaledTypography = CelestiaTypography.copy(
        bodySmall  = MaterialTheme.typography.bodySmall.copy(fontSize = MaterialTheme.typography.bodySmall.fontSize * scale),
        bodyMedium = MaterialTheme.typography.bodyMedium.copy(fontSize = MaterialTheme.typography.bodyMedium.fontSize * scale),
        bodyLarge  = MaterialTheme.typography.bodyLarge.copy(fontSize = MaterialTheme.typography.bodyLarge.fontSize * scale),
        titleSmall = MaterialTheme.typography.titleSmall.copy(fontSize = MaterialTheme.typography.titleSmall.fontSize * scale),
        titleMedium= MaterialTheme.typography.titleMedium.copy(fontSize = MaterialTheme.typography.titleMedium.fontSize * scale),
        titleLarge = MaterialTheme.typography.titleLarge.copy(fontSize = MaterialTheme.typography.titleLarge.fontSize * scale),
        labelSmall = MaterialTheme.typography.labelSmall.copy(fontSize = MaterialTheme.typography.labelSmall.fontSize * scale),
        labelMedium= MaterialTheme.typography.labelMedium.copy(fontSize = MaterialTheme.typography.labelMedium.fontSize * scale),
        labelLarge = MaterialTheme.typography.labelLarge.copy(fontSize = MaterialTheme.typography.labelLarge.fontSize * scale),
    )
    MaterialTheme(
        colorScheme = colors,
        typography = scaledTypography,
        content = content
    )
}
