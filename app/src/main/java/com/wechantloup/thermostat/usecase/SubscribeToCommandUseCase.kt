package com.wechantloup.thermostat.usecase

import com.wechantloup.thermostat.model.Command
import com.wechantloup.thermostat.repository.CommandRepository
import kotlinx.coroutines.flow.Flow

class SubscribeToCommandUseCase {
    fun execute(id: String): Flow<Command> {
        return CommandRepository.subscribe(id)
    }
}
