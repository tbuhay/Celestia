package com.example.celestia.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * ---------------------------------------------------------------------------
 * Celestia Brand Color Palette
 * ---------------------------------------------------------------------------
 *
 * This file defines all globally used colors in the Celestia design system.
 * Each color is purpose-assigned and referenced consistently across screens
 * (Kp Index, ISS, Lunar Phases, Asteroid Tracking, etc.).
 *
 * These values serve as the foundation for:
 * - Custom card accents
 * - Theming patterns
 * - Status indicators
 * - Gradient backgrounds
 *
 * All colors follow the Celestia "cosmic data dashboard" visual identity.
 */

// ---------------------------------------------------------------------------
// Core Brand Colors
// ---------------------------------------------------------------------------

/** Primary Celestia blue for accents, buttons, and emphasis elements. */
val CelestiaBlue = Color(0xFF4C8CFF)

/** Purple accent used primarily for Kp Index and cosmic visuals. */
val CelestiaPurple = Color(0xFFA43FE9)

/** Orange highlight for asteroid danger levels and NEO UI elements. */
val CelestiaOrange = Color(0xFFFF7A00)

/** Teal accent used for ISS visuals, orbit markers, and secondary highlights. */
val CelestiaGreen = Color(0xFF207E30)

/** Sky-blue mid-tone used for gradients, headings, and ISS cards. */
val CelestiaSkyBlue = Color(0xFF1E88E5)

/** Gold/yellow accent used for lunar highlights and illumination percentages. */
val CelestiaYellow = Color(0xFFDCB30C)


// ---------------------------------------------------------------------------
// Dark Theme Surfaces (Celestia Space Theme)
// ---------------------------------------------------------------------------

/** Deep cosmic background color used across dark mode surfaces. */
val DeepSpace = Color(0xFF0B1221)

/** Elevated surface color used for cards and containers in dark mode. */
val DarkSurface = Color(0xFF1B2236)

/** Primary readable text color for dark mode (slightly cool off-white). */
val TextPrimary = Color(0xFFE6E9F0)



// ---------------------------------------------------------------------------
// Hazard Colors (Asteroid Tracking & Alerts)
// ---------------------------------------------------------------------------

/** Strong hazard red used for NEO threats, warnings, and delete actions. */
val CelestiaHazardRed = Color(0xFFD32F2F)
