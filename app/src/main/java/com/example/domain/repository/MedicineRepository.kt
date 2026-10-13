package com.example.domain.repository

import com.example.domain.model.Medicine
import com.example.domain.model.MedicineLog
import kotlinx.coroutines.flow.Flow

interface MedicineRepository {
    fun getMedicines(): Flow<List<Medicine>>
    fun getMedicineLogs(): Flow<List<MedicineLog>>
    suspend fun markAsTaken(medicineId: String, takenBy: String): Boolean
    suspend fun markAsUntaken(medicineId: String): Boolean
    suspend fun addMedicine(medicine: Medicine): Boolean
    suspend fun deleteMedicine(medicineId: String): Boolean
}
