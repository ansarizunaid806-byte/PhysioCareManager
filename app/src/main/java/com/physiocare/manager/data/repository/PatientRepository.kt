package com.physiocare.manager.data.repository

import com.physiocare.manager.data.local.dao.PatientDao
import com.physiocare.manager.data.local.entity.PatientEntity
import kotlinx.coroutines.flow.Flow

class PatientRepository(private val patientDao: PatientDao) {

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

    suspend fun insert(patient: PatientEntity): Long = patientDao.insert(patient)

    suspend fun update(patient: PatientEntity) = patientDao.update(patient)

    suspend fun delete(patient: PatientEntity) = patientDao.delete(patient)

    suspend fun getPatientCount(): Int = patientDao.getPatientCount()

    suspend fun getAllPatientsOnce(): List<PatientEntity> = patientDao.getAllPatientsOnce()
}
