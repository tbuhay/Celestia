package com.example.celestia.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Represents a single user-created **Observation Journal entry**.
 *
 * Each entry captures sky conditions, environmental data, metadata pulled
 * from APIs (Kp Index, ISS coordinates, weather), and the user’s own notes.
 * This model is persisted in the local Room database and displayed across:
 *
 * - Observation history list
 * - Observation detail screen
 * - Auto-fill workflows inside the editor
 *
 * Fields are designed to be optional, since users may choose to manually
 * enter information or skip auto-populated values.
 *
 * @property id Auto-generated primary key for the entry.
 * @property observationTitle User-provided title describing the observation.
 * @property timestamp Unix timestamp (ms) recording when the entry was created.
 *
 * @property locationName Human-readable location (city, region, country), either
 * manually entered or auto-filled using device/home location.
 *
 * @property latitude Latitude coordinate of the observation (optional).
 * @property longitude Longitude coordinate of the observation (optional).
 *
 * @property kpIndex NOAA Kp Index value at time of observation (optional).
 *
 * @property issLat The latitude of the ISS at the moment of observation (optional).
 * @property issLon The longitude of the ISS at the moment of observation (optional).
 *
 * @property weatherSummary Text description of weather (e.g., “Clear”, “Light clouds”).
 * @property temperatureC Air temperature in Celsius.
 * @property cloudCoverPercent Estimated cloud cover percentage (0–100%).
 *
 * @property notes **Required** field where the user records their observation details.
 *
 * @property photoUrl Optional URI string pointing to an image selected from the device.
 */
@Entity(tableName = "observation_entries")
data class ObservationEntry(

    /** Auto-generated unique ID for each journal entry. */
    @PrimaryKey(autoGenerate = true) val id: Int = 0,

    /** User-defined title for the observation. */
    val observationTitle: String = "",

    /** Timestamp for when the entry was created (in milliseconds). */
    val timestamp: Long = System.currentTimeMillis(),

    /** Human-readable location name (optional). */
    val locationName: String? = null,

    /** Latitude coordinate (optional). */
    val latitude: Double? = null,

    /** Longitude coordinate (optional). */
    val longitude: Double? = null,

    /** Kp Index at time of observation (optional). */
    val kpIndex: Double? = null,

    /** ISS latitude at the time the entry was made (optional). */
    val issLat: Double? = null,

    /** ISS longitude at the same moment (optional). */
    val issLon: Double? = null,

    /** Short sky/weather description (optional). */
    val weatherSummary: String? = null,

    /** Temperature in °C (optional). */
    val temperatureC: Double? = null,

    /** Cloud cover percentage (optional). */
    val cloudCoverPercent: Int? = null,

    /** Main observation notes written by the user (required). */
    val notes: String = "",

    /** Photo URI string (optional). */
    val photoUrl: String? = null
)
