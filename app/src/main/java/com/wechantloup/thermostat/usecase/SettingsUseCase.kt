package com.wechantloup.thermostat.usecase

import com.wechantloup.thermostat.model.Device
import com.wechantloup.thermostat.model.Switch
import com.wechantloup.thermostat.repository.DeviceRepository
import com.wechantloup.thermostat.repository.SwitchRepository

class SettingsUseCase {
    suspend fun getDevice(id: String): Device {
        return DeviceRepository.getDevice(id)
    }

    suspend fun saveDevice(device: Device, cb: () -> Unit) {
        DeviceRepository.add(device)
        cb()
    }

    suspend fun getPairedSwitches(id: String): List<Switch> {
        return SwitchRepository.getAllSwitches().filter { it.pairedDeviceId == id }
    }
}
