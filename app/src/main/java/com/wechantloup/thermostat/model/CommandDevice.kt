package com.wechantloup.thermostat.model

data class CommandDevice(
    val id: String,
    val name: String,
) {
    fun getLabel(): String {
        return name.takeIf { it.isNotBlank() } ?: id
    }
}
