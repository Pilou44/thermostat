package com.wechantloup.thermostat.usecase

import android.util.Log
import com.wechantloup.provider.DbProvider
import com.wechantloup.provider.DbProvider.getAll
import com.wechantloup.provider.DbProvider.getAllKeys
import com.wechantloup.thermostat.model.Status
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class RoomSelectionUseCase {

    suspend fun getRooms(): List<String> {
        return DbProvider.statusRef.getAllKeys()
    }

    companion object {
        private const val TAG = "RoomSelectionUseCase"
    }
}
