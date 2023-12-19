package com.wechantloup.thermostat.model

import androidx.annotation.Keep

@Keep
data class Switch(
    val address: String,
    val type: SwitchType,
    val pairedDeviceId: String?,
)
