package com.example.celestia.utils

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
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

    fun formatTime(timestamp: Long, use24h: Boolean): String {
        val pattern = if (use24h) "HH:mm" else "h:mm a"
        val sdf = SimpleDateFormat(pattern, Locale.getDefault())
        return sdf.format(Date(timestamp))
    }

    fun convertTimeFormat(input: String, use24h: Boolean): String {
        val possibleFormats = listOf(
            "MMM dd, HH:mm",            // Already formatted 24h
            "MMM dd, h:mm a",           // Already formatted 12h
            "yyyy-MM-dd'T'HH:mm:ss",    // NOAA + ISO without Z
            "yyyy-MM-dd HH:mm:ss",      // fallback
            "yyyy-MM-dd"                // date only
        )

        val parsedDate = possibleFormats.firstNotNullOfOrNull { pattern ->
            try {
                val sdf = java.text.SimpleDateFormat(pattern, java.util.Locale.US)
                sdf.parse(input)
            } catch (e: Exception) {
                null
            }
        } ?: return input  // fallback

        val outputPattern = if (use24h)
            "MMM dd, HH:mm"
        else
            "MMM dd, h:mm a"

        val outputFormat = java.text.SimpleDateFormat(outputPattern, java.util.Locale.US)
        return outputFormat.format(parsedDate)
    }

    // -------------------------------------------------------------------------
    //  COORDINATE FORMATTING
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
    fun convertLunarTime(input: String, use24h: Boolean): String {
        if (input.isBlank() || input == "N/A") return "N/A"

        return try {
            // API format is "HH:mm:ss.xxx"
            val parts = input.split(":")
            if (parts.size < 2) return input

            val hour = parts[0].toInt()
            val minute = parts[1].toInt()

            if (use24h) {
                String.format("%02d:%02d", hour, minute)
            } else {
                // Convert to 12-hour
                val suffix = if (hour >= 12) "PM" else "AM"
                val hour12 = when {
                    hour == 0 -> 12
                    hour > 12 -> hour - 12
                    else -> hour
                }
                String.format("%d:%02d %s", hour12, minute, suffix)
            }
        } catch (e: Exception) {
            input
        }
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
