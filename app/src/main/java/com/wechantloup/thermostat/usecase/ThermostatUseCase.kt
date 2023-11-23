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

internal class ThermostatUseCase(
    private val commandListener: CommandListener,
    private val statusListener: StatusListener,
) {

    private val database = Firebase
        .database("https://thermostat-4211f-default-rtdb.europe-west1.firebasedatabase.app/")
        .reference

    private val commandChild = database.child(COMMAND_CHILD)
    private val statusChild = database.child(STATUS_CHILD)

    private var roomId: String? = null
    private var fbCommandListener: ValueEventListener? = null
    private var fbStatusListener: ValueEventListener? = null

    fun setRoomId(roomId: String) {
        this.roomId = roomId
        checkExistingCommand()
        setCommandListener()
        setStatusListener()
    }

    fun setManualTemperature(temperature: Int) {
        val roomId = roomId ?: return
        commandChild.child(roomId).child("manualTemperature").setValue(temperature)
    }

    fun setPowered(on: Boolean) {
        val roomId = roomId ?: return
        commandChild.child(roomId).child("powerOn").setValue(on)
    }

    fun setMode(mode: Mode) {
        val roomId = roomId ?: return
        commandChild.child(roomId).child("mode").setValue(mode)
    }

    private fun setCommand(command: Command) {
        val roomId = roomId ?: return
        commandChild.child(roomId).setValue(command)
    }

    private fun checkExistingCommand() {
        val roomId = roomId ?: return

        commandChild.child(roomId).get().addOnSuccessListener {
            val existingValue = it.value
            if (existingValue == null) {
                setCommand(Command())
            }
        }.addOnFailureListener {
            // ToDo Handle error
            Log.e(TAG, "Error getting data", it)
        }
    }

    private fun setCommandListener() {
        val roomId = roomId ?: return

        val ref = commandChild.child(roomId)

        fbCommandListener?.let {
            ref.removeEventListener(it)
        }

        val fbListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val value = dataSnapshot.getValue<Command>() ?: return
                commandListener.onCommandReceived(value)
            }

            override fun onCancelled(error: DatabaseError) {
                // ToDo Handle error
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException())
            }
        }.also {
            fbCommandListener = it
        }
        Log.i(TAG, "Add value listener")
        ref.addValueEventListener(fbListener)
    }

    private fun setStatusListener() {
        val roomId = roomId ?: return

        val ref = statusChild.child(roomId)

        fbStatusListener?.let {
            ref.removeEventListener(it)
        }

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
        }.also {
            fbStatusListener = it
        }
        Log.i(TAG, "Add value listener")
        ref.addValueEventListener(fbListener)
    }

    companion object {
        private const val TAG = "ThermostatUseCase"

        private const val COMMAND_CHILD = "commands"
        private const val STATUS_CHILD = "statuses"
    }
}

interface CommandListener {
    fun onCommandReceived(command: Command)
}

interface StatusListener {
    fun onStatusReceived(status: Status)
}
