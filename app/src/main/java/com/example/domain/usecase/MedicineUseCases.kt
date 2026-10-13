package com.example.domain.usecase

import com.example.domain.model.Medicine
import com.example.domain.model.MedicineLog
import com.example.domain.repository.MedicineRepository
import kotlinx.coroutines.flow.Flow

class MedicineUseCases(
    private val medicineRepository: MedicineRepository
) {
    fun getMedicines(): Flow<List<Medicine>> = medicineRepository.getMedicines()

    fun getMedicineLogs(): Flow<List<MedicineLog>> = medicineRepository.getMedicineLogs()

    suspend fun markAsTaken(medicineId: String, takenBy: String = "Mrs. Sharma"): Boolean {
        return medicineRepository.markAsTaken(medicineId, takenBy)
    }

    suspend fun markAsUntaken(medicineId: String): Boolean {
        return medicineRepository.markAsUntaken(medicineId)
    }

    suspend fun addMedicine(medicine: Medicine): Boolean {
        return medicineRepository.addMedicine(medicine)
    }

    suspend fun deleteMedicine(medicineId: String): Boolean {
        return medicineRepository.deleteMedicine(medicineId)
    }
}
