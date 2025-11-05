package com.diverger.mig_android_sdk.support

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

fun isToday(dateString: String?, dateFormat: String = "yyyy-MM-dd"): Boolean {
    if (dateString.isNullOrBlank()) return false

    val sdf = SimpleDateFormat(dateFormat, Locale.getDefault())
    return try {
        val parsedDate = sdf.parse(dateString) ?: return false

        val calParsed = Calendar.getInstance().apply { time = parsedDate }
        val calToday = Calendar.getInstance()

        calParsed.get(Calendar.YEAR) == calToday.get(Calendar.YEAR) &&
                calParsed.get(Calendar.DAY_OF_YEAR) == calToday.get(Calendar.DAY_OF_YEAR)
    } catch (_: Exception) {
        false
    }
}

fun Date.toUIDateString(): String {
    val formatter = SimpleDateFormat.getDateInstance(SimpleDateFormat.FULL, Locale.getDefault())
    return formatter.format(this)
}

fun dateFromString(date: String, format: String = "yyyy-MM-dd"): Date? {
    val formatter = SimpleDateFormat(format, Locale.getDefault())
    return formatter.parse(date)
}

fun Date.isDateOlderThanToday(): Boolean {
    val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    val normalizedDate1 = format.parse(format.format(this))
    val normalizedDate2 = format.parse(format.format(Date()))

    return normalizedDate1?.after(normalizedDate2) == true
}

fun getCurrentYear(): Int {
    val calendar = Calendar.getInstance()
    return calendar.get(Calendar.YEAR)
}