package com.example.celestia.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "lunar_phase")
data class LunarPhaseEntity(
    @PrimaryKey val id: Int = 1,
    val moonPhase: String,
    val illuminationPercent: String,
    val moonRise: String?,
    val moonSet: String?,
    val moonDistanceKm: Double,
    val updatedAt: String
)