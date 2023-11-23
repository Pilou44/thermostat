package com.wechantloup.thermostat.usecase

import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.database.database
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class RoomSelectionUseCase {

    private val database = Firebase
        .database("https://thermostat-4211f-default-rtdb.europe-west1.firebasedatabase.app/")
        .reference
    private val statusChild = database.child(STATUS_CHILD)

    suspend fun getRooms(): List<String> = withContext(Dispatchers.IO) {
        suspendCoroutine { cont ->
            statusChild.get().addOnSuccessListener { snapshot ->
                cont.resume(snapshot.children.mapNotNull { it.key })
            }.addOnFailureListener {
                // ToDo Handle error
                Log.e(TAG, "Error getting data", it)
                cont.resumeWithException(it)
            }
        }
    }

    companion object {
        private const val TAG = "RoomSelectionUseCase"
        private const val STATUS_CHILD = "statuses"
    }
}
