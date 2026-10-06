package com.physiocare.manager.data.local.dao

import androidx.room.*
import com.physiocare.manager.data.local.entity.PaymentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PaymentDao {

    @Query("SELECT * FROM payments WHERE patientId = :patientId ORDER BY date DESC")
    fun getPaymentsByPatient(patientId: Long): Flow<List<PaymentEntity>>

    @Query("SELECT * FROM payments WHERE patientId = :patientId ORDER BY date DESC")
    suspend fun getPaymentsByPatientOnce(patientId: Long): List<PaymentEntity>

    @Query("SELECT * FROM payments WHERE date >= :startOfMonth AND date < :endOfMonth ORDER BY date DESC")
    fun getPaymentsForMonth(startOfMonth: Long, endOfMonth: Long): Flow<List<PaymentEntity>>

    @Query("SELECT * FROM payments WHERE date >= :startOfMonth AND date < :endOfMonth ORDER BY date DESC")
    suspend fun getPaymentsForMonthOnce(startOfMonth: Long, endOfMonth: Long): List<PaymentEntity>

    @Query("SELECT COALESCE(SUM(amount), 0) FROM payments WHERE patientId = :patientId AND date >= :startOfMonth AND date < :endOfMonth")
    suspend fun getMonthlyPayments(patientId: Long, startOfMonth: Long, endOfMonth: Long): Int

    @Query("SELECT COALESCE(SUM(amount), 0) FROM payments WHERE patientId = :patientId")
    suspend fun getTotalPaymentsForPatient(patientId: Long): Int

    @Query("SELECT COALESCE(SUM(amount), 0) FROM payments WHERE date >= :startOfMonth AND date < :endOfMonth")
    suspend fun getTotalMonthlyPayments(startOfMonth: Long, endOfMonth: Long): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(payment: PaymentEntity): Long

    @Update
    suspend fun update(payment: PaymentEntity)

    @Delete
    suspend fun delete(payment: PaymentEntity)

    @Query("SELECT * FROM payments ORDER BY date DESC")
    suspend fun getAllPaymentsOnce(): List<PaymentEntity>
}
