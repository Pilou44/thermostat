package com.wechantloup.thermostat.repository

import com.wechantloup.provider.DbProvider
import com.wechantloup.provider.DbProvider.getAll
import com.wechantloup.provider.DbProvider.remove
import com.wechantloup.provider.DbProvider.set
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
            .set(dbSwitch)
    }

    suspend fun pair(switch: Switch, deviceId: String) {
        val (id, _) = switch.toDbSwitch()
        DbProvider
            .switchRef
            .child(id)
            .child("pairedDeviceId")
            .set(deviceId)
    }

    suspend fun unPair(switch: Switch) {
        val (id, _) = switch.toDbSwitch()
        DbProvider
            .switchRef
            .child(id)
            .child("pairedDeviceId")
            .set<String>(null)
    }

    suspend fun remove(switch: Switch) {
        val (id, _) = switch.toDbSwitch()
        DbProvider
            .switchRef
            .child(id)
            .remove()
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
