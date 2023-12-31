package com.wechantloup.thermostat.repository

import androidx.annotation.Keep
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
        val dbThermostat = DbThermostat(temperature, humidity, on, time)
        return deviceId to dbThermostat
    }

    private fun DbThermostat.toThermostat(id: String): Thermostat {
        return Thermostat(id, temperature, humidity, on, time)
    }

    @Keep
    private data class DbThermostat(
        val temperature: Float = 0f,
        val humidity: Float = Float.NaN,
        val on: Boolean = false,
        val time: String = "",
    )
}
