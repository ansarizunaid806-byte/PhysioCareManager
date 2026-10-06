package com.physiocare.manager.data.repository

import com.physiocare.manager.data.local.dao.PatientDao
import com.physiocare.manager.data.local.entity.PatientEntity
import com.physiocare.manager.data.sync.CloudSyncManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class PatientRepository(
    private val patientDao: PatientDao,
    private val cloudSyncManager: CloudSyncManager? = null
) {

    fun getAllPatients(): Flow<List<PatientEntity>> = patientDao.getAllPatients()

    fun getPatientsByStatus(status: String): Flow<List<PatientEntity>> =
        patientDao.getPatientsByStatus(status)

    fun searchPatients(query: String): Flow<List<PatientEntity>> =
        patientDao.searchPatients(query)

    fun getPatientById(id: Long): Flow<PatientEntity?> = patientDao.getPatientById(id)

    suspend fun getPatientByIdOnce(id: Long): PatientEntity? = patientDao.getPatientByIdOnce(id)

    fun getPatientsWithDues(): Flow<List<PatientEntity>> = patientDao.getPatientsWithDues()

    fun getFilteredPatients(query: String, statusFilter: String): Flow<List<PatientEntity>> =
        patientDao.getFilteredPatients(query, statusFilter)

    fun getTodaysPatients(startOfDay: Long, endOfDay: Long): Flow<List<PatientEntity>> =
        patientDao.getTodaysPatients(startOfDay, endOfDay)

    fun getPatientsAbsentSince(cutoffDate: Long): Flow<List<PatientEntity>> =
        patientDao.getPatientsAbsentSince(cutoffDate)

    suspend fun insert(patient: PatientEntity): Long {
        val id = patientDao.insert(patient)
        // Sync to cloud in background
        cloudSyncManager?.syncToCloud(listOf(patient), emptyList(), emptyList())
        return id
    }

    suspend fun update(patient: PatientEntity) {
        patientDao.update(patient)
        // Sync to cloud in background
        cloudSyncManager?.syncToCloud(listOf(patient), emptyList(), emptyList())
    }

    suspend fun delete(patient: PatientEntity) {
        patientDao.delete(patient)
        // Delete from cloud
        cloudSyncManager?.deletePatientFromCloud(patient.id)
    }

    suspend fun getPatientCount(): Int = patientDao.getPatientCount()

    suspend fun getAllPatientsOnce(): List<PatientEntity> = patientDao.getAllPatientsOnce()
}
