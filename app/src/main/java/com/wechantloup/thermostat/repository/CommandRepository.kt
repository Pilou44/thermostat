package com.wechantloup.thermostat.repository

import com.wechantloup.thermostat.model.Command
import com.wechantloup.thermostat.model.Mode
import com.wechantloup.thermostat.provider.DbProvider
import com.wechantloup.thermostat.provider.DbProvider.getValue
import com.wechantloup.thermostat.provider.DbProvider.set
import com.wechantloup.thermostat.provider.DbProvider.subscribe
import kotlinx.coroutines.flow.Flow

object CommandRepository {

    private const val TAG = "CommandRepository"

    suspend fun setManualTemperature(deviceId: String, temperature: Int) {
        DbProvider.commandRef.child(deviceId).child("manualTemperature").set(temperature)
    }

    suspend fun setDayTemperature(deviceId: String, temperature: Int) {
        DbProvider.commandRef.child(deviceId).child("automaticTemperatureDay").set(temperature)
    }

    suspend fun setNightTemperature(deviceId: String, temperature: Int) {
        DbProvider.commandRef.child(deviceId).child("automaticTemperatureNight").set(temperature)
    }

    suspend fun setPowered(deviceId: String, on: Boolean) {
        DbProvider.commandRef.child(deviceId).child("powerOn").set(on)
    }

    suspend fun setMode(deviceId: String, mode: Mode) {
        DbProvider.commandRef.child(deviceId).child("mode").set(mode)
    }

    suspend fun getCommand(deviceId: String): Command? {
        return DbProvider
            .commandRef
            .child(deviceId)
            .getValue<Command>()
    }

    suspend fun setCommand(deviceId: String, command: Command) {
        DbProvider.commandRef.child(deviceId).set(command)
    }

    suspend fun setIsDay(deviceId: String, day: Int, hour: Int, isDay: Boolean) {
        DbProvider.commandRef
            .child(deviceId)
            .child("automaticTemperatures")
            .child(day.toString())
            .child(hour.toString())
            .set(isDay)
    }

    fun subscribe(deviceId: String): Flow<Command> {
        val ref = DbProvider.commandRef.child(deviceId)
        return ref.subscribe()
    }
}
