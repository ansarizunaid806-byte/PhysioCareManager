package com.physiocare.manager.util

import android.content.Context
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.physiocare.manager.data.local.entity.PatientEntity
import com.physiocare.manager.data.local.entity.PaymentEntity
import com.physiocare.manager.data.local.entity.SessionEntity
import com.physiocare.manager.data.repository.PatientRepository
import com.physiocare.manager.data.repository.PaymentRepository
import com.physiocare.manager.data.repository.SessionRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

data class BackupData(
    val patients: List<PatientEntity>,
    val sessions: List<SessionEntity>,
    val payments: List<PaymentEntity>,
    val version: Int = 1,
    val timestamp: Long = System.currentTimeMillis()
)

class BackupManager(
    private val context: Context,
    private val patientRepository: PatientRepository,
    private val sessionRepository: SessionRepository,
    private val paymentRepository: PaymentRepository
) {
    private val gson: Gson = GsonBuilder().setPrettyPrinting().create()

    suspend fun createBackup(): File = withContext(Dispatchers.IO) {
        val patients = patientRepository.getAllPatientsOnce()
        val sessions = sessionRepository.getAllSessionsOnce()
        val payments = paymentRepository.getAllPaymentsOnce()

        val backupData = BackupData(patients, sessions, payments)
        val json = gson.toJson(backupData)

        val backupDir = File(context.filesDir, "backup")
        if (!backupDir.exists()) backupDir.mkdirs()

        val backupFile = File(backupDir, "physiocare_backup_${System.currentTimeMillis()}.json")
        backupFile.writeText(json)
        backupFile
    }

    suspend fun restoreBackup(file: File): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val json = file.readText()
            val backupData = gson.fromJson(json, BackupData::class.java)

            // Clear existing data would be handled by the ViewModel
            for (patient in backupData.patients) {
                patientRepository.insert(patient)
            }
            for (session in backupData.sessions) {
                sessionRepository.insert(session)
            }
            for (payment in backupData.payments) {
                paymentRepository.insert(payment)
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
