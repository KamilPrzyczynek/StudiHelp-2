package com.example.studihelp.ui.Timetable

data class TimetableItem(
    val id: String = "",
    val name: String = "",
    val room: String = "",
    val startTime: String = "",
    val endTime: String = "",
    val dayOfWeek: Int = 0,
    val color: Int = 0
)
