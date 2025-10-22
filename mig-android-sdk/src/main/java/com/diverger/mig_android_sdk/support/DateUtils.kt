package com.diverger.mig_android_sdk.support

import android.os.Build
import androidx.annotation.RequiresApi
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Calendar
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
    } catch (e: Exception) {
        false
    }
}
