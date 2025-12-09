package com.example.celestia.data.model


/**
 * Represents a single astronaut currently in space, as reported by the
 * Open Notify API (or similar astronaut data source).
 *
 * This model is used to display:
 * - The astronaut's name
 * - The spacecraft or station they are aboard (e.g., "ISS")
 *
 * @property name The astronaut's full name.
 * @property craft The spacecraft or station the astronaut is assigned to.
 */
data class Astronaut(
    val name: String,
    val craft: String
)