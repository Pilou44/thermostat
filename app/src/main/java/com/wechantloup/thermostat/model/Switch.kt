package com.wechantloup.thermostat.model

data class Switch(
    val address: String,
    val type: Type,
    val pairedDeviceId: String?,
) {
    enum class Type {
        SHELLY_PLUS_1,
    }
}
