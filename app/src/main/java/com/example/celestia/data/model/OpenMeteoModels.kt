package com.example.celestia.data.model

import com.google.gson.annotations.SerializedName

data class OpenMeteoResponse(
    @SerializedName("current")
    val current: OpenMeteoCurrent?
)

data class OpenMeteoCurrent(
    @SerializedName("temperature_2m")
    val temperatureC: Double?,

    @SerializedName("cloud_cover")
    val cloudCoverPercent: Int?,

    @SerializedName("weather_code")
    val weatherCode: Int?
)

/**
 * Normalized snapshot of the current weather
 * for use by the Observation Journal.
 */
data class WeatherSnapshot(
    val temperatureC: Double?,
    val cloudCoverPercent: Int?,
    val weatherSummary: String?
)