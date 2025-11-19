package com.example.celestia.utils

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import kotlin.math.abs
import kotlin.math.roundToInt

object FormatUtils {

    // -------------------------------------------------------------------------
    //  INTERNAL 12-HOUR FORMATTER (a.m. / p.m.)
    // -------------------------------------------------------------------------
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

    // -------------------------------------------------------------------------
    //  SMART PRECISION NUMBER FORMATTER
    // -------------------------------------------------------------------------
    fun formatNumber(value: Double): String {
        if (value.isNaN()) return "N/A"

        val whole = value.roundToInt().toDouble()
        if (value == whole) {
            return withCommas(whole.toInt().toString())
        }

        val oneDecimal = String.format("%.1f", value)
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
        val date = Date(timestamp)
        return if (use24h) {
            SimpleDateFormat("HH:mm", Locale.getDefault()).format(date)
        } else {
            format12HourWithDots(date)
        }
    }

    // -------------------------------------------------------------------------
    //  UPDATED TIMESTAMP (Used in ISS + KpIndex)
    // -------------------------------------------------------------------------
    fun formatUpdatedTimestamp(rawMillis: String?, use24h: Boolean): String {
        if (rawMillis.isNullOrBlank()) return "Unknown"

        return try {
            val millis = rawMillis.toLong()
            val date = Date(millis)

            return if (use24h) {
                SimpleDateFormat("MMM d, HH:mm", Locale.getDefault()).format(date)
            } else {
                val day = SimpleDateFormat("MMM d,", Locale.getDefault()).format(date)
                "$day ${format12HourWithDots(date)}"
            }

        } catch (e: Exception) {
            "Unknown"
        }
    }

    // -------------------------------------------------------------------------
    //  CONVERT TIME FORMAT (Simplified)
    // -------------------------------------------------------------------------
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
            } catch (e: Exception) { null }
        } ?: return input

        return if (use24h) {
            SimpleDateFormat("MMM dd, HH:mm", Locale.US).format(parsed)
        } else {
            val day = SimpleDateFormat("MMM dd,", Locale.US).format(parsed)
            "$day ${format12HourWithDots(parsed)}"
        }
    }

    // -------------------------------------------------------------------------
    //  COORDINATES, ALTITUDE, ETC. (unchanged)
    // -------------------------------------------------------------------------
    fun formatCoordinates(lat: Double, lon: Double): String {
        return "${formatSingleCoord(lat, 'N', 'S')}, ${formatSingleCoord(lon, 'E', 'W')}"
    }

    private fun formatSingleCoord(value: Double, positive: Char, negative: Char): String {
        val hemi = if (value >= 0) positive else negative
        val absValue = abs(value)
        val formatted = String.format("%.4f", absValue)
        return "$formatted° $hemi"
    }

    fun formatAltitude(km: Double): String = "${formatNumber(km)} km"
    fun formatVelocity(kmh: Double): String = "${formatNumber(kmh)} km/h"
    fun formatDistance(km: Double): String = "${formatNumber(km)} km"
    fun formatPercent(raw: Double): String = "${formatNumber(abs(raw))}%"

    fun formatMoonAge(days: Double): String = "${formatNumber(days)} days"

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
}