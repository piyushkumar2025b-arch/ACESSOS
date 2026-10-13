package com.example.domain.model

data class Medicine(
    val id: String = "",
    val familyId: String = "FAM-CARE-01",
    val name: String = "",
    val time: String = "",               // e.g. "09:00 AM", "02:00 PM", "08:30 PM"
    val dosage: String = "",             // e.g. "1 Tablet", "2 Drops", "5 ml Syrup"
    val instructions: String = "",       // e.g. "After Breakfast with water"
    val pillIconType: String = "tablet", // tablet, capsule, drops, syrup
    val isTakenToday: Boolean = false,
    val lastTakenTimestamp: Long? = null,
    val active: Boolean = true
)

data class MedicineLog(
    val id: String = "",
    val medicineId: String = "",
    val medicineName: String = "",
    val dosage: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val status: String = "taken", // "taken", "skipped"
    val loggedBy: String = "Authorized User"
)
