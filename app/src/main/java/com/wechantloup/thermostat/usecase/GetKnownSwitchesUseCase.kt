package com.wechantloup.thermostat.usecase

import com.wechantloup.thermostat.repository.SwitchRepository
import com.wechantloup.thermostat.model.KnownSwitch
import com.wechantloup.thermostat.repository.CommandDeviceRepository

class GetKnownSwitchesUseCase {

    private val switchRepository = SwitchRepository()
    private val deviceRepository = CommandDeviceRepository()

    suspend fun execute(excludingDeviceId: String? = null): List<KnownSwitch> {
        val switches = switchRepository.getAllSwitches()
        val filteredSwitches = if (excludingDeviceId == null) {
            switches
        } else {
            switches.filter { it.pairedDeviceId != excludingDeviceId }
        }

        val devices = deviceRepository.getAllCommandDevices()

        return filteredSwitches.map { switch ->
            KnownSwitch(
                switch,
                devices.firstOrNull { it.id == switch.pairedDeviceId },
            )
        }
    }
}
