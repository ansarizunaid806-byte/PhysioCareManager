package com.physiocare.manager.data.local.dao

import androidx.room.*
import com.physiocare.manager.data.local.entity.SessionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SessionDao {

    @Query("SELECT * FROM sessions WHERE patientId = :patientId ORDER BY date DESC")
    fun getSessionsByPatient(patientId: Long): Flow<List<SessionEntity>>

    @Query("SELECT * FROM sessions WHERE patientId = :patientId AND date >= :startOfMonth AND date < :endOfMonth ORDER BY date ASC")
    fun getSessionsForPatientMonth(patientId: Long, startOfMonth: Long, endOfMonth: Long): Flow<List<SessionEntity>>

    @Query("SELECT * FROM sessions WHERE date >= :startOfDay AND date < :endOfDay ORDER BY date ASC")
    fun getSessionsForDate(startOfDay: Long, endOfDay: Long): Flow<List<SessionEntity>>

    @Query("SELECT * FROM sessions WHERE patientId = :patientId AND date >= :startOfMonth AND date < :endOfMonth ORDER BY date ASC")
    suspend fun getSessionsForPatientMonthOnce(patientId: Long, startOfMonth: Long, endOfMonth: Long): List<SessionEntity>

    @Query("SELECT * FROM sessions WHERE patientId = :patientId AND date >= :startOfMonth AND date < :endOfMonth AND status = 'Present'")
    fun getPresentSessionsForPatientMonth(patientId: Long, startOfMonth: Long, endOfMonth: Long): Flow<List<SessionEntity>>

    @Query("SELECT * FROM sessions WHERE patientId = :patientId AND date >= :startOfMonth AND date < :endOfMonth AND status = 'Present'")
    suspend fun getPresentSessionsForPatientMonthOnce(patientId: Long, startOfMonth: Long, endOfMonth: Long): List<SessionEntity>

    @Query("SELECT * FROM sessions WHERE patientId = :patientId AND paymentStatus = 'Due' AND status = 'Present' ORDER BY date ASC")
    fun getDueSessionsForPatient(patientId: Long): Flow<List<SessionEntity>>

    @Query("SELECT * FROM sessions WHERE patientId = :patientId AND paymentStatus = 'Due' AND status = 'Present' ORDER BY date ASC")
    suspend fun getDueSessionsForPatientOnce(patientId: Long): List<SessionEntity>

    @Query("SELECT COALESCE(SUM(charge), 0) FROM sessions WHERE patientId = :patientId AND status = 'Present' AND date >= :startOfMonth AND date < :endOfMonth")
    suspend fun getMonthlyCharges(patientId: Long, startOfMonth: Long, endOfMonth: Long): Int

    @Query("SELECT COUNT(*) FROM sessions WHERE patientId = :patientId AND status = 'Present' AND date >= :startOfMonth AND date < :endOfMonth")
    suspend fun getMonthlySessionCount(patientId: Long, startOfMonth: Long, endOfMonth: Long): Int

    @Query("SELECT COUNT(*) FROM sessions WHERE patientId = :patientId AND status = 'Absent' AND date >= :startOfMonth AND date < :endOfMonth")
    suspend fun getMonthlyAbsentCount(patientId: Long, startOfMonth: Long, endOfMonth: Long): Int

    @Query("SELECT COALESCE(SUM(charge), 0) FROM sessions WHERE status = 'Present' AND date >= :startOfMonth AND date < :endOfMonth")
    suspend fun getTotalMonthlyCharges(startOfMonth: Long, endOfMonth: Long): Int

    @Query("SELECT COUNT(*) FROM sessions WHERE status = 'Present' AND date >= :startOfMonth AND date < :endOfMonth")
    suspend fun getTotalMonthlySessions(startOfMonth: Long, endOfMonth: Long): Int

    @Query("SELECT * FROM sessions WHERE id = :id")
    suspend fun getSessionById(id: Long): SessionEntity?

    @Query("SELECT * FROM sessions WHERE patientId = :patientId AND date = :date")
    suspend fun getSessionForPatientDate(patientId: Long, date: Long): SessionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(session: SessionEntity): Long

    @Update
    suspend fun update(session: SessionEntity)

    @Delete
    suspend fun delete(session: SessionEntity)

    @Query("UPDATE sessions SET paymentStatus = 'Paid' WHERE id IN (:ids)")
    suspend fun markSessionsAsPaid(ids: List<Long>)

    @Query("SELECT * FROM sessions WHERE patientId = :patientId ORDER BY date DESC")
    suspend fun getAllSessionsForPatientOnce(patientId: Long): List<SessionEntity>

    @Query("SELECT * FROM sessions ORDER BY date DESC")
    suspend fun getAllSessionsOnce(): List<SessionEntity>
}
