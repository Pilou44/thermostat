package com.wechantloup.thermostat.usecase

import android.util.Log
import com.wechantloup.thermostat.model.Command
import com.wechantloup.thermostat.model.Mode
import com.wechantloup.thermostat.model.Status
import com.wechantloup.thermostat.repository.CommandRepository
import com.wechantloup.thermostat.repository.StatusRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

internal class ThermostatUseCase {

    private var roomId: String? = null

    suspend fun setRoomId(roomId: String) {
        this.roomId = roomId
        checkExistingCommand()
    }

    fun subscribeToCommands(): Flow<Command> {
        val roomId = requireNotNull(roomId)
        return CommandRepository.subscribe(roomId)
    }

    fun subscribeToStatuses(): Flow<Status> {
        Log.d("TEST", "roomId = $roomId")
        val roomId = requireNotNull(roomId)
        return StatusRepository.subscribe(roomId)
    }

    fun setManualTemperature(temperature: Int) {
        val roomId = roomId ?: return
        CommandRepository.setManualTemperature(roomId, temperature)
    }

    fun setPowered(on: Boolean) {
        val roomId = roomId ?: return
        CommandRepository.setPowered(roomId, on)
    }

    fun setMode(mode: Mode) {
        val roomId = roomId ?: return
        CommandRepository.setMode(roomId, mode)
    }

    private suspend fun checkExistingCommand() {
        val roomId = roomId ?: return

        val existingValue = CommandRepository.getCommand(roomId)
        if (existingValue == null) {
            CommandRepository.setCommand(roomId, Command())
        }
    }

}

interface CommandListener {
    fun onCommandReceived(command: Command)
}

interface StatusListener {
    fun onStatusReceived(status: Status)
}
