package com.example.domain.usecase

import com.example.domain.model.FamilyGroup
import com.example.domain.model.FamilyMember
import com.example.domain.model.SosAlert
import com.example.domain.repository.FamilyRepository
import kotlinx.coroutines.flow.Flow

class FamilyUseCases(
    private val familyRepository: FamilyRepository
) {
    fun getFamilyMembers(): Flow<List<FamilyMember>> = familyRepository.getFamilyMembers()

    fun getFamilyGroup(): Flow<FamilyGroup> = familyRepository.getFamilyGroup()

    fun getSosAlertHistory(): Flow<List<SosAlert>> = familyRepository.getSosAlertHistory()

    suspend fun addFamilyMember(member: FamilyMember): Boolean = familyRepository.addFamilyMember(member)

    suspend fun deleteFamilyMember(id: String): Boolean = familyRepository.deleteFamilyMember(id)

    suspend fun createFamilyGroup(groupName: String): FamilyGroup = familyRepository.createFamilyGroup(groupName)

    suspend fun joinFamilyGroup(inviteCode: String): Result<FamilyGroup> = familyRepository.joinFamilyGroup(inviteCode)

    suspend fun triggerEmergencySos(
        triggeredBy: String = "Authorized User",
        lat: Double? = null,
        lng: Double? = null,
        address: String = "Location Unavailable",
        isRealGps: Boolean = false
    ): SosAlert {
        return if (familyRepository is com.example.data.repository.FamilyRepositoryImpl) {
            familyRepository.triggerSosAlertWithLocation(
                triggeredBy = triggeredBy,
                lat = lat,
                lng = lng,
                address = address,
                isRealGps = isRealGps
            )
        } else {
            familyRepository.triggerSosAlert(triggeredBy)
        }
    }
}
