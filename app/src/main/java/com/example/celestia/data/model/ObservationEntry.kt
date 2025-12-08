package com.example.celestia.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "observation_entries")
data class ObservationEntry(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,

    // NEW FIELD: user-entered title
    val observationTitle: String = "",

    val timestamp: Long = System.currentTimeMillis(),

    // User-entered or auto-filled
    val locationName: String? = null,

    // Coordinates (optional)
    val latitude: Double? = null,
    val longitude: Double? = null,

    // Kp Index (optional)
    val kpIndex: Double? = null,

    // ISS coordinates (optional)
    val issLat: Double? = null,
    val issLon: Double? = null,

    // Weather (optional)
    val weatherSummary: String? = null,
    val temperatureC: Double? = null,
    val cloudCoverPercent: Int? = null,

    // Required field
    val notes: String = "",

    // Photo URL (stored as string)
    val photoUrl: String? = null
)
