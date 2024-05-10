package com.example.studihelp.ui.Timetable

data class TimetableItem(
    val id: String = "", // Unikalne ID zajęcia
    val name: String = "",
    val room: String = "",
    val startTime: String = "",
    val endTime: String = "",
    val dayOfWeek: Int = 0, // Dzień tygodnia, np. 1 dla poniedziałku, 2 dla wtorku itd.
    val color: Int = 0 // Kolor zajęcia
)
