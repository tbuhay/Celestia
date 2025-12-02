package com.example.celestia.data.model

import com.google.gson.annotations.SerializedName


/**
 * Represents the current lunar phase information returned by your
 * astronomy/lunar API source.
 *
 * This DTO is parsed directly by Retrofit and later mapped into
 * [LunarPhaseEntity] for local Room storage.
 *
 * Includes key astronomical details such as:
 * - Phase name (e.g., "Waxing Crescent")
 * - Illumination percentage
 * - Moonrise and moonset times
 * - Lunar distance from Earth
 *
 * @property date The calendar date for which the data applies (YYYY-MM-DD).
 * @property currentTime Timestamp identifying when this data was computed.
 * @property moonPhase Human-readable phase description.
 * @property moonrise Local moonrise time.
 * @property moonset Local moonset time.
 * @property moonDistance Distance from Earth in kilometers.
 * @property moonIlluminationPercentage Illuminated fraction of the moon as a formatted string.
 */
data class LunarPhase(
    @SerializedName("date")
    val date: String,

    @SerializedName("current_time")
    val currentTime: String,

    @SerializedName("moon_phase")
    val moonPhase: String,

    @SerializedName("moonrise")
    val moonrise: String,

    @SerializedName("moonset")
    val moonset: String,

    @SerializedName("moon_distance")
    val moonDistance: Double,

    @SerializedName("moon_illumination_percentage")
    val moonIlluminationPercentage: String
)
