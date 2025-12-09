package com.example.celestia.data.model

import com.google.gson.annotations.SerializedName

/**
 * Top-level response object for NASA's Near-Earth Object (NEO) Feed API.
 *
 * The API groups NEOs by date, so the response contains a map where:
 * - The key is a date string (e.g., "2024-12-01")
 * - The value is a list of objects approaching on that date
 *
 * This DTO is parsed directly by Retrofit and later transformed
 * into domain models like [AsteroidApproach].
 *
 * @property nearEarthObjects Map of date → list of NEO objects.
 */
data class AsteroidFeedResponse(
    @SerializedName("near_earth_objects")
    val nearEarthObjects: Map<String, List<NeoObjectDto>>
)

/**
 * Data Transfer Object representing a single NEO (asteroid) entry from the API.
 *
 * Includes hazard status, estimated diameter, and all recorded close approaches.
 *
 * @property id Unique NASA NEO identifier.
 * @property name Human-readable asteroid name or designation.
 * @property isHazardous Whether the asteroid is considered potentially hazardous.
 * @property estimatedDiameter Diameter information in various units.
 * @property closeApproachData List of close approach events (date, velocity, distance).
 */
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

/**
 * Container for diameter estimates across measurement systems.
 *
 * NASA provides diameters in kilometers, meters, miles, and feet.
 * Celestia uses the meter values for consistency.
 *
 * @property meters Estimated diameter range in meters.
 */
data class EstimatedDiameterDto(
    @SerializedName("meters")
    val meters: DiameterRangeDto
)

/**
 * Represents minimum and maximum estimated diameter for a NEO.
 *
 * @property min Minimum estimated diameter in meters.
 * @property max Maximum estimated diameter in meters.
 */
data class DiameterRangeDto(
    @SerializedName("estimated_diameter_min") val min: Double,
    @SerializedName("estimated_diameter_max") val max: Double
)

/**
 * Represents a single close-approach event for a NEO.
 *
 * Includes approach date, relative velocity, and miss distance.
 *
 * @property date The date of closest approach (YYYY-MM-DD).
 * @property velocity Velocity data (km/h).
 * @property missDistance Miss distance data (km and AU).
 */
data class CloseApproachDto(
    @SerializedName("close_approach_date") val date: String,
    @SerializedName("relative_velocity") val velocity: VelocityDto,
    @SerializedName("miss_distance") val missDistance: MissDistanceDto
)

/**
 * Represents asteroid relative velocity data.
 *
 * NASA returns velocity values as strings.
 * Conversion to Double happens later in mapping logic.
 *
 * @property kph Velocity in kilometers per hour.
 */
data class VelocityDto(
    @SerializedName("kilometers_per_hour") val kph: String
)

/**
 * Represents the distance an asteroid will pass by Earth.
 *
 * Raw values come as strings.
 *
 * @property km Miss distance in kilometers.
 * @property au Miss distance in astronomical units.
 */
data class MissDistanceDto(
    @SerializedName("kilometers") val km: String,
    @SerializedName("astronomical") val au: String
)
