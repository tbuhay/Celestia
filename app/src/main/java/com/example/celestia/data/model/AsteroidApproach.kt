package com.example.celestia.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "asteroid_approaches")
data class AsteroidApproach(
    @PrimaryKey
    val id: String,

    val name: String,
    val approachDate: String,
    val missDistanceKm: Double,
    val missDistanceAu: Double,
    val relativeVelocityKph: Double,
    val diameterMinMeters: Double,
    val diameterMaxMeters: Double,
    val isPotentiallyHazardous: Boolean
)
