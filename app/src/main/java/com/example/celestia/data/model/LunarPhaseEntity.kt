package com.example.celestia.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "lunar_phase")
data class LunarPhaseEntity(
    @PrimaryKey val id: Int = 1, // always 1 row
    val moonPhase: String,
    val illuminationPercent: String,
    val moonRise: String?,
    val moonSet: String?,
    val moonDistanceKm: Double,
    val updatedAt: String // formatted local time when stored
)