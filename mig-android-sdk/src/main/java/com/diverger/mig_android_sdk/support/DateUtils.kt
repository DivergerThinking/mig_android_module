package com.diverger.mig_android_sdk.support

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

fun isToday(dateString: String?, dateFormat: String = "yyyy-MM-dd"): Boolean {
    if (dateString.isNullOrBlank()) return false

    val sdf = SimpleDateFormat(dateFormat, Locale.getDefault())
    return try {
        val parsedDate = sdf.parse(dateString) ?: return false

        val calParsed = Calendar.getInstance().apply { time = parsedDate }
        val calToday = Calendar.getInstance()

        calParsed.get(Calendar.YEAR) == calToday.get(Calendar.YEAR) &&
                calParsed.get(Calendar.DAY_OF_YEAR) == calToday.get(Calendar.DAY_OF_YEAR)
    } catch (e: Exception) {
        false
    }
}

fun Date.toUIDateString(): String {
    val formatter = SimpleDateFormat.getDateInstance(SimpleDateFormat.FULL, Locale.getDefault())
    formatter.timeZone = TimeZone.getTimeZone("GMT")
    return formatter.format(this)
}

fun dateFromString(date: String, format: String = "yyyy-MM-dd"): Date? {
    val formatter = SimpleDateFormat(format, Locale.getDefault())
    return formatter.parse(date)
}
