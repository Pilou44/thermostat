package com.wechantloup.thermostat.usecase

import com.wechantloup.thermostat.repository.UserRepository

class IsUserAuthorizedUseCase {

    suspend fun execute(uid: String): Boolean {
        return UserRepository.getUser(uid).authorized
    }
}
