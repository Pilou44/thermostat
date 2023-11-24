package com.wechantloup.thermostat.model

import android.content.Context
import com.wechantloup.thermostat.R

data class KnownSwitch(
    val switch: Switch,
    val usedBy: CommandDevice?,
) {
    fun getLabel(context: Context): String {
        if (usedBy == null) {
            return context.getString(R.string.unused_switch_label, switch.address)
        }

        return context.getString(R.string.used_switch_label, switch.address, usedBy.getLabel())
    }
}
