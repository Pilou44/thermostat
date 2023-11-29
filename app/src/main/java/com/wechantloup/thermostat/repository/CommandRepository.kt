package com.wechantloup.thermostat.repository

import com.wechantloup.thermostat.provider.DbProvider
import com.wechantloup.thermostat.provider.DbProvider.getValue
import com.wechantloup.thermostat.provider.DbProvider.subscribe
import com.wechantloup.thermostat.model.Command
import com.wechantloup.thermostat.model.Mode
import kotlinx.coroutines.flow.Flow

object CommandRepository {

    private const val TAG = "CommandRepository"

    fun setManualTemperature(deviceId: String, temperature: Int) {
        DbProvider.commandRef.child(deviceId).child("manualTemperature").setValue(temperature)
    }

    fun setPowered(deviceId: String, on: Boolean) {
        DbProvider.commandRef.child(deviceId).child("powerOn").setValue(on)
    }

    fun setMode(deviceId: String, mode: Mode) {
        DbProvider.commandRef.child(deviceId).child("mode").setValue(mode)
    }

    suspend fun getCommand(deviceId: String): Command? {
        return DbProvider
            .commandRef
            .child(deviceId)
            .getValue<Command>()
    }

    fun setCommand(deviceId: String, command: Command) {
        DbProvider.commandRef.child(deviceId).setValue(command)
    }

    fun subscribe(deviceId: String): Flow<Command> {
        val ref = DbProvider.commandRef.child(deviceId)
        return ref.subscribe()
    }
}
