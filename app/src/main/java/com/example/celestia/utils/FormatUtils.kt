package com.example.celestia.utils

import androidx.compose.ui.graphics.Color
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * Utility object for formatting numbers, timestamps, coordinates, and various
 * presentation values used throughout the Celestia app.
 *
 * All logic here is display-focused and intentionally contains no behavior changes.
 */
object FormatUtils {

    // === Internal 12-Hour Formatter (a.m. / p.m.) ===

    /**
     * Formats a Date into a custom 12-hour time format using "a.m." / "p.m." suffixes.
     *
     * @param date The Date to format.
     * @return A formatted time string such as "3:45 p.m.".
     */
    private fun format12HourWithDots(date: Date): String {
        val cal = Calendar.getInstance()
        cal.time = date

        val hour24 = cal.get(Calendar.HOUR_OF_DAY)
        val minute = cal.get(Calendar.MINUTE)

        val isPM = hour24 >= 12
        val hour12 = when {
            hour24 == 0 -> 12
            hour24 > 12 -> hour24 - 12
            else -> hour24
        }

        val suffix = if (isPM) "p.m." else "a.m."
        return String.format("%d:%02d %s", hour12, minute, suffix)
    }

    // === Smart Precision Number Formatter ===

    /**
     * Formats a number using intelligent precision rules:
     * - Whole numbers are shown with commas only (e.g., 1,200)
     * - Non-whole numbers may use one decimal if needed
     *
     * @param value The numeric value to format.
     * @return A clean, human-readable number string.
     */
    fun formatNumber(value: Double): String {
        if (value.isNaN()) return "N/A"

        val whole = value.roundToInt().toDouble()
        if (value == whole) {
            return withCommas(whole.toInt().toString())
        }

        val oneDecimal = String.format("%.1f", value)
        return withCommas(oneDecimal.replace(".0", ""))
    }

    /**
     * Adds comma separators to integer or decimal strings.
     *
     * @param input The numeric string.
     * @return A properly formatted string with commas or the original input on failure.
     */
    private fun withCommas(input: String): String {
        return try {
            val parts = input.split(".")
            val intPart = parts[0]
            val withCommas = intPart
                .reversed()
                .chunked(3)
                .joinToString(",")
                .reversed()

            if (parts.size == 1) withCommas else "$withCommas.${parts[1]}"
        } catch (e: Exception) {
            input
        }
    }

    // === Time Formatting ===

    /**
     * Formats a timestamp into 24-hour or 12-hour time format based on user preference.
     *
     * @param timestamp Milliseconds since epoch.
     * @param use24h Whether 24-hour format is preferred.
     * @return A formatted time string.
     */
    fun formatTime(timestamp: Long, use24h: Boolean): String {
        val date = Date(timestamp)
        return if (use24h) {
            SimpleDateFormat("HH:mm", Locale.getDefault()).format(date)
        } else {
            format12HourWithDots(date)
        }
    }

    // === Updated Timestamp (Used in ISS + Kp Index) ===

    /**
     * Formats "last updated" timestamps as:
     * - "MMM d, HH:mm" (24-hour)
     * - "MMM d, h:mm a.m./p.m." (12-hour)
     *
     * @param rawMillis Timestamp in milliseconds as a string.
     * @param use24h Whether the user prefers 24-hour time.
     * @return A formatted timestamp or "Unknown".
     */
    fun formatUpdatedTimestamp(rawMillis: String?, use24h: Boolean): String {
        if (rawMillis.isNullOrBlank()) return "Unknown"

        return try {
            val millis = rawMillis.toLong()
            val date = Date(millis)

            if (use24h) {
                SimpleDateFormat("MMM d, HH:mm", Locale.getDefault()).format(date)
            } else {
                val day = SimpleDateFormat("MMM d,", Locale.getDefault()).format(date)
                "$day ${format12HourWithDots(date)}"
            }
        } catch (e: Exception) {
            "Unknown"
        }
    }

    // === Convert Time Format (Simplified) ===

    /**
     * Attempts to parse an input time string using several fallback formats, then
     * re-renders it in the user’s preferred time format.
     *
     * @param input The time string being converted.
     * @param use24h Whether 24-hour format is preferred.
     * @return A formatted time string or the original input on failure.
     */
    fun convertTimeFormat(input: String, use24h: Boolean): String {
        val formats = listOf(
            "MMM dd, HH:mm",
            "MMM dd, h:mm a",
            "yyyy-MM-dd'T'HH:mm:ss",
            "yyyy-MM-dd HH:mm:ss",
            "yyyy-MM-dd"
        )

        val parsed = formats.firstNotNullOfOrNull { pattern ->
            try {
                SimpleDateFormat(pattern, Locale.US).parse(input)
            } catch (e: Exception) {
                null
            }
        } ?: return input

        return if (use24h) {
            SimpleDateFormat("MMM dd, HH:mm", Locale.US).format(parsed)
        } else {
            val day = SimpleDateFormat("MMM dd,", Locale.US).format(parsed)
            "$day ${format12HourWithDots(parsed)}"
        }
    }

