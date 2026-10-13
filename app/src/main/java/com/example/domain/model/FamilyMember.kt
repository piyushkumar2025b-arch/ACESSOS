package com.example.domain.model

data class FamilyMember(
    val id: String = "",
    val name: String = "",
    val relationship: String = "",       // e.g. "Son", "Daughter", "Doctor", "Caregiver"
    val phoneNumber: String = "",
    val isPrimaryEmergencyContact: Boolean = false,
    val avatarInitials: String = "",
    val avatarColorHex: Long = 0xFF3D5A80,
    val fcmToken: String? = null
)

data class FamilyGroup(
    val familyId: String = "FAM-CARE-01",
    val groupName: String = "Family Care Circle",
    val inviteCode: String = "CARE01",
    val primaryElderName: String = "Authorized Elder",
    val isLinked: Boolean = true,
    val lastSyncedTimestamp: Long = System.currentTimeMillis(),
    val totalMembersCount: Int = 1
)

data class SosAlert(
    val id: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val latitude: Double? = null,
    val longitude: Double? = null,
    val address: String = "Location Unavailable",
    val triggeredBy: String = "Authorized User",
    val status: String = "SENT_TO_FAMILY", // "SENT_TO_FAMILY", "LOCATION_UNAVAILABLE", "DISPATCH_FAILED"
    val isRealGps: Boolean = false
) {
    val googleMapsUrl: String? get() = if (latitude != null && longitude != null) "https://maps.google.com/?q=$latitude,$longitude" else null
}
