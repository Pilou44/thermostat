package com.wechantloup.thermostat.usecase

import com.wechantloup.provider.DbProvider
import com.wechantloup.provider.DbProvider.getAllValues
import com.wechantloup.provider.DbProvider.getValue
import com.wechantloup.provider.DbProvider.setValueWithCb
import com.wechantloup.thermostat.model.CommandDevice
import com.wechantloup.thermostat.model.Switch

class SettingsUseCase {
    suspend fun getDevice(id: String): CommandDevice {
        val name = DbProvider.deviceRef.child(id).getValue<String>()
        return CommandDevice(id = id, name = name.orEmpty())
    }

    suspend fun saveDevice(device: CommandDevice, cb: () -> Unit) {
        DbProvider.deviceRef.child(device.id).setValueWithCb(
            value = device.name,
            cb = cb,
        )
    }

    suspend fun getAllSwitches(): List<Switch> {
        return DbProvider.switchRef.getAllValues()
    }

    suspend fun getPairedSwitches(id: String): List<Switch> {
        return getAllSwitches().filter { it.pairedDeviceId == id }
    }

    companion object {
        private const val TAG = "SettingsUseCase"
    }
}
