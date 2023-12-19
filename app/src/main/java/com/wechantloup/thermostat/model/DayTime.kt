package com.wechantloup.thermostat.model

import androidx.annotation.Keep
import kotlinx.collections.immutable.ImmutableList

@Keep
data class DayTime(
    val hours: ImmutableList<Mode>
) {
    init {
        require(hours.size == 24)
    }

    @Keep
    enum class Mode {
        DAY, NIGHT,
    }
}
