package com.physiocare.manager.data.local.dao

import androidx.room.*
import com.physiocare.manager.data.local.entity.PatientEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PatientDao {

    @Query("SELECT * FROM patients ORDER BY updatedAt DESC")
    fun getAllPatients(): Flow<List<PatientEntity>>

    @Query("SELECT * FROM patients WHERE status = :status ORDER BY updatedAt DESC")
    fun getPatientsByStatus(status: String): Flow<List<PatientEntity>>

    @Query("SELECT * FROM patients WHERE fullName LIKE '%' || :query || '%' OR mobileNumber LIKE '%' || :query || '%' OR condition LIKE '%' || :query || '%' ORDER BY updatedAt DESC")
    fun searchPatients(query: String): Flow<List<PatientEntity>>

    @Query("SELECT * FROM patients WHERE id = :id")
    fun getPatientById(id: Long): Flow<PatientEntity?>

    @Query("SELECT * FROM patients WHERE id = :id")
    suspend fun getPatientByIdOnce(id: Long): PatientEntity?

    @Query("""
        SELECT * FROM patients WHERE id IN (
            SELECT DISTINCT patientId FROM sessions WHERE paymentStatus = 'Due' AND status = 'Present'
        ) ORDER BY updatedAt DESC
    """)
    fun getPatientsWithDues(): Flow<List<PatientEntity>>

    @Query("""
        SELECT * FROM patients WHERE 
        (:statusFilter = 'All' OR status = :statusFilter)
        AND (fullName LIKE '%' || :query || '%' OR mobileNumber LIKE '%' || :query || '%' OR condition LIKE '%' || :query || '%')
        ORDER BY updatedAt DESC
    """)
    fun getFilteredPatients(query: String, statusFilter: String): Flow<List<PatientEntity>>

    @Query("""
        SELECT * FROM patients WHERE 
        status = 'Active'
        AND nextAppointmentDate IS NOT NULL
        AND nextAppointmentDate >= :startOfDay AND nextAppointmentDate < :endOfDay
        ORDER BY nextAppointmentDate ASC
    """)
    fun getTodaysPatients(startOfDay: Long, endOfDay: Long): Flow<List<PatientEntity>>

    @Query("""
        SELECT p.* FROM patients p
        WHERE p.status = 'Active'
        AND p.id NOT IN (
            SELECT patientId FROM sessions WHERE date >= :cutoffDate AND status = 'Present'
        )
        AND p.id IN (
            SELECT patientId FROM sessions WHERE status = 'Present'
        )
    """)
    fun getPatientsAbsentSince(cutoffDate: Long): Flow<List<PatientEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(patient: PatientEntity): Long

    @Update
    suspend fun update(patient: PatientEntity)

    @Delete
    suspend fun delete(patient: PatientEntity)

    @Query("SELECT COUNT(*) FROM patients")
    suspend fun getPatientCount(): Int

    @Query("SELECT * FROM patients ORDER BY fullName ASC")
    suspend fun getAllPatientsOnce(): List<PatientEntity>
}
