package com.wechantloup.thermostat.repository

import com.wechantloup.provider.DbProvider
import com.wechantloup.provider.DbProvider.getAll
import com.wechantloup.provider.DbProvider.getValue
import com.wechantloup.provider.DbProvider.set
import com.wechantloup.thermostat.model.Device

object DeviceRepository {

    suspend fun getAllDevices(): List<Device> {
        return DbProvider
            .deviceRef
            .getAll<String>()
            .map {
                Device(
                    id = it.first,
                    name = it.second.orEmpty(),
                )
            }
    }

    suspend fun getDevice(id: String): Device {
        val name = DbProvider
            .deviceRef
            .child(id)
            .getValue<String>()
        return Device(
            id = id,
            name = name.orEmpty(),
        )
    }

    suspend fun add(device: Device) {
        DbProvider
            .deviceRef
            .child(device.id)
            .set(device.name)
    }
}
