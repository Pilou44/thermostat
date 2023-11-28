package com.wechantloup.thermostat.model

data class Status(
    val deviceId: String,
    val temperature: Float,
    val on: Boolean,
)
