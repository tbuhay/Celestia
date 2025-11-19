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

    // === Constants ===
    private const val synodicMonth = 29.530588
    private const val fullMoonAge = 14.8

    // === Moon Age Calculation ===

    /**
     * Computes the current moon age using Julian date calculations.
     * This represents the synodic age of the moon (0–29.53 days).
     *
     * @return The current moon age.
     */
    fun calculateRealMoonAge(): Double {
        val nowUtcMillis = System.currentTimeMillis()
        val jd = nowUtcMillis / 86400000.0 + 2440587.5

        val daysSinceNewMoon = jd - 2451550.1
        return (daysSinceNewMoon % synodicMonth + synodicMonth) % synodicMonth
    }

    /**
     * Returns the current moon age.
     */
    fun getMoonAge(): Double = calculateRealMoonAge()

    // === Next-Phase Calculations ===

    /**
     * Accurate days until the next full moon based on current lunar age.
     */
    fun daysUntilNextFullMoon(age: Double): Double {
        return if (age < fullMoonAge) {
            fullMoonAge - age
        } else {
            (synodicMonth - age) + fullMoonAge
        }
    }

    /**
     * Accurate days until the next new moon based on current lunar age.
     */
    fun daysUntilNextNewMoon(age: Double): Double {
        return synodicMonth - age
    }

    // === Phase Formatting ===

    /**
     * Converts "FIRST_QUARTER" → "First quarter".
     *
     * @param raw Raw phase string from API or database.
     */
    fun formatMoonPhaseName(raw: String?): String {
        if (raw.isNullOrBlank()) return "Unknown Phase"
        return raw.lowercase()
            .replace("_", " ")
            .replaceFirstChar { it.uppercase() }
    }

    // === Illumination Parsing ===

    /**
     * Safely parses the illumination percent from a LunarPhaseEntity.
     *
     * Negative values are normalized to positive, as illumination is conceptually positive.
     */
    fun parseIlluminationPercent(lunar: LunarPhaseEntity?): Double {
        if (lunar == null) return 0.0
        return lunar.illuminationPercent
            .toDoubleOrNull()
            ?.let { abs(it) }
            ?: 0.0
    }
}
