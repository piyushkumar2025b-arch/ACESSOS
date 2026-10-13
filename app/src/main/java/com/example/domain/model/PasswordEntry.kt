package com.example.domain.model

data class PasswordEntry(
    val id: Long = 0,
    val title: String,
    val username: String,
    val password: String,
    val category: String = "General",
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
