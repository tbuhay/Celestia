package com.example.celestia.utils

import com.example.celestia.data.model.LunarPhaseEntity
import kotlin.math.abs

object LunarHelper {

    private const val lunarCycleDays = 29.53

    /** Converts "FIRST_QUARTER" → "First quarter". */
    fun formatMoonPhaseName(raw: String?): String {
        if (raw.isNullOrBlank()) return "Unknown Phase"
        return raw.lowercase()
            .replace("_", " ")
            .replaceFirstChar { it.uppercase() }
    }

    /** Parses percent illumination safely. */
    fun parseIlluminationPercent(lunar: LunarPhaseEntity?): Double {
        if (lunar == null) return 0.0
        return lunar.illuminationPercent
            .replace("%", "")
            .trim()
            .toDoubleOrNull()
            ?.let { abs(it) }
            ?: 0.0
    }

    /** Calculates days until next full moon based on current moon age. */
    fun daysUntilNextFullMoon(age: Double): Double {
        val fullMoonAge = 14.8
        return if (age <= fullMoonAge) {
            fullMoonAge - age
        } else {
            (lunarCycleDays - age) + fullMoonAge
        }
    }

    /** Calculates days until the next new moon based on current age. */
    fun daysUntilNextNewMoon(age: Double): Double {
        return if (age <= 0.0) 0.0 else lunarCycleDays - age
    }

    /** Real moon age based on Julian date. */
    fun calculateRealMoonAge(): Double {
        val nowUtcMillis = System.currentTimeMillis()
        val jd = nowUtcMillis / 86400000.0 + 2440587.5

        val synodicMonth = 29.530588
        val referenceNewMoonJD = 2451550.1

        val daysSinceNewMoon = jd - referenceNewMoonJD
        return (daysSinceNewMoon % synodicMonth + synodicMonth) % synodicMonth
    }

    fun getMoonAge(): Double = calculateRealMoonAge()
}