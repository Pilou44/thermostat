package com.wechantloup.thermostat.usecase

import com.wechantloup.thermostat.repository.CommandRepository

class HasCommandsUseCase {
    suspend fun execute(id: String): Boolean {
        val existingValue = CommandRepository.getCommand(id)
        return existingValue != null
    }
}
