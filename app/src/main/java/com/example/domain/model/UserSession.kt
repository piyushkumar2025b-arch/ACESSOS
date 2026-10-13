package com.example.domain.model

data class UserSession(
    val isUnlocked: Boolean = false,
    val rfidUid: String? = null,
    val userName: String = "Authorized User",
    val unlockedTimestamp: Long = 0L,
    val lastActivityTimestamp: Long = 0L,
    val activeFamilyId: String = "FAM-CARE-01"
)
