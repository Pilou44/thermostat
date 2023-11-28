package com.wechantloup.thermostat.usecase

import com.wechantloup.thermostat.model.Status
import com.wechantloup.thermostat.repository.StatusRepository
import kotlinx.coroutines.flow.Flow

class SubscribeToStatusUseCase {
    fun execute(id: String): Flow<Status> {
        return StatusRepository.subscribe(id)
    }
}
