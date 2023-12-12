package com.wechantloup.thermostat.utils

import java.util.Locale

fun String.toFirstLetterUpperCase(): String =
    replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
