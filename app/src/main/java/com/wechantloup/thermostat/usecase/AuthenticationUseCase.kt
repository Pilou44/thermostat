package com.wechantloup.thermostat.usecase

import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.database.database
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class AuthenticationUseCase {

    private val database = Firebase
        .database("https://thermostat-4211f-default-rtdb.europe-west1.firebasedatabase.app/")
        .reference

    private val userChild = database.child(USER_CHILD)

    suspend fun isUserAllowed(uid: String): Boolean = withContext(Dispatchers.IO) {
        suspendCoroutine { cont ->
            userChild.child(uid).get().addOnSuccessListener {
                val allowed: Boolean = it.getValue(Boolean::class.java) ?: false
                cont.resume(allowed)
            }.addOnFailureListener {
                // ToDo Handle error
                Log.e(TAG, "Error getting data", it)
                cont.resume(false)
            }
        }
    }

    companion object {
        private const val TAG = "AuthenticationUseCase"

        private const val USER_CHILD = "users"
    }
}
