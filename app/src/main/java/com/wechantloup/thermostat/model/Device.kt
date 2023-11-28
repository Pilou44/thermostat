package com.wechantloup.thermostat.model

data class Device(
    val id: String,
    val name: String,
) {
    fun getLabel(): String {
        return name.takeIf { it.isNotBlank() } ?: id
    }
}
