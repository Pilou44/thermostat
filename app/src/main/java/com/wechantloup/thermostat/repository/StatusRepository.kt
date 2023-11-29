package com.wechantloup.thermostat.repository

import com.wechantloup.thermostat.provider.DbProvider
import com.wechantloup.thermostat.provider.DbProvider.getAll
import com.wechantloup.thermostat.provider.DbProvider.subscribe
import com.wechantloup.thermostat.model.Status
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

object StatusRepository {

    private const val TAG = "StatusRepository"

    suspend fun getStatuses(): List<Status> {
        return DbProvider
            .statusRef
            .getAll<DbStatus>()
            .mapNotNull {
                it.second?.toStatus(it.first)
            }
    }

    fun subscribe(deviceId: String): Flow<Status> {
        val ref = DbProvider.statusRef.child(deviceId)
        return ref.subscribe(DbStatus::class.java).map { it.toStatus(deviceId) }
    }

    private fun Status.toDbStatus(): Pair<String, DbStatus> {
        val dbStatus = DbStatus(temperature, on)
        return deviceId to dbStatus
    }

    private fun DbStatus.toStatus(id: String): Status {
        return Status(id, temperature, on)
    }

    private data class DbStatus(
        val temperature: Float = 0f,
        val on: Boolean = false,
    )
}
