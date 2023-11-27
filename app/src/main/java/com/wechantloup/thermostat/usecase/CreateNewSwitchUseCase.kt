package com.wechantloup.thermostat.usecase

import com.wechantloup.thermostat.model.Switch
import com.wechantloup.thermostat.repository.SwitchRepository

class CreateNewSwitchUseCase {
    suspend fun execute(newSwitch: Switch): Boolean {
        val switches = SwitchRepository.getAllSwitches()
        val knownIps = switches.map { it.address }.toSet()

        // IP already exists
        if (knownIps.contains(newSwitch.address)) return false

        SwitchRepository.add(newSwitch)
        return true
    }
}
