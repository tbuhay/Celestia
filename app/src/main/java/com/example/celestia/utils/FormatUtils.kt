package com.example.celestia.utils

import kotlin.math.abs
import kotlin.math.roundToInt

object FormatUtils {

    // -------------------------------------------------------------------------
    //  SMART PRECISION NUMBER FORMATTER (Core helper)
    // -------------------------------------------------------------------------
    /**
     * Returns a string:
     *  - Whole numbers → "123"
     *  - Decimals → "123.4"
     *  - Always comma-separated
     *  - Never trailing zeros
     */
    fun formatNumber(value: Double): String {
        if (value.isNaN()) return "N/A"

        // Whole number?
        val whole = value.roundToInt().toDouble()
        if (value == whole) {
            return withCommas(whole.toInt().toString())
        }

        // Otherwise 1 decimal max
        val oneDecimal = String.format("%.1f", value)
        // Remove ".0" if it occurs
        return withCommas(oneDecimal.replace(".0", ""))
    }

    private fun withCommas(input: String): String {
        return try {
            val parts = input.split(".")
            val intPart = parts[0]
            val withCommas = intPart.reversed()
                .chunked(3)
                .joinToString(",")
                .reversed()

            if (parts.size == 1) withCommas
            else "$withCommas.${parts[1]}"
        } catch (e: Exception) {
            input
        }
    }

    // -------------------------------------------------------------------------
    //  COORDINATE FORMATTING (Option A)
    // -------------------------------------------------------------------------
    /**
     * Converts:
     *  lat: 49.8951, lon: -97.1384
     * Into:
     *  "49.8951° N, 97.1384° W"
     */
    fun formatCoordinates(lat: Double, lon: Double): String {
        return "${formatSingleCoord(lat, 'N', 'S')}, ${formatSingleCoord(lon, 'E', 'W')}"
    }

    private fun formatSingleCoord(value: Double, positive: Char, negative: Char): String {
        val hemi = if (value >= 0) positive else negative
        val absValue = abs(value)
        val formatted = String.format("%.4f", absValue)
        return "$formatted° $hemi"
    }

    // -------------------------------------------------------------------------
    //  ALTITUDE (km)
    // -------------------------------------------------------------------------
    fun formatAltitude(km: Double): String {
        return "${formatNumber(km)} km"
    }

    // -------------------------------------------------------------------------
    //  VELOCITY (km/h)
    // -------------------------------------------------------------------------
    fun formatVelocity(kmh: Double): String {
        return "${formatNumber(kmh)} km/h"
    }

    // -------------------------------------------------------------------------
    //  DISTANCE (km)
    // -------------------------------------------------------------------------
    fun formatDistance(km: Double): String {
        return "${formatNumber(km)} km"
    }

    // -------------------------------------------------------------------------
    //  PERCENT / ILLUMINATION
    // -------------------------------------------------------------------------
    fun formatPercent(raw: String?): String {
        if (raw.isNullOrBlank()) return "N/A"

        // Remove %, convert to double, abs() for waning cases
        val clean = raw.replace("%", "").trim()
        val value = clean.toDoubleOrNull() ?: return "N/A"
        return "${formatNumber(abs(value))}%"
    }

    fun formatPercent(raw: Double): String {
        return "${formatNumber(abs(raw))}%"
    }

    // -------------------------------------------------------------------------
    //  MOON AGE (days)
    // -------------------------------------------------------------------------
    fun formatMoonAge(days: Double): String {
        val num = formatNumber(days)
        return "$num days"
    }
}
