package com.example.celestia.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Represents a single near-Earth asteroid close approach event.
 *
 * This entity is stored in the local Room database and is populated
 * using data from NASA's Near-Earth Object (NEO) API.
 *
 * Each record corresponds to one future or past approach of an asteroid,
 * including distance, velocity, and size estimates.
 *
 * @property id Unique NASA NEO identifier used as the primary key.
 * @property name The human-readable asteroid name or designation.
 * @property approachDate The date of the asteroid’s closest approach to Earth (YYYY-MM-DD).
 * @property missDistanceKm Miss distance in kilometers.
 * @property missDistanceAu Miss distance in astronomical units.
 * @property relativeVelocityKph Relative velocity in kilometers per hour.
 * @property diameterMinMeters Estimated minimum diameter in meters.
 * @property diameterMaxMeters Estimated maximum diameter in meters.
 * @property isPotentiallyHazardous Whether NASA flags the asteroid as potentially hazardous.
 */
@Entity(tableName = "asteroid_approaches")
data class AsteroidApproach(
    @PrimaryKey
    val id: String,

    val name: String,
    val approachDate: String,
    val missDistanceKm: Double,
    val missDistanceAu: Double,
    val relativeVelocityKph: Double,
    val diameterMinMeters: Double,
    val diameterMaxMeters: Double,
    val isPotentiallyHazardous: Boolean
)
