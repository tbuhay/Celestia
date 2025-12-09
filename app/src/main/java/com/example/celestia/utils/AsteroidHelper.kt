package com.example.celestia.utils

import com.example.celestia.data.model.AsteroidApproach
import java.time.LocalDate

/**
 * Utility object for filtering, evaluating, and preparing asteroid approach data
 * for display within the Celestia application.
 *
 * Logic here focuses purely on interpretation and presentation rules (e.g.,
 * "meaningful" asteroids, sorting criteria, upcoming days).
 */

object AsteroidHelper {

    // -------------------------------
    // Diameter helpers
    // -------------------------------
    private fun avgDiameter(asteroid: AsteroidApproach): Double {
        return (asteroid.diameterMinMeters + asteroid.diameterMaxMeters) / 2.0
    }

    // -------------------------------
    // PHA classification (NASA rules)
    // -------------------------------
    fun isPotentiallyHazardous(asteroid: AsteroidApproach): Boolean {
        val diameter = avgDiameter(asteroid)
        return diameter >= 140.0 && asteroid.missDistanceAu <= 0.05
    }

    // -------------------------------
    // Meaningful asteroid (UI list)
    // -------------------------------
    private fun isMeaningful(asteroid: AsteroidApproach): Boolean {
        val diameter = avgDiameter(asteroid)
        val bigEnough = diameter >= 50.0       // lowered from 120m → 50m
        val closeEnough = asteroid.missDistanceAu <= 0.5
        return bigEnough && closeEnough
    }

    // -------------------------------
    // Next 7 days
    // -------------------------------
    private fun isWithinNext7Days(dateStr: String): Boolean {
        val today = LocalDate.now()
        val date = LocalDate.parse(dateStr)
        return !date.isBefore(today) && !date.isAfter(today.plusDays(7))
    }

    // -------------------------------
    // LIST FOR UI (refined)
    // -------------------------------
    fun getNext7DaysList(list: List<AsteroidApproach>): List<AsteroidApproach> {
        return list
            .filter { isMeaningful(it) && isWithinNext7Days(it.approachDate) }
            .sortedBy { it.missDistanceAu }
    }

    // -------------------------------
    // Featured asteroid
    // -------------------------------
    fun getFeaturedAsteroid(list: List<AsteroidApproach>): AsteroidApproach? {
        if (list.isEmpty()) return null

        val next7 = list.filter { isWithinNext7Days(it.approachDate) }

        // Priority 1 — PHA asteroid first
        val pha = next7.filter { isPotentiallyHazardous(it) }
            .minByOrNull { it.missDistanceAu }
        if (pha != null) return pha

        // Priority 2 — meaningful (≥50m and ≤0.5 AU)
        val meaningful = next7.filter { isMeaningful(it) }
            .sortedBy { it.missDistanceAu }
        if (meaningful.isNotEmpty()) return meaningful.first()

        // Fallback: closest asteroid in next 7 days
        return next7.minByOrNull { it.missDistanceAu }
    }
}