    // === Coordinates, Altitude, Velocity, Percent, etc. ===

    /**
     * Formats latitude and longitude into a readable compass format.
     *
     * @param lat Latitude in decimal degrees.
     * @param lon Longitude in decimal degrees.
     * @return A formatted coordinate string (e.g., "49.1234° N, 97.1234° W").
     */
    fun formatCoordinates(lat: Double, lon: Double): String {
        return "${formatSingleCoord(lat, 'N', 'S')}, ${formatSingleCoord(lon, 'E', 'W')}"
    }

    /**
     * Formats a single coordinate value into degrees + hemisphere suffix.
     */
    private fun formatSingleCoord(value: Double, positive: Char, negative: Char): String {
        val hemi = if (value >= 0) positive else negative
        val absValue = abs(value)
        val formatted = String.format("%.4f", absValue)
        return "$formatted° $hemi"
    }

    /** Formats altitude in kilometers. */
    fun formatAltitude(km: Double): String = "${formatNumber(km)} km"

    /** Formats speed in km/h. */
    fun formatVelocity(kmh: Double): String = "${formatNumber(kmh)} km/h"

    /** Formats distance in kilometers. */
    fun formatDistance(km: Double): String = "${formatNumber(km)} km"

    /** Formats a percentage (absolute value). */
    fun formatPercent(raw: Double): String = "${formatNumber(abs(raw))}%"

    /** Formats lunar age (days). */
    fun formatMoonAge(days: Double): String = "${formatNumber(days)} days"

    // === Lunar Time Conversion ===

    /**
     * Converts simple "HH:mm" style lunar times into 12-hour or 24-hour format.
     *
     * @param input The time string (e.g., "14:30").
     * @param use24h Whether the user prefers 24-hour format.
     * @return A converted time string or "N/A".
     */
    fun convertLunarTime(input: String, use24h: Boolean): String {
        if (input.isBlank() || input == "N/A") return "N/A"

        return try {
            val parts = input.split(":")
            if (parts.size < 2) return input

            val hour = parts[0].toInt()
            val minute = parts[1].toInt()

            if (use24h) {
                String.format("%02d:%02d", hour, minute)
            } else {
                val suffix = if (hour >= 12) "p.m." else "a.m."
                val hour12 = when {
                    hour == 0 -> 12
                    hour > 12 -> hour - 12
                    else -> hour
                }
                "$hour12:${String.format("%02d", minute)} $suffix"
            }
        } catch (e: Exception) {
            input
        }
    }

    /**
     * Returns NOAA-style Kp category, color, and a short description
     * for the CURRENT Kp reading.
     */
    fun getNoaaKpInfo(kp: Double): Triple<String, Color, String> {
        val (status, color) = getNoaaKpStatusAndColor(kp)

        val description = when {
            kp >= 8 -> "Extreme geomagnetic storm conditions. Significant effects likely."
            kp >= 7 -> "Severe geomagnetic storm. Strong auroral activity possible."
            kp >= 6 -> "Strong geomagnetic storm. Aurora visible at lower latitudes."
            kp >= 5 -> "Moderate storm activity. Aurora possible in mid-latitudes."
            kp >= 4 -> "Disturbed geomagnetic conditions."
            kp >= 2 -> "Elevated solar wind influence."
            else    -> "Calm geomagnetic conditions."
        }

        return Triple(status, color, description)
    }

    /**
     * NOAA-like status + color mapping for a given Kp value.
     *
     * 0–1 Quiet (green)
     * 2–3 Unsettled (yellow-green)
     * 4 Active (yellow/orange)
     * 5 Minor Storm (orange)
     * 6 Major Storm (red-orange)
     * 7–9 Severe/Extreme Storm (deep red)
     */
    fun getNoaaKpStatusAndColor(kp: Double): Pair<String, Color> {
        return when {
            kp >= 8 -> "Extreme Geomagnetic Storm" to Color(0xFF8B0000)  // deep red
            kp >= 7 -> "Severe Storm"              to Color(0xFFB71C1C)  // strong red
            kp >= 6 -> "Strong Storm"              to Color(0xFFE53935)  // red-orange
            kp >= 5 -> "Moderate Storm"            to Color(0xFFFB8C00)  // orange
            kp >= 4 -> "Disturbed"                 to Color(0xFFFFC107)  // amber
            kp >= 2 -> "Elevated"                  to Color(0xFFCDDC39)  // yellow-green
            else    -> "Calm"                      to Color(0xFF4CAF50)  // green
        }
    }
}
