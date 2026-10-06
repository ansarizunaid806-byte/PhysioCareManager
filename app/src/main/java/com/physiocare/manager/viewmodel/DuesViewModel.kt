package com.physiocare.manager.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.physiocare.manager.data.local.entity.PatientEntity
import com.physiocare.manager.data.local.entity.SessionEntity
import com.physiocare.manager.data.local.entity.PaymentEntity
import com.physiocare.manager.data.repository.PatientRepository
import com.physiocare.manager.data.repository.PaymentRepository
import com.physiocare.manager.data.repository.SessionRepository
import com.physiocare.manager.util.CurrencyUtils
import com.physiocare.manager.util.DateUtils
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class DuesState(
    val patientsWithDues: List<PatientDueInfo> = emptyList(),
    val totalPending: Int = 0,
    val isLoading: Boolean = true
)

data class PatientDueInfo(
    val patient: PatientEntity,
    val totalCharges: Int,
    val totalPaid: Int,
    val outstanding: Int,
    val dueSessionCount: Int
)

class DuesViewModel(
    private val patientRepository: PatientRepository,
    private val sessionRepository: SessionRepository,
    private val paymentRepository: PaymentRepository
) : ViewModel() {

    private val _state = MutableStateFlow(DuesState())
    val state: StateFlow<DuesState> = _state.asStateFlow()

    init {
        loadDues()
    }

    private fun loadDues() {
        viewModelScope.launch {
            patientRepository.getAllPatients().collect { patients ->
                val dueInfoList = mutableListOf<PatientDueInfo>()
                for (patient in patients) {
                    val sessions = sessionRepository.getAllSessionsForPatientOnce(patient.id)
                    val totalCharges = sessions.filter { it.status == "Present" }.sumOf { it.charge }
                    val totalPaid = paymentRepository.getTotalPaymentsForPatient(patient.id)
                    val outstanding = totalCharges - totalPaid
                    val dueSessions = sessions.count { it.status == "Present" && it.paymentStatus == "Due" }

                    if (outstanding > 0) {
                        dueInfoList.add(
                            PatientDueInfo(patient, totalCharges, totalPaid, outstanding, dueSessions)
                        )
                    }
                }

                // Sort by highest due first
                dueInfoList.sortByDescending { it.outstanding }
                val total = dueInfoList.sumOf { it.outstanding }

                _state.update {
                    it.copy(
                        patientsWithDues = dueInfoList,
                        totalPending = total,
                        isLoading = false
                    )
                }
            }
        }
    }

    fun refresh() {
        loadDues()
    }

    class Factory(
        private val patientRepository: PatientRepository,
        private val sessionRepository: SessionRepository,
        private val paymentRepository: PaymentRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return DuesViewModel(patientRepository, sessionRepository, paymentRepository) as T
        }
    }
}
