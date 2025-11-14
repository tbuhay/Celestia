package com.example.celestia.data.model

import java.util.Date

data class KpHourlyGroup(
    val hour: Date,
    val avg: Double,
    val high: Double,
    val low: Double
)