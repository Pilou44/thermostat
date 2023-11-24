package com.wechantloup.thermostat.usecase

import android.util.Log
import com.google.firebase.database.getValue
import com.wechantloup.provider.DbProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class AuthenticationUseCase {

    suspend fun isUserAllowed(uid: String): Boolean = withContext(Dispatchers.IO) {
        suspendCoroutine { cont ->
            DbProvider.userRef.child(uid).get().addOnSuccessListener {
                val allowed: Boolean = it.getValue<Boolean>() ?: false
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
    }
}
