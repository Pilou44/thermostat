package com.wechantloup.thermostat.repository

import com.wechantloup.thermostat.model.Thermostat
import com.wechantloup.thermostat.provider.DbProvider
import com.wechantloup.thermostat.provider.DbProvider.getAll
import com.wechantloup.thermostat.provider.DbProvider.subscribe
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

object ThermostatRepository {

    private const val TAG = "StatusRepository"

    suspend fun getThermostats(): List<Thermostat> {
        return DbProvider
            .thermostatRef
            .getAll<DbThermostat>()
            .mapNotNull {
                it.second?.toThermostat(it.first)
            }
    }

    fun subscribe(deviceId: String): Flow<Thermostat> {
        val ref = DbProvider.thermostatRef.child(deviceId)
        return ref.subscribe(DbThermostat::class.java).map { it.toThermostat(deviceId) }
    }

    private fun Thermostat.toDbThermostat(): Pair<String, DbThermostat> {
        val dbThermostat = DbThermostat(temperature, on, time)
        return deviceId to dbThermostat
    }

    private fun DbThermostat.toThermostat(id: String): Thermostat {
        return Thermostat(id, temperature, on, time)
    }

    private data class DbThermostat(
        val temperature: Float = 0f,
        val on: Boolean = false,
        val time: String = "",
    )
}
