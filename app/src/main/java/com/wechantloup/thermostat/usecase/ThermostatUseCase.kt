package com.wechantloup.thermostat.usecase

import com.wechantloup.thermostat.model.Mode
import com.wechantloup.thermostat.repository.CommandRepository

internal class ThermostatUseCase {

    fun setManualTemperature(roomId: String, temperature: Int) {
        CommandRepository.setManualTemperature(roomId, temperature)
    }

    fun setPowered(roomId: String, on: Boolean) {
        CommandRepository.setPowered(roomId, on)
    }

    fun setMode(roomId: String, mode: Mode) {
        CommandRepository.setMode(roomId, mode)
    }

}
