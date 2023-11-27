package com.wechantloup.thermostat.usecase

import com.wechantloup.thermostat.repository.SwitchRepository
import com.wechantloup.thermostat.model.KnownSwitch
import com.wechantloup.thermostat.repository.CommandDeviceRepository

class GetKnownSwitchesUseCase {

    suspend fun execute(excludingDeviceId: String? = null): List<KnownSwitch> {
        val switches = SwitchRepository.getAllSwitches()
        val filteredSwitches = if (excludingDeviceId == null) {
            switches
        } else {
            switches.filter { it.pairedDeviceId != excludingDeviceId }
        }

        val devices = CommandDeviceRepository.getAllCommandDevices()

        return filteredSwitches.map { switch ->
            KnownSwitch(
                switch,
                devices.firstOrNull { it.id == switch.pairedDeviceId },
            )
        }
    }
}
