package com.example.celestia.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Represents a single snapshot of the International Space Station's
 * real-time orbital position and motion data.
 *
 * This entity is stored locally in Room and refreshed whenever the
 * ISS API endpoint is called.
 *
 * Notes:
 * - Only one record is stored in the database at a time.
 * - The `id` is fixed at `1` to simplify replacement of the latest reading.
 *
 * @property id Static primary key (always 1) used to overwrite the previous reading.
 * @property latitude Current latitude of the ISS in decimal degrees.
 * @property longitude Current longitude of the ISS in decimal degrees.
 * @property altitude Altitude above Earth in kilometers.
 * @property velocity Orbital velocity in kilometers per hour.
 * @property timestamp Timestamp of the reading (ISO or API-provided format).
 */
@Entity(tableName = "iss_reading")
data class IssReading(
    @PrimaryKey val id: Int = 1,
    val latitude: Double,
    val longitude: Double,
    val altitude: Double,
    val velocity: Double,
    val timestamp: String
)