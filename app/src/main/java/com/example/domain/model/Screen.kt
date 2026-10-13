package com.example.domain.model

sealed class Screen(val title: String) {
    object Lock : Screen("Locked")
    object Home : Screen("Home")
    object Medicine : Screen("Medicine Reminders")
    object Password : Screen("Password Manager")
    object Family : Screen("Call Family")
    object Sos : Screen("Emergency SOS")
}
