package com.wechantloup.thermostat.model

import androidx.annotation.Keep

@Keep
data class Thermostat(
    val deviceId: String,
    val temperature: Float,
    val humidity: Float,
    val on: Boolean,
    val time: String,
)
