package com.example.celestia.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey


/**
 * Room entity representing the most recently fetched lunar phase data.
 *
 * This is the persistent, normalized form of the API response
 * provided by the [LunarPhase] DTO. Only one record is stored at a time,
 * using a fixed primary key (`id = 1`) so that new data replaces the old.
 *
 * Fields include:
 * - Current phase name
 * - Illumination percentage
 * - Moonrise and moonset times (nullable depending on API availability)
 * - Lunar distance from Earth
 * - Timestamp of when the data was last updated
 *
 * @property id Static primary key used to overwrite previous lunar data.
 * @property moonPhase Human-readable lunar phase name (e.g., "Waxing Gibbous").
 * @property illuminationPercent Illuminated fraction of the moon as a formatted string.
 * @property moonRise Local moonrise time (nullable if unavailable from API).
 * @property moonSet Local moonset time (nullable if unavailable from API).
 * @property moonDistanceKm Distance from Earth in kilometers.
 * @property updatedAt ISO timestamp indicating when this data was last refreshed.
 */
@Entity(tableName = "lunar_phase")
data class LunarPhaseEntity(
    @PrimaryKey val id: Int = 1,
    val moonPhase: String,
    val illuminationPercent: String,
    val moonRise: String?,
    val moonSet: String?,
    val moonDistanceKm: Double,
    val updatedAt: String
)