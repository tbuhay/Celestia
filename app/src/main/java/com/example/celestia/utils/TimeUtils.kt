package com.example.celestia.utils

import android.os.Build
import androidx.annotation.RequiresApi
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.*

object TimeUtils {

    // -------------------------------------------------------------------------
    //  MAIN PUBLIC FORMATTER
    // -------------------------------------------------------------------------
    fun formatWithPreference(raw: String?, use24h: Boolean): String {
        val base = format(raw)
        return if (use24h) base else convertTo12Hour(base)
    }

    private fun convertTo12Hour(input: String): String {
        return try {
            val inputFormat = SimpleDateFormat("MMM dd, HH:mm", Locale.US)
            val date = inputFormat.parse(input) ?: return input

            val day = SimpleDateFormat("MMM dd,", Locale.US).format(date)
            val time = FormatUtils.run {
                val d = date
                // use the same suffix style
                val hour = SimpleDateFormat("h", Locale.getDefault()).format(d)
                val minute = SimpleDateFormat("mm", Locale.getDefault()).format(d)
                val isPM = SimpleDateFormat("a", Locale.getDefault()).format(d) == "PM"
                val suffix = if (isPM) "p.m." else "a.m."
                "$hour:$minute $suffix"
            }

            "$day $time"
        } catch (e: Exception) {
            input
        }
    }

    fun format(raw: String?): String {
        if (raw.isNullOrBlank()) return "Unknown"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            parseInstant(raw)?.let { instant ->
                return formatInstant(instant)
            }
        }

        parseLegacy(raw)?.let { date ->
            return formatDateLegacy(date)
        }

        return "Unknown"
    }

    // -------------------------------------------------------------------------
    // PARSING HELPERS
    // -------------------------------------------------------------------------
    @RequiresApi(Build.VERSION_CODES.O)
    private fun parseInstant(value: String): Instant? {
        try { return Instant.parse(value) } catch (_: Exception) {}
        try {
            val longVal = value.toLong()
            val millis = if (value.length == 10) longVal * 1000 else longVal
            return Instant.ofEpochMilli(millis)
        } catch (_: Exception) {}

        try {
            val cleaned = value.replace(" ", "T")
            val dt = ZonedDateTime.parse(
                cleaned,
                DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")
            )
            return dt.toInstant()
        } catch (_: Exception) {}

        return null
    }

    private fun parseLegacy(raw: String): Date? {
        try {
            val longVal = raw.toLong()
            val millis = if (raw.length == 10) longVal * 1000 else longVal
            return Date(millis)
        } catch (_: Exception) {}

        try {
            val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US)
            sdf.timeZone = TimeZone.getTimeZone("UTC")
            return sdf.parse(raw)
        } catch (_: Exception) {}

        if (raw.matches(Regex("\\d{2}:\\d{2}:\\d{2}(\\.\\d+)?"))) {
            return null
        }

        return null
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun formatInstant(instant: Instant): String {
        val zone = ZoneId.systemDefault()
        val fmt = DateTimeFormatter.ofPattern("MMM dd, HH:mm", Locale.US)
        return fmt.format(instant.atZone(zone))
    }

    private fun formatDateLegacy(date: Date): String {
        val sdf = SimpleDateFormat("MMM dd, HH:mm", Locale.US)
        sdf.timeZone = TimeZone.getDefault()
        return sdf.format(date)
    }
}