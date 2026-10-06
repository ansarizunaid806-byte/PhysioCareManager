package com.physiocare.manager.data.repository

import com.physiocare.manager.data.local.dao.SessionDao
import com.physiocare.manager.data.local.entity.SessionEntity
import kotlinx.coroutines.flow.Flow

class SessionRepository(private val sessionDao: SessionDao) {

    fun getSessionsByPatient(patientId: Long): Flow<List<SessionEntity>> =
        sessionDao.getSessionsByPatient(patientId)

    fun getSessionsForPatientMonth(
        patientId: Long, startOfMonth: Long, endOfMonth: Long
    ): Flow<List<SessionEntity>> =
        sessionDao.getSessionsForPatientMonth(patientId, startOfMonth, endOfMonth)

    fun getSessionsForDate(startOfDay: Long, endOfDay: Long): Flow<List<SessionEntity>> =
        sessionDao.getSessionsForDate(startOfDay, endOfDay)

    suspend fun getSessionsForPatientMonthOnce(
        patientId: Long, startOfMonth: Long, endOfMonth: Long
    ): List<SessionEntity> =
        sessionDao.getSessionsForPatientMonthOnce(patientId, startOfMonth, endOfMonth)

    fun getPresentSessionsForPatientMonth(
        patientId: Long, startOfMonth: Long, endOfMonth: Long
    ): Flow<List<SessionEntity>> =
        sessionDao.getPresentSessionsForPatientMonth(patientId, startOfMonth, endOfMonth)

    suspend fun getPresentSessionsForPatientMonthOnce(
        patientId: Long, startOfMonth: Long, endOfMonth: Long
    ): List<SessionEntity> =
        sessionDao.getPresentSessionsForPatientMonthOnce(patientId, startOfMonth, endOfMonth)

    fun getDueSessionsForPatient(patientId: Long): Flow<List<SessionEntity>> =
        sessionDao.getDueSessionsForPatient(patientId)

    suspend fun getDueSessionsForPatientOnce(patientId: Long): List<SessionEntity> =
        sessionDao.getDueSessionsForPatientOnce(patientId)

    suspend fun getMonthlyCharges(
        patientId: Long, startOfMonth: Long, endOfMonth: Long
    ): Int = sessionDao.getMonthlyCharges(patientId, startOfMonth, endOfMonth)

    suspend fun getMonthlySessionCount(
        patientId: Long, startOfMonth: Long, endOfMonth: Long
    ): Int = sessionDao.getMonthlySessionCount(patientId, startOfMonth, endOfMonth)

    suspend fun getMonthlyAbsentCount(
        patientId: Long, startOfMonth: Long, endOfMonth: Long
    ): Int = sessionDao.getMonthlyAbsentCount(patientId, startOfMonth, endOfMonth)

    suspend fun getTotalMonthlyCharges(startOfMonth: Long, endOfMonth: Long): Int =
        sessionDao.getTotalMonthlyCharges(startOfMonth, endOfMonth)

    suspend fun getTotalMonthlySessions(startOfMonth: Long, endOfMonth: Long): Int =
        sessionDao.getTotalMonthlySessions(startOfMonth, endOfMonth)

    suspend fun getSessionById(id: Long): SessionEntity? = sessionDao.getSessionById(id)

    suspend fun getSessionForPatientDate(patientId: Long, date: Long): SessionEntity? =
        sessionDao.getSessionForPatientDate(patientId, date)

    suspend fun insert(session: SessionEntity): Long = sessionDao.insert(session)

    suspend fun update(session: SessionEntity) = sessionDao.update(session)

    suspend fun delete(session: SessionEntity) = sessionDao.delete(session)

    suspend fun markSessionsAsPaid(ids: List<Long>) = sessionDao.markSessionsAsPaid(ids)

    suspend fun getAllSessionsForPatientOnce(patientId: Long): List<SessionEntity> =
        sessionDao.getAllSessionsForPatientOnce(patientId)

    suspend fun getAllSessionsOnce(): List<SessionEntity> = sessionDao.getAllSessionsOnce()
}
