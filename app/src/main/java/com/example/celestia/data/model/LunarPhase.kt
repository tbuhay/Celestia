package com.example.celestia.data.model

import com.google.gson.annotations.SerializedName

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
