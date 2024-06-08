package com.example.studihelp.ui.Timetable

data class TimetableItem(
    val id: String = "",
    val name: String = "",
    val room: String = "",
    val startTime: String = "",
    val endTime: String = "",
    val dayOfWeek: String = "",
    val color: String = "",
    val isHeader: Boolean = false
) {
    constructor() : this("", "", "", "", "", "", "")

    fun getDayOfWeekIndex(): Int {
        return when (dayOfWeek) {
            "Monday" -> 0
            "Tuesday" -> 1
            "Wednesday" -> 2
            "Thursday" -> 3
            "Friday" -> 4
            "Saturday" -> 5
            "Sunday" -> 6
            else -> -1
        }
    }
}
