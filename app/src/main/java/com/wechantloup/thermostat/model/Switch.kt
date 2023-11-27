package com.wechantloup.thermostat.model

data class Switch(
    val address: String,
    val type: SwitchType,
    val pairedDeviceId: String?,
)
