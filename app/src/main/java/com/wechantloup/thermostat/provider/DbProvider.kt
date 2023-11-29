package com.wechantloup.thermostat.provider

import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database
import com.google.firebase.database.getValue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
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

    suspend inline fun <reified T> DatabaseReference.getAll(): List<Pair<String, T?>> = withContext(Dispatchers.IO) {
        suspendCoroutine { cont ->
            get().addOnSuccessListener { snapshot ->
                cont.resume(snapshot.children.mapNotNull { requireNotNull(it.key) to it.getValue<T>() })
            }.addOnFailureListener {
                Log.e(TAG, "Error getting data", it)
                cont.resumeWithException(it)
            }
        }
    }

    suspend inline fun <reified T> DatabaseReference.set(value: T?) = withContext(Dispatchers.IO) {
        suspendCoroutine { cont ->
            setValue(value).addOnSuccessListener {
                cont.resume(Unit)
            }.addOnFailureListener {
                Log.e(TAG, "Error setting data", it)
                cont.resumeWithException(it)
            }
        }
    }

    suspend fun DatabaseReference.remove() = withContext(Dispatchers.IO) {
        suspendCoroutine { cont ->
            removeValue().addOnSuccessListener {
                cont.resume(Unit)
            }.addOnFailureListener {
                Log.e(TAG, "Error removing data", it)
                cont.resumeWithException(it)
            }
        }
    }

    inline fun <reified T> DatabaseReference.subscribe(): Flow<T> = subscribe(T::class.java)

    fun <T> DatabaseReference.subscribe(dataType: Class<T>): Flow<T> = callbackFlow {
        Log.i(TAG, "Subscribe to ${dataType.simpleName}")
        val listener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                Log.i(TAG, "Data received for ${dataType.simpleName}")
                val value = dataSnapshot.getValue(dataType) ?: return
                trySend(value)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.w(TAG, "Cancel subscription for ${dataType.simpleName}")
                cancel()
            }
        }
        addValueEventListener(listener)
        awaitClose {
            Log.i(TAG, "Unsubscribe from ${dataType.simpleName}")
            removeEventListener(listener)
        }
    }
}
