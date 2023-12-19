package com.wechantloup.thermostat.model

import androidx.annotation.Keep
import com.wechantloup.thermostat.repository.DeviceRepository
import com.wechantloup.thermostat.repository.ThermostatRepository

@Keep
data class Device(
    val id: String,
    val name: String,
) {
    fun getLabel(): String {
        return name.takeIf { it.isNotBlank() } ?: id
    }

    companion object {
        suspend fun getAll(): List<Device> {
            val deviceIds = ThermostatRepository.getThermostats().map { it.deviceId }
            val devices = DeviceRepository.getAllDevices()
            return deviceIds.map { deviceId ->
                Device(
                    id = deviceId,
                    name = devices
                        .firstOrNull { it.id == deviceId }
                        ?.name
                        .orEmpty(),
                )
            }
        }

        suspend fun get(id: String): Device {
            return DeviceRepository.getDevice(id)
        }
    }
}
