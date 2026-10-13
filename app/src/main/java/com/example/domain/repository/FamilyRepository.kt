package com.example.domain.repository

import com.example.domain.model.FamilyGroup
import com.example.domain.model.FamilyMember
import com.example.domain.model.SosAlert
import kotlinx.coroutines.flow.Flow

interface FamilyRepository {
    fun getFamilyMembers(): Flow<List<FamilyMember>>
    fun getFamilyGroup(): Flow<FamilyGroup>
    fun getSosAlertHistory(): Flow<List<SosAlert>>
    suspend fun addFamilyMember(member: FamilyMember): Boolean
    suspend fun deleteFamilyMember(id: String): Boolean
    suspend fun createFamilyGroup(groupName: String): FamilyGroup
    suspend fun joinFamilyGroup(inviteCode: String): Result<FamilyGroup>
    suspend fun triggerSosAlert(triggeredBy: String): SosAlert
}

