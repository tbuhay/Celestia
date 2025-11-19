package com.example.celestia.utils

import android.os.Build
import androidx.annotation.RequiresApi
import com.example.celestia.data.model.AsteroidApproach
import java.time.LocalDate

object AsteroidHelper {

    /** Returns the average diameter of an asteroid in meters. */
    fun avgDiameter(asteroid: AsteroidApproach): Double {
        return (asteroid.diameterMinMeters + asteroid.diameterMaxMeters) / 2.0
    }

    /** Determines if an asteroid is large enough and close enough to be meaningful. */
    fun isMeaningful(asteroid: AsteroidApproach): Boolean {
        val diameter = avgDiameter(asteroid)
        val isBigEnough = diameter >= 120.0 // meters
        val isCloseEnough = asteroid.missDistanceAu <= 0.5
        return isBigEnough && isCloseEnough
    }

    /** Returns whether the asteroid's date is within the next 7 days. */
    @RequiresApi(Build.VERSION_CODES.O)
    fun isWithinNext7Days(dateString: String): Boolean {
        val today = LocalDate.now()
        val date = LocalDate.parse(dateString)
        return !date.isBefore(today) && !date.isAfter(today.plusDays(7))
    }

    /** Meaningful asteroids sorted by distance then date. */
    @RequiresApi(Build.VERSION_CODES.O)
    fun getMeaningfulAsteroids(list: List<AsteroidApproach>): List<AsteroidApproach> {
        return list.filter { asteroid ->
            isMeaningful(asteroid) && isWithinNext7Days(asteroid.approachDate)
        }
            .sortedWith(
                compareBy<AsteroidApproach> { it.missDistanceAu }
                    .thenBy { LocalDate.parse(it.approachDate) }
            )
    }

    /** Returns asteroids that qualify for Option D display. */
    @RequiresApi(Build.VERSION_CODES.O)
    fun getNext7DaysList(list: List<AsteroidApproach>): List<AsteroidApproach> {
        return list.filter { isMeaningful(it) && isWithinNext7Days(it.approachDate) }
            .sortedBy { LocalDate.parse(it.approachDate) }
    }

    /** Determines the featured asteroid for the UI. */
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