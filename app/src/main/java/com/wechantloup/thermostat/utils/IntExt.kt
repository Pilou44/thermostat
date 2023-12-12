package com.wechantloup.thermostat.utils

import android.text.format.DateFormat
import java.util.Date
import java.util.GregorianCalendar

fun Int.toDayOfWeek(): String {
    val date = this.dayToDate()
    return DateFormat.format("EEEE", date).toString().toFirstLetterUpperCase()
}

fun Int.toAbbreviatedDayOfWeek(): String {
    val date = this.dayToDate()
    return DateFormat.format("E", date).toString().toFirstLetterUpperCase()
}

private fun Int.dayToDate(): Date {
    if (this > 6) throw IllegalStateException()
    val calendar = GregorianCalendar()
    calendar.set(GregorianCalendar.DAY_OF_WEEK, ((this + 1) % 7) + 1)
    return calendar.time
}
