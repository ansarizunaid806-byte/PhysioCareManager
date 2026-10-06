package com.physiocare.manager.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.physiocare.manager.data.local.entity.PaymentEntity
import com.physiocare.manager.data.local.entity.SessionEntity
import com.physiocare.manager.data.repository.PaymentRepository
import com.physiocare.manager.data.repository.SessionRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class PaymentViewModel(
    private val patientId: Long,
    private val sessionRepository: SessionRepository,
    private val paymentRepository: PaymentRepository
) : ViewModel() {

    private val _outstanding = MutableStateFlow(0)
    val outstanding: StateFlow<Int> = _outstanding.asStateFlow()

    private val _dueSessions = MutableStateFlow<List<SessionEntity>>(emptyList())
    val dueSessions: StateFlow<List<SessionEntity>> = _dueSessions.asStateFlow()

    init {
        loadOutstanding()
    }

    private fun loadOutstanding() {
        viewModelScope.launch {
            val sessions = sessionRepository.getAllSessionsForPatientOnce(patientId)
            val totalCharges = sessions.filter { it.status == "Present" }.sumOf { it.charge }
            val totalPaid = paymentRepository.getTotalPaymentsForPatient(patientId)
            _outstanding.value = totalCharges - totalPaid

            val dueSessions = sessionRepository.getDueSessionsForPatientOnce(patientId)
            _dueSessions.value = dueSessions
        }
    }

    fun recordPayment(amount: Int, mode: String, note: String?) {
        viewModelScope.launch {
            // Insert payment
            paymentRepository.insert(
                PaymentEntity(
                    patientId = patientId,
                    amount = amount,
                    date = System.currentTimeMillis(),
                    mode = mode,
                    note = note
                )
            )

            // Auto-mark oldest due sessions as paid
            val dueSessions = sessionRepository.getDueSessionsForPatientOnce(patientId)
            var remaining = amount
            val toMarkPaid = mutableListOf<Long>()

            for (session in dueSessions) {
                if (remaining <= 0) break
                if (remaining >= session.charge) {
                    toMarkPaid.add(session.id)
                    remaining -= session.charge
                } else {
                    // Partial - don't mark this one as fully paid
                    break
                }
            }

            if (toMarkPaid.isNotEmpty()) {
                sessionRepository.markSessionsAsPaid(toMarkPaid)
            }

            loadOutstanding()
        }
    }

    class Factory(
        private val patientId: Long,
        private val sessionRepository: SessionRepository,
        private val paymentRepository: PaymentRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return PaymentViewModel(patientId, sessionRepository, paymentRepository) as T
        }
    }
}
