package com.wechantloup.thermostat.usecase

import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database
import com.google.firebase.database.getValue
import com.wechantloup.thermostat.model.Command
import com.wechantloup.thermostat.model.Mode
import com.wechantloup.thermostat.model.Status
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ThermostatUseCase(
    commandListener: CommandListener,
    statusListener: StatusListener,
) {

    private val database = Firebase
        .database("https://thermostat-4211f-default-rtdb.europe-west1.firebasedatabase.app/")
        .reference

    private val commandChild = database.child(COMMAND_CHILD)
    private val statusChild = database.child(STATUS_CHILD)

    init {
        checkExistingCommand()
        setCommandListener(commandListener)
        setStatusListener(statusListener)
    }

    internal fun setManualTemperature(temperature: Int) {
        commandChild.child(ROOM_ID).child("manualTemperature").setValue(temperature)
    }

    internal fun setPowered(on: Boolean) {
        commandChild.child(ROOM_ID).child("powerOn").setValue(on)
    }

    internal fun setMode(mode: Mode) {
        commandChild.child(ROOM_ID).child("mode").setValue(mode)
    }

    private fun setCommand(command: Command) {
        commandChild.child(ROOM_ID).setValue(command)
    }

    private fun checkExistingCommand() {
        commandChild.child(ROOM_ID).get().addOnSuccessListener {
            val existingValue = it.value
            if (existingValue == null) {
                setCommand(Command())
            }
        }.addOnFailureListener {
            // ToDo Handle error
            Log.e(TAG, "Error getting data", it)
        }
    }

    private fun setCommandListener(commandListener: CommandListener) {
        val fbListener: ValueEventListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val value = dataSnapshot.getValue<Command>() ?: return
                commandListener.onCommandReceived(value)
            }

            override fun onCancelled(error: DatabaseError) {
                // ToDo Handle error
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException())
            }
        }
        Log.i(TAG, "Add value listener")
        commandChild.child(ROOM_ID).addValueEventListener(fbListener)
    }

    private fun setStatusListener(statusListener: StatusListener) {
        val fbListener: ValueEventListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                Log.i(TAG, "New status received")
                val value = dataSnapshot.getValue<Status>() ?: return
                Log.i(TAG, "New status received $value")
                statusListener.onStatusReceived(value)
            }

            override fun onCancelled(error: DatabaseError) {
                // ToDo Handle error
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException())
            }
        }
        Log.i(TAG, "Add value listener")
        statusChild.child(ROOM_ID).addValueEventListener(fbListener)
    }

    companion object {
        private const val TAG = "ThermostatUseCase"

        private const val COMMAND_CHILD = "commands"
        private const val STATUS_CHILD = "statuses"

        private const val ROOM_ID = "room"
    }
}

interface CommandListener {
    fun onCommandReceived(command: Command)
}

interface StatusListener {
    fun onStatusReceived(status: Status)
}
