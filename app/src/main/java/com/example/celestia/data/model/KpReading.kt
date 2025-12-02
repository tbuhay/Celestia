package com.example.celestia.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

/**
 * Represents a single Kp Index reading from NOAA's Space Weather Prediction Center.
 *
 * The Kp Index measures global geomagnetic activity on a scale from 0 to 9,
 * and is commonly used to estimate aurora visibility and space weather conditions.
 *
 * This entity is stored locally in Room and populated through the NOAA API.
 *
 * Notes:
 * - The API provides each reading with a timestamp-based key (`time_tag`),
 *   which is used directly as the primary key for storage.
 *
 * @property timestamp ISO-formatted time (UTC) identifying the reading.
 * @property kpIndex The raw Kp Index value (integer 0–9).
 * @property estimatedKp Modelled/estimated Kp value provided by NOAA.
 * @property kpLabel A textual interpretation or category (e.g., “Active”, “Minor Storm”).
 */
@Entity(tableName = "kp_readings")
data class KpReading(

    @PrimaryKey
    @SerializedName("time_tag")
    val timestamp: String,

    @SerializedName("kp_index")
    val kpIndex: Int,

    @SerializedName("estimated_kp")
    val estimatedKp: Double,

    @SerializedName("kp")
    val kpLabel: String
)