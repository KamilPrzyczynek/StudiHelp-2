package com.example.studihelp.ui.Drive

data class DriveItem(
    val id: String,
    val topic: String,
    val imageUrl: String = ""
) {
    constructor() : this("", "", "")
}
