package com.example.studihelp.ui.Timetable

import java.util.Calendar
import java.util.Locale

object TimeUtil {
    fun getHour(timeInMillis: Long): Int {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = timeInMillis
        return calendar.get(Calendar.HOUR_OF_DAY)
    }

    fun getMinute(timeInMillis: Long): Int {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = timeInMillis
        return calendar.get(Calendar.MINUTE)
    }

    fun getTimeString(hour: Int, minute: Int): String {
        return String.format(Locale.getDefault(), "%02d:%02d", hour, minute)
    }
}
