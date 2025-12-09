package com.example.celestia.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.celestia.R

/**
 * ---------------------------------------------------------------------------
 * Celestia Typography System
 * ---------------------------------------------------------------------------
 *
 * Defines the full text hierarchy for the Celestia app, including:
 * - Screen titles
 * - Section/card titles
 * - Metric values (Kp, ISS velocity, lunar illumination, etc.)
 * - Body text
 * - Labels & metadata
 *
 * The system uses two font families:
 *
 * **Inter** — for headings, titles, and emphasized text
 * **Roboto** — for body paragraphs and supporting labels
 *
 * All sizes were tuned specifically for:
 * - High readability over dark space-themed surfaces
 * - Clean UI presentation on small and large Android screens
 * - Compatibility with the dynamic text scaling system found in `CelestiaTheme`
 */

// ---------------------------------------------------------------------------
// Font Families
// ---------------------------------------------------------------------------

/**
 * Inter Variable — primary display font for Celestia.
 * Used for titles, headings, KP/ISS values, and important content.
 */
val Inter = FontFamily(
    Font(R.font.inter_variable)
)

/**
 * Roboto — default body font.
 * Chosen for excellent readability and Material compatibility.
 */
val Roboto = FontFamily.Default


// ---------------------------------------------------------------------------
// Celestia Typography Tokens
// ---------------------------------------------------------------------------

/**
 * The main typography configuration for all Material components.
 *
 * These values define the baseline text sizes before accessibility scaling
 * is applied via `CelestiaTheme(textSize = ...)`.
 */
val CelestiaTypography = Typography(

    /* -------------------------------------------------
     * SCREEN TITLES (Large, bold — 32sp)
     * Used in: Home top bar title, screen headers
     * ------------------------------------------------- */
    headlineLarge = TextStyle(
        fontFamily = Inter,
        fontWeight = FontWeight.Medium,
        fontSize = 32.sp,
        lineHeight = 38.sp
    ),

    /* -------------------------------------------------
     * SECTION & CARD TITLES (18–22sp)
     * Used in: cards like ISS, Kp Index, Asteroid, Lunar
     * ------------------------------------------------- */
    titleLarge = TextStyle(
        fontFamily = Inter,
        fontWeight = FontWeight.SemiBold,
        fontSize = 22.sp,
        lineHeight = 28.sp
    ),

    titleMedium = TextStyle(
        fontFamily = Inter,
        fontWeight = FontWeight.Medium,
        fontSize = 18.sp,
        lineHeight = 24.sp
    ),

    /* -------------------------------------------------
     * METRIC VALUES (24–26sp)
     * Used for: Kp index value, ISS altitude/velocity,
     *           asteroid distances, moon illumination %, etc.
     * ------------------------------------------------- */
    displayMedium = TextStyle(
        fontFamily = Inter,
        fontWeight = FontWeight.Bold,
        fontSize = 26.sp,
        lineHeight = 30.sp
    ),

    /* -------------------------------------------------
     * BODY TEXT (14–16sp)
     * Used for descriptions, paragraphs, and explanatory text.
     * ------------------------------------------------- */
    bodyLarge = TextStyle(
        fontFamily = Roboto,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 22.sp
    ),

    bodyMedium = TextStyle(
        fontFamily = Roboto,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp
    ),

    bodySmall = TextStyle(
        fontFamily = Roboto,
        fontWeight = FontWeight.Light,
        fontSize = 13.sp,
        lineHeight = 18.sp
    ),

    /* -------------------------------------------------
     * LABELS (metadata, timestamps, subtext)
     * ------------------------------------------------- */
    labelSmall = TextStyle(
        fontFamily = Roboto,
        fontWeight = FontWeight.Light,
        fontSize = 12.sp,
        lineHeight = 16.sp
    )
)
