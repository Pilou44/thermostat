package com.wechantloup.thermostat.model

data class Command(
    var powerOn: Boolean = false,
    var mode: Mode = Mode.MANUAL,
    var manualTemperature: Int = 19,
)
