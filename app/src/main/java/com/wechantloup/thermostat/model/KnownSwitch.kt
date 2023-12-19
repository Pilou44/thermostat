package com.wechantloup.thermostat.model

import android.content.Context
import androidx.annotation.Keep
import com.wechantloup.thermostat.R

@Keep
data class KnownSwitch(
    val switch: Switch,
    val usedBy: Device?,
) {
    fun getLabel(context: Context): String {
        if (usedBy == null) {
            return context.getString(R.string.unused_switch_label, switch.address)
        }

        return context.getString(R.string.used_switch_label, switch.address, usedBy.getLabel())
    }
}
