package com.wechantloup.thermostat.model

data class Command(
    var powerOn: Boolean = false,
    var mode: Mode = Mode.MANUAL,
    var manualTemperature: Int = 19,
    var automaticTemperatureDay: Int = 19,
    var automaticTemperatureNight: Int = 16,
    var automaticTemperatures: List<List<Boolean>> = listOf(
        listOf(false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false),
        listOf(false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false),
        listOf(false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false),
        listOf(false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false),
        listOf(false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false),
        listOf(false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false),
        listOf(false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false),
    )
)
