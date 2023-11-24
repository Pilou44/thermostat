package com.wechantloup.thermostat.repository

import com.wechantloup.provider.DbProvider
import com.wechantloup.provider.DbProvider.getAll
import com.wechantloup.thermostat.model.Switch

class SwitchRepository {

    suspend fun getAllSwitches(): List<Switch> {
        return DbProvider
            .switchRef
            .getAll<DbSwitch>()
            .map {
                val dbSwitch = requireNotNull(it.second)
                Switch(
                    address = it.first,
                    type = dbSwitch.type,
                    pairedDeviceId = dbSwitch.pairedDeviceId,
                )
            }
    }

    private data class DbSwitch(
        val type: Switch.Type,
        val pairedDeviceId: String?,
    )
}
