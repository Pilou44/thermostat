package com.wechantloup.thermostat.repository

import com.wechantloup.provider.DbProvider
import com.wechantloup.provider.DbProvider.getAll
import com.wechantloup.thermostat.model.CommandDevice

object CommandDeviceRepository {

    suspend fun getAllCommandDevices(): List<CommandDevice> {
        return DbProvider
            .deviceRef
            .getAll<String>()
            .map {
                CommandDevice(
                    id = it.first,
                    name = it.second.orEmpty(),
                )
            }
    }
}
