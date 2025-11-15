package com.example.celestia.data.model

import com.google.gson.annotations.SerializedName

data class AsteroidFeedResponse(
    @SerializedName("near_earth_objects")
    val nearEarthObjects: Map<String, List<NeoObjectDto>>
)

data class NeoObjectDto(
    val id: String,
    val name: String,
    @SerializedName("is_potentially_hazardous_asteroid")
    val isHazardous: Boolean,
    @SerializedName("estimated_diameter")
    val estimatedDiameter: EstimatedDiameterDto,
    @SerializedName("close_approach_data")
    val closeApproachData: List<CloseApproachDto>
)

data class EstimatedDiameterDto(
    @SerializedName("meters")
    val meters: DiameterRangeDto
)

data class DiameterRangeDto(
    @SerializedName("estimated_diameter_min") val min: Double,
    @SerializedName("estimated_diameter_max") val max: Double
)

data class CloseApproachDto(
    @SerializedName("close_approach_date") val date: String,
    @SerializedName("relative_velocity") val velocity: VelocityDto,
    @SerializedName("miss_distance") val missDistance: MissDistanceDto
)

data class VelocityDto(
    @SerializedName("kilometers_per_hour") val kph: String
)

data class MissDistanceDto(
    @SerializedName("kilometers") val km: String,
    @SerializedName("astronomical") val au: String
)
