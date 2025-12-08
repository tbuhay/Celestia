package com.example.celestia.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Represents a single sky observation entry made by the user.
 *
 * Each entry captures:
 * - When it was created (timestamp)
 * - Where the user was (location name + lat/lon)
 * - Sky conditions (Kp index, ISS position, weather summary)
 * - User notes and an optional photo URL (Firebase Storage or local URI)
 */
@Entity(tableName = "observation_entries")
data class ObservationEntry(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,

    // When the entry was created (System.currentTimeMillis())
    val timestamp: Long,

    // Short title for quickly identifying the observation
    val observationTitle: String = "",

    // Location
    val locationName: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,

    // Automatically captured data
    val kpIndex: Double? = null,
    val issLat: Double? = null,
    val issLon: Double? = null,
    val weatherSummary: String? = null,
    val temperatureC: Double? = null,
    val cloudCoverPercent: Int? = null,

    // User content
    val notes: String = "",
    val photoUrl: String? = null // Firebase Storage URL or local URI
)
