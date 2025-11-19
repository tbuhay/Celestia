package com.example.celestia.utils

import android.os.Build
import androidx.annotation.RequiresApi
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

    // === Diameter & Basic Properties ===

    /**
     * Computes the average estimated diameter of an asteroid in meters.
     *
     * @param asteroid The asteroid approach object containing min/max diameters.
     * @return The average diameter in meters.
     */
    fun avgDiameter(asteroid: AsteroidApproach): Double {
        return (asteroid.diameterMinMeters + asteroid.diameterMaxMeters) / 2.0
    }

    /**
     * Determines whether an asteroid is considered "meaningful" for user display.
     * A meaningful asteroid must:
     * - Have an average diameter ≥ 120m
     * - Have a miss distance ≤ 0.5 AU
     *
     * @param asteroid The asteroid to evaluate.
     * @return True if the asteroid meets visibility criteria.
     */
    fun isMeaningful(asteroid: AsteroidApproach): Boolean {
        val diameter = avgDiameter(asteroid)
        val isBigEnough = diameter >= 120.0
        val isCloseEnough = asteroid.missDistanceAu <= 0.5
        return isBigEnough && isCloseEnough
    }

    // === Date Window Logic ===

    /**
     * Returns true if the given date (yyyy-MM-dd) occurs within the next 7 days,
     * including today.
     *
     * @param dateString The date string in ISO yyyy-MM-dd format.
     * @return True if the date is between today and seven days from now.
     */
    @RequiresApi(Build.VERSION_CODES.O)
    fun isWithinNext7Days(dateString: String): Boolean {
        val today = LocalDate.now()
        val date = LocalDate.parse(dateString)
        return !date.isBefore(today) && !date.isAfter(today.plusDays(7))
    }

    // === Meaningful Asteroid Lists ===

    /**
     * Filters the list to meaningful asteroids that occur within the next 7 days,
     * sorted first by distance from Earth, then by date.
     *
     * @param list Full asteroid approach dataset.
     * @return A sorted list of meaningful asteroid approaches.
     */
    @RequiresApi(Build.VERSION_CODES.O)
    fun getMeaningfulAsteroids(list: List<AsteroidApproach>): List<AsteroidApproach> {
        return list
            .filter { asteroid ->
                isMeaningful(asteroid) && isWithinNext7Days(asteroid.approachDate)
            }
            .sortedWith(
                compareBy<AsteroidApproach> { it.missDistanceAu }
                    .thenBy { LocalDate.parse(it.approachDate) }
            )
    }

    /**
     * Returns asteroids that qualify for the “next 7 days” option of the UI.
     * Meaningful asteroids only, sorted chronologically.
     *
     * @param list The asteroid approaches.
     * @return A list sorted by approach date.
     */
    @RequiresApi(Build.VERSION_CODES.O)
    fun getNext7DaysList(list: List<AsteroidApproach>): List<AsteroidApproach> {
        return list
            .filter { isMeaningful(it) && isWithinNext7Days(it.approachDate) }
            .sortedBy { LocalDate.parse(it.approachDate) }
    }

    // === Featured Asteroid Selection ===

    /**
     * Selects the best asteroid to feature in the UI.
     * Priority order:
     * 1. First meaningful asteroid from the next 7 days (sorted by distance).
     * 2. If none, the closest asteroid within the next 7 days.
     * 3. If none, the closest asteroid overall.
     *
     * @param list The asteroid approach list.
     * @return The asteroid to be featured, or null if list is empty.
     */
    @RequiresApi(Build.VERSION_CODES.O)
    fun getFeaturedAsteroid(list: List<AsteroidApproach>): AsteroidApproach? {
        if (list.isEmpty()) return null

        val meaningful = getMeaningfulAsteroids(list)
        if (meaningful.isNotEmpty()) return meaningful.first()

        val next7 = list.filter { isWithinNext7Days(it.approachDate) }
        if (next7.isNotEmpty()) {
            return next7.minByOrNull { it.missDistanceAu }
        }

        return list.minByOrNull { it.missDistanceAu }
    }
}
