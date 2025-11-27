package com.example.celestia.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity(tableName = "kp_readings")
data class KpReading(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    @SerializedName("time_tag")
    val timestamp: String,

    @SerializedName("kp_index")
    val kpIndex: Int,

    @SerializedName("estimated_kp")
    val estimatedKp: Double,

    @SerializedName("kp")
    val kpLabel: String
)