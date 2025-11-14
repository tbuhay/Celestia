package com.example.celestia.utils

import android.os.Build
import java.text.ParseException
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.*

object TimeUtils {

    // -------------------------------------------------------------------------
    //  PUBLIC FUNCTION
    // -------------------------------------------------------------------------
    /**
     * Convert ANY supported timestamp into local device time in the format:
     *
     *     MMM dd, HH:mm      (example: Nov 12, 14:32)
     *
     * This is the ONLY timestamp format Celestia uses.
     */
    fun format(raw: String?): String {
        if (raw.isNullOrBlank()) return "Unknown"

        // Try Android O+ first (more accurate & more formats)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            parseInstant(raw)?.let { instant ->
                return formatInstant(instant)
            }
        }

        // Fallback for older devices (manual parsing)
        parseLegacy(raw)?.let { date ->
            return formatDateLegacy(date)
        }

        return "Unknown"
    }

    // -------------------------------------------------------------------------
    //  ANDROID O+ PARSING
    // -------------------------------------------------------------------------
    @Suppress("NewApi")
    private fun parseInstant(value: String): Instant? {
        // Try ISO formats first (NASA / Lunar)
        try {
            return Instant.parse(value)
        } catch (_: Exception) { }

        // If it's UNIX ms or sec
        try {
            val longVal = value.toLong()
            val millis = if (value.length == 10) longVal * 1000 else longVal
            return Instant.ofEpochMilli(millis)
        } catch (_: Exception) { }

        // NOAA: "2025-11-12T14:00:00" (no Z)
        try {
            val cleaned = value.replace(" ", "T")
            val dt = ZonedDateTime.parse(
                cleaned,
                DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")
            )
            return dt.toInstant()
        } catch (_: Exception) { }

        return null
    }

    @Suppress("NewApi")
    private fun formatInstant(instant: Instant): String {
        val zone = ZoneId.systemDefault()
        val formatter = DateTimeFormatter.ofPattern("MMM dd, HH:mm", Locale.US)
        return formatter.format(instant.atZone(zone))
    }

    // -------------------------------------------------------------------------
    //  LEGACY (< ANDROID O) PARSING
    // -------------------------------------------------------------------------
    private fun parseLegacy(raw: String): Date? {
        // UNIX timestamps
        try {
            val longVal = raw.toLong()
            val millis = if (raw.length == 10) longVal * 1000 else longVal
            return Date(millis)
        } catch (_: Exception) { }

        // NOAA format
        try {
            val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US)
            sdf.timeZone = TimeZone.getTimeZone("UTC")
            return sdf.parse(raw)
        } catch (_: Exception) { }

        // Lunar API time-only (we cannot parse without a date)
        if (raw.matches(Regex("\\d{2}:\\d{2}:\\d{2}(\\.\\d+)?"))) {
            return null // handled elsewhere, VM will combine date + time
        }

        return null
    }

    private fun formatDateLegacy(date: Date): String {
        val sdf = SimpleDateFormat("MMM dd, HH:mm", Locale.US)
        sdf.timeZone = TimeZone.getDefault()
        return sdf.format(date)
    }

    // -------------------------------------------------------------------------
    //  SPECIAL HANDLER:
    //  Combine Lunar date ("2025-11-12") + time ("19:04:19.662")
    // -------------------------------------------------------------------------
    fun format(date: String?, time: String?): String {
        if (date == null || time == null) return "Unknown"
        return try {
            val cleanTime = time.substring(0, 5) // HH:mm
            val combined = "$date $cleanTime"
            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US)
            sdf.timeZone = TimeZone.getDefault()
            val parsed = sdf.parse(combined)
            format(parsed?.let { formatDateLegacy(it) } ?: combined)
        } catch (_: Exception) {
            "Unknown"
        }
    }
}
