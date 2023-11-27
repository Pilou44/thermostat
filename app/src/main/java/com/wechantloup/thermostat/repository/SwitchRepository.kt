package com.wechantloup.thermostat.repository

import com.wechantloup.provider.DbProvider
import com.wechantloup.provider.DbProvider.add
import com.wechantloup.provider.DbProvider.getAll
import com.wechantloup.thermostat.model.Switch
import com.wechantloup.thermostat.model.SwitchType

object SwitchRepository {

    suspend fun getAllSwitches(): List<Switch> {
        return DbProvider
            .switchRef
            .getAll<DbSwitch>()
            .mapNotNull {
                it.second?.toSwitch(it.first)
            }
    }

    suspend fun add(switch: Switch) {
        val (id, dbSwitch) = switch.toDbSwitch()
        DbProvider
            .switchRef
            .child(id)
            .add(dbSwitch)
    }

    private fun Switch.toDbSwitch(): Pair<String, DbSwitch> {
        val id = address.replace(".", "-")
        val dbSwitch = DbSwitch(type, pairedDeviceId)
        return id to dbSwitch
    }

    private fun DbSwitch.toSwitch(id: String): Switch {
        val address = id.replace("-", ".")
        return Switch(address, type, pairedDeviceId)
    }

    private data class DbSwitch(
        val type: SwitchType = SwitchType.values().first(),
        val pairedDeviceId: String? = null,
    )
}
