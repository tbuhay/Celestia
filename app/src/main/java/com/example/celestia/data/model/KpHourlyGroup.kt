package com.example.celestia.data.model

import java.util.Date

/**
 * Represents a grouped summary of Kp Index readings for a specific hour.
 *
 * This model is used only on the UI layer to display aggregated Kp data
 * (e.g., averages and ranges) for easier interpretation on the Kp Index screen.
 *
 * It is *not* stored in the database — instead, it is computed from
 * raw NOAA Kp readings and grouped by hour.
 *
 * @property hour The hour these readings represent (Date truncated to the hour).
 * @property avg The average Kp Index value for this hour.
 * @property high The highest Kp Index value for this hour.
 * @property low The lowest Kp Index value for this hour.
 */
data class KpHourlyGroup(
    val hour: Date,
    val avg: Double,
    val high: Double,
    val low: Double
)