package com.example.celestia.utils

import android.os.Build
import androidx.annotation.RequiresApi
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.*

/**
 * Utility object for handling time parsing and formatting across devices and API levels.
 *
 * Provides consistent formatting for timestamps, supports both Instant (API 26+) and legacy Date,
 * and integrates with FormatUtils for 12-hour suffix conventions.
 */
object TimeUtils {

    // === Main Public Formatter ===

    /**
     * Formats a raw time string using the user's 12-hour or 24-hour clock preference.
     *
     * @param raw The raw timestamp input (ISO-8601, epoch millis/seconds, etc.)
     * @param use24h Whether the user prefers 24-hour time format.
     * @return A formatted date-time string or "Unknown" if parsing fails.
     */
    fun formatWithPreference(raw: String?, use24h: Boolean): String {
        val base = format(raw)
        return if (use24h) base else convertTo12Hour(base)
    }

    /**
     * Converts a formatted 24-hour time string ("MMM dd, HH:mm") into a 12-hour format
     * using Celestia’s custom a.m./p.m. suffix style.
     *
     * @param input The formatted 24-hour timestamp.
     * @return The converted 12-hour timestamp or the original input on failure.
     */
    private fun convertTo12Hour(input: String): String {
        return try {
            val inputFormat = SimpleDateFormat("MMM dd, HH:mm", Locale.US)
            val date = inputFormat.parse(input) ?: return input

            val dayPart = SimpleDateFormat("MMM dd,", Locale.US).format(date)

            val timePart = FormatUtils.run {
                val d = date
                val hour = SimpleDateFormat("h", Locale.getDefault()).format(d)
                val minute = SimpleDateFormat("mm", Locale.getDefault()).format(d)
                val isPM = SimpleDateFormat("a", Locale.getDefault()).format(d) == "PM"
                val suffix = if (isPM) "p.m." else "a.m."
                "$hour:$minute $suffix"
            }

            "$dayPart $timePart"
        } catch (e: Exception) {
            input
        }
    }

    /**
     * Attempts to parse and format a raw timestamp using available parsing strategies.
     * Supports Instant parsing (API 26+), epoch values, and ISO-like strings.
     *
     * @param raw The timestamp to format.
     * @return A standardized formatted time string or "Unknown".
     */
    fun format(raw: String?): String {
        if (raw.isNullOrBlank()) return "Unknown"

        // API 26+ Instant parsing
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            parseInstant(raw)?.let { instant ->
                return formatInstant(instant)
            }
        }

        // Legacy Date fallback
        parseLegacy(raw)?.let { date ->
            return formatDateLegacy(date)
        }

        return "Unknown"
    }

    // === Parsing Helpers ===

    /**
     * Tries multiple strategies to convert a raw string into an Instant.
     * Supports:
     * - ISO-8601 `Instant.parse`
     * - Epoch seconds or millis
     * - Simplified `yyyy-MM-dd HH:mm:ss` patterns
     *
     * @param value The raw timestamp string.
     * @return A parsed Instant or null if all strategies fail.
     */
    @RequiresApi(Build.VERSION_CODES.O)
    private fun parseInstant(value: String): Instant? {
        try {
            return Instant.parse(value)
        } catch (_: Exception) {
        }

        try {
            val longVal = value.toLong()
            val millis = if (value.length == 10) longVal * 1000 else longVal
            return Instant.ofEpochMilli(millis)
        } catch (_: Exception) {
        }

        try {
            val cleaned = value.replace(" ", "T")
            val dt = ZonedDateTime.parse(
                cleaned,
                DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")
            )
            return dt.toInstant()
        } catch (_: Exception) {
        }

        return null
    }

    /**
     * Legacy fallback for parsing dates on older Android versions.
     * Supports:
     * - Epoch millis or seconds
     * - Basic ISO-like strings with UTC timezone
     *
     * @param raw The raw timestamp string.
     * @return A parsed Date or null.
     */
    private fun parseLegacy(raw: String): Date? {
        try {
            val longVal = raw.toLong()
            val millis = if (raw.length == 10) longVal * 1000 else longVal
            return Date(millis)
        } catch (_: Exception) {
        }

        try {
            val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US)
            sdf.timeZone = TimeZone.getTimeZone("UTC")
            return sdf.parse(raw)
        } catch (_: Exception) {
        }

        // Reject pure time strings (e.g., "12:45:00") — not enough data
        if (raw.matches(Regex("\\d{2}:\\d{2}:\\d{2}(\\.\\d+)?"))) {
            return null
        }

        return null
    }

    // === Output Formatting Helpers ===

    /**
     * Formats an Instant into "MMM dd, HH:mm" using system time zone.
     *
     * @param instant The Instant to format.
     * @return A formatted timestamp string.
     */
    @RequiresApi(Build.VERSION_CODES.O)
    private fun formatInstant(instant: Instant): String {
        val zone = ZoneId.systemDefault()
        val fmt = DateTimeFormatter.ofPattern("MMM dd, HH:mm", Locale.US)
        return fmt.format(instant.atZone(zone))
    }

    /**
     * Formats a legacy Date into "MMM dd, HH:mm" using the local device timezone.
     *
     * @param date The Date object.
     * @return A formatted timestamp string.
     */
    private fun formatDateLegacy(date: Date): String {
        val sdf = SimpleDateFormat("MMM dd, HH:mm", Locale.US)
        sdf.timeZone = TimeZone.getDefault()
        return sdf.format(date)
    }
}
