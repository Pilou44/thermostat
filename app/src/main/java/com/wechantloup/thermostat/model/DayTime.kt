package com.wechantloup.thermostat.model

import kotlinx.collections.immutable.ImmutableList

data class DayTime(
    val hours: ImmutableList<Mode>
) {
    init {
        require(hours.size == 24)
    }

    enum class Mode {
        DAY, NIGHT,
    }
}
