package com.wechantloup.thermostat.model

data class Status(
    var temperature: Float = 0f,
    var on: Boolean = false,
)
