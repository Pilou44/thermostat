package com.wechantloup.provider

import android.media.MediaPlayer.OnCompletionListener
import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.database
import com.google.firebase.database.getValue
import com.wechantloup.thermostat.model.CommandDevice
import com.wechantloup.thermostat.usecase.AuthenticationUseCase
import com.wechantloup.thermostat.usecase.RoomSelectionUseCase
import com.wechantloup.thermostat.usecase.SettingsUseCase
import com.wechantloup.thermostat.usecase.ThermostatUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

object DbProvider {
    const val TAG = "DbProvider"

    private const val URL = "https://thermostat-4211f-default-rtdb.europe-west1.firebasedatabase.app/"

    private const val STATUS_CHILD = "statuses"
    private const val USER_CHILD = "users"
    private const val COMMAND_CHILD = "commands"
    private const val DEVICE_CHILD = "devices"
    private const val SWITCH_CHILD = "switches"

    private val databaseRef = Firebase.database(URL).reference

    val statusRef = databaseRef.child(STATUS_CHILD)
    val userRef = databaseRef.child(USER_CHILD)
    val commandRef = databaseRef.child(COMMAND_CHILD)
    val deviceRef = databaseRef.child(DEVICE_CHILD)
    val switchRef = databaseRef.child(SWITCH_CHILD)

    suspend inline fun <reified T> DatabaseReference.getValue(): T? = withContext(Dispatchers.IO) {
        suspendCoroutine { cont ->
            get().addOnSuccessListener { snapshot ->
                val value = snapshot.getValue<T>()
                cont.resume(value)
            }.addOnFailureListener {
                Log.e(TAG, "Error getting data", it)
                cont.resumeWithException(it)
            }
        }
    }

    suspend fun <T: Any> DatabaseReference.setValueWithCb(value: T, cb: () -> Unit) = withContext(Dispatchers.IO) {
        suspendCoroutine { cont ->
            setValue(
                value,
            ) { error, ref ->
                // ToDo Handle error
                cont.resume(cb())
            }
        }
    }

    suspend inline fun <reified T> DatabaseReference.getAll(): List<Pair<String, T?>> = withContext(Dispatchers.IO) {
        suspendCoroutine { cont ->
            get().addOnSuccessListener { snapshot ->
                cont.resume(snapshot.children.mapNotNull { requireNotNull(it.key) to it.getValue<T>() })
            }.addOnFailureListener {
                // ToDo Handle error
                Log.e(TAG, "Error getting data", it)
                cont.resumeWithException(it)
            }
        }
    }

    suspend inline fun <reified T> DatabaseReference.getAllValues(): List<T> {
        return getAll<T>().mapNotNull { it.second }
    }

    suspend fun DatabaseReference.getAllKeys(): List<String> {
        return getAll<Any>().map { it.first }
    }
}
