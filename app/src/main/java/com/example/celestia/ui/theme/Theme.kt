package com.example.celestia.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/**
 * Celestia's **dark mode color palette**.
 *
 * Used when:
 * - The system is in dark mode
 * - OR the user manually enables dark mode inside Settings
 *
 * This palette emphasizes deep blues/purples with high contrast text,
 * matching the “cosmic dashboard” feel of the app.
 */
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

/**
 * Celestia's **light mode color palette**.
 *
 * A soft, bright interface for use in daylight conditions.
 * Light mode retains the same brand accents as dark mode,
 * but uses lighter surfaces and darker text for readability.
 */
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

/**
 * Root theme wrapper for the Celestia app.
 *
 * This setup handles:
 * - **Dark vs Light theme selection**
 * - **Dynamic text-size scaling** (Accessibility → Small, Medium, Large)
 * - **Material3 theming** with custom typography + color palettes
 *
 * @param darkTheme Whether to use dark mode.
 * Defaults to `isSystemInDarkTheme()` but can be overridden by user settings.
 *
 * @param textSize An integer index:
 * ```
 * 0 = Small text
 * 1 = Medium (default)
 * 2 = Large text
 * ```
 *
 * @param content Composable content that should inherit Celestia’s theme.
 */
@Composable
fun CelestiaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    textSize: Int = 1,
    content: @Composable () -> Unit
) {
    // Apply either light or dark palette
    val colors = if (darkTheme) DarkColorScheme else LightColorScheme

    // Text scaling logic
    val scale = when (textSize) {
        0 -> 0.90f  // Small
        1 -> 1.20f  // Medium (default experience)
        2 -> 1.45f  // Large — accessibility optimized
        else -> 1.00f
    }

    /**
     * Typography scaling is applied proportionally to *all*
     * Material3 text tokens used across the app.
     */
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

    // Apply theme to the entire app
    MaterialTheme(
        colorScheme = colors,
        typography = scaledTypography,
        content = content
    )
}
