package com.wechantloup.thermostat.repository

import com.wechantloup.thermostat.provider.DbProvider
import com.wechantloup.thermostat.provider.DbProvider.getValue
import com.wechantloup.thermostat.model.User

object UserRepository {

    suspend fun getUser(uid: String): User {
        val authorized = DbProvider.userRef.child(uid).getValue<Boolean>() ?: false
        return User(uid, authorized)
    }

}
