package com.physiocare.manager.data.repository

import com.physiocare.manager.data.local.dao.PaymentDao
import com.physiocare.manager.data.local.entity.PaymentEntity
import com.physiocare.manager.data.sync.CloudSyncManager
import kotlinx.coroutines.flow.Flow

class PaymentRepository(
    private val paymentDao: PaymentDao,
    private val cloudSyncManager: CloudSyncManager? = null
) {

    fun getPaymentsByPatient(patientId: Long): Flow<List<PaymentEntity>> =
        paymentDao.getPaymentsByPatient(patientId)

    suspend fun getPaymentsByPatientOnce(patientId: Long): List<PaymentEntity> =
        paymentDao.getPaymentsByPatientOnce(patientId)

    fun getPaymentsForMonth(startOfMonth: Long, endOfMonth: Long): Flow<List<PaymentEntity>> =
        paymentDao.getPaymentsForMonth(startOfMonth, endOfMonth)

    suspend fun getPaymentsForMonthOnce(startOfMonth: Long, endOfMonth: Long): List<PaymentEntity> =
        paymentDao.getPaymentsForMonthOnce(startOfMonth, endOfMonth)

    suspend fun getMonthlyPayments(
        patientId: Long, startOfMonth: Long, endOfMonth: Long
    ): Int = paymentDao.getMonthlyPayments(patientId, startOfMonth, endOfMonth)

    suspend fun getTotalPaymentsForPatient(patientId: Long): Int =
        paymentDao.getTotalPaymentsForPatient(patientId)

    suspend fun getTotalMonthlyPayments(startOfMonth: Long, endOfMonth: Long): Int =
        paymentDao.getTotalMonthlyPayments(startOfMonth, endOfMonth)

    suspend fun insert(payment: PaymentEntity): Long {
        val id = paymentDao.insert(payment)
        cloudSyncManager?.syncToCloud(emptyList(), emptyList(), listOf(payment))
        return id
    }

    suspend fun update(payment: PaymentEntity) {
        paymentDao.update(payment)
        cloudSyncManager?.syncToCloud(emptyList(), emptyList(), listOf(payment))
    }

    suspend fun delete(payment: PaymentEntity) {
        paymentDao.delete(payment)
    }

    suspend fun getAllPaymentsOnce(): List<PaymentEntity> = paymentDao.getAllPaymentsOnce()
}
