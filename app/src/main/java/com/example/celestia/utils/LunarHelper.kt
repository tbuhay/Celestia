package com.example.celestia.utils

import com.example.celestia.data.model.LunarPhaseEntity
import kotlin.math.abs

/**
 * Utility object for interpreting lunar illumination, moon phases,
 * and calculating lunar cycle values used throughout Celestia.
 *
 * All methods here are purely mathematical or formatting helpers.
 */
object LunarHelper {

    private const val lunarCycleDays = 29.53

    // === Phase Name Formatting ===

    /**
     * Converts an enum-like raw phase name such as "FIRST_QUARTER"
     * into a human-readable form such as "First quarter".
     *
     * @param raw The phase name string.
     * @return A readable phase label or "Unknown Phase".
     */
    fun formatMoonPhaseName(raw: String?): String {
        if (raw.isNullOrBlank()) return "Unknown Phase"
        return raw.lowercase()
            .replace("_", " ")
            .replaceFirstChar { it.uppercase() }
    }

    // === Illumination Percent Parsing ===

    /**
     * Safely parses the illumination percent from a LunarPhaseEntity,
     * removing the percent symbol and handling invalid inputs.
     *
     * @param lunar The lunar phase entity.
     * @return The absolute illumination percent as a Double.
     */
    fun parseIlluminationPercent(lunar: LunarPhaseEntity?): Double {
        if (lunar == null) return 0.0
        return lunar.illuminationPercent
            .replace("%", "")
            .trim()
            .toDoubleOrNull()
            ?.let { abs(it) }
            ?: 0.0
    }

    // === Days Until Next Major Phases ===

    /**
     * Calculates days until the next full moon based on the current moon age.
     *
     * @param age Current moon age in days.
     * @return Number of days until the next full moon.
     */
    fun daysUntilNextFullMoon(age: Double): Double {
        val fullMoonAge = 14.8
        return if (age <= fullMoonAge) {
            fullMoonAge - age
        } else {
            (lunarCycleDays - age) + fullMoonAge
        }
    }

    /**
     * Calculates days until the next new moon based on the current moon age.
     *
     * @param age Current moon age in days.
     * @return Days until new moon (0 if already at new moon).
     */
    fun daysUntilNextNewMoon(age: Double): Double {
        return if (age <= 0.0) 0.0 else lunarCycleDays - age
    }

    // === Lunar Age Calculation ===

    /**
     * Computes the current moon age using Julian date calculations.
     * This represents the synodic age of the moon (0–29.53 days).
     *
     * @return The current moon age.
     */
    fun calculateRealMoonAge(): Double {
        val nowUtcMillis = System.currentTimeMillis()
        val jd = nowUtcMillis / 86400000.0 + 2440587.5

        val synodicMonth = 29.530588
        val referenceNewMoonJD = 2451550.1

        val daysSinceNewMoon = jd - referenceNewMoonJD
        return (daysSinceNewMoon % synodicMonth + synodicMonth) % synodicMonth
    }

    /**
     * Returns the current moon age. Alias for calculateRealMoonAge().
     *
     * @return The current moon age in days.
     */
    fun getMoonAge(): Double = calculateRealMoonAge()
}
