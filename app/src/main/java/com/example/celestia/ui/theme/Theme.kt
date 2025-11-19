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
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colors,
        typography = CelestiaTypography,
        content = content
    )
}
