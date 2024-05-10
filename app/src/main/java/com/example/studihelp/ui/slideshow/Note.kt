package com.example.studihelp.ui.slideshow

data class Note(
    val title: String,
    val content: String,
    val timestamp: Long,
    var isPinned: Boolean,
    val key: String
)

