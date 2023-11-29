package com.wechantloup.thermostat.repository

import com.wechantloup.thermostat.provider.DbProvider
import com.wechantloup.thermostat.provider.DbProvider.getAll
import com.wechantloup.thermostat.provider.DbProvider.getValue
import com.wechantloup.thermostat.provider.DbProvider.set
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
