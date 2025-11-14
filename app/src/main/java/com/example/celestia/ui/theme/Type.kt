package com.example.celestia.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.celestia.R

// Custom font families
val Inter = FontFamily(
    Font(R.font.inter_variable)
)

val Roboto = FontFamily.Default

// -----------------------------------------------------
// Celestia Typography System (Finalized)
// -----------------------------------------------------
val CelestiaTypography = Typography(

    /* -------------------------------------------------
     * SCREEN TITLES (Large, bold — your 32.sp style)
     * ------------------------------------------------- */
    headlineLarge = TextStyle(
        fontFamily = Inter,
        fontWeight = FontWeight.Medium,
        fontSize = 32.sp,
        lineHeight = 38.sp
    ),

    /* -------------------------------------------------
     * SECTION & CARD TITLES (your 18–20.sp style)
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
     * METRIC VALUES (your KP/ISS value style)
     * for large numbers: 24–26.sp
     * ------------------------------------------------- */
    displayMedium = TextStyle(
        fontFamily = Inter,
        fontWeight = FontWeight.Bold,
        fontSize = 26.sp,
        lineHeight = 30.sp
    ),

    /* -------------------------------------------------
     * BODY / DESCRIPTION TEXT (14.sp)
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
     * LABELS (small metadata)
     * ------------------------------------------------- */
    labelSmall = TextStyle(
        fontFamily = Roboto,
        fontWeight = FontWeight.Light,
        fontSize = 12.sp,
        lineHeight = 16.sp
    )
)
