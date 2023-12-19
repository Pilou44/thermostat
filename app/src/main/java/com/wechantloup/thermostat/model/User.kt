package com.wechantloup.thermostat.model

import androidx.annotation.Keep

@Keep
data class User(
    val id: String,
    val authorized: Boolean,
)
