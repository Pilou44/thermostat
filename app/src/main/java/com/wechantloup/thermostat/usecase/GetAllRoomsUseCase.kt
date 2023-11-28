package com.wechantloup.thermostat.usecase

import com.wechantloup.thermostat.model.Device
import com.wechantloup.thermostat.repository.DeviceRepository
import com.wechantloup.thermostat.repository.StatusRepository

class GetAllRoomsUseCase {

    suspend fun execute(): List<Device> {
        val deviceIds = StatusRepository.getStatuses().map { it.deviceId }
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
}
