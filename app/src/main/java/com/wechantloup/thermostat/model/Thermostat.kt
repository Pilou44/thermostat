package com.wechantloup.thermostat.model

data class Thermostat(
    val deviceId: String,
    val temperature: Float,
    val on: Boolean,
    val time: String,
)
