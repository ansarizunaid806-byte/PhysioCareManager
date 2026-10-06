package com.physiocare.manager.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.physiocare.manager.data.local.entity.PatientEntity
import com.physiocare.manager.data.local.entity.SessionEntity
import com.physiocare.manager.data.local.entity.PaymentEntity
import com.physiocare.manager.data.repository.PatientRepository
import com.physiocare.manager.data.repository.PaymentRepository
import com.physiocare.manager.data.repository.SessionRepository
import com.physiocare.manager.util.DateUtils
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class PatientProfileState(
    val patient: PatientEntity? = null,
    val allSessions: List<SessionEntity> = emptyList(),
    val allPayments: List<PaymentEntity> = emptyList(),
    val monthlySessions: List<SessionEntity> = emptyList(),
    val presentCount: Int = 0,
    val absentCount: Int = 0,
    val monthlyCharges: Int = 0,
    val totalBilled: Int = 0,
    val totalPaid: Int = 0,
    val outstanding: Int = 0,
    val selectedYear: Int = DateUtils.getYear(System.currentTimeMillis()),
    val selectedMonth: Int = DateUtils.getMonth(System.currentTimeMillis()),
    val isLoading: Boolean = true
)

class PatientProfileViewModel(
    private val patientId: Long,
    private val patientRepository: PatientRepository,
    private val sessionRepository: SessionRepository,
    private val paymentRepository: PaymentRepository
) : ViewModel() {

    private val _state = MutableStateFlow(PatientProfileState())
    val state: StateFlow<PatientProfileState> = _state.asStateFlow()

    init {
        loadPatientData()
    }

    private fun loadPatientData() {
        viewModelScope.launch {
            patientRepository.getPatientById(patientId).collect { patient ->
                _state.update { it.copy(patient = patient, isLoading = false) }
                if (patient != null) {
                    loadMonthlyData(patient, _state.value.selectedYear, _state.value.selectedMonth)
                    loadTotals(patient)
                }
            }
        }

        viewModelScope.launch {
            sessionRepository.getSessionsByPatient(patientId).collect { sessions ->
                _state.update { it.copy(allSessions = sessions) }
            }
        }

        viewModelScope.launch {
            paymentRepository.getPaymentsByPatient(patientId).collect { payments ->
                _state.update { it.copy(allPayments = payments) }
            }
        }
    }

    private suspend fun loadMonthlyData(patient: PatientEntity, year: Int, month: Int) {
        val start = DateUtils.getStartOfMonth(year, month)
        val end = DateUtils.getEndOfMonth(year, month)

        val sessions = sessionRepository.getSessionsForPatientMonthOnce(patientId, start, end)
        val presentCount = sessionRepository.getMonthlySessionCount(patientId, start, end)
        val absentCount = sessionRepository.getMonthlyAbsentCount(patientId, start, end)
        val charges = sessionRepository.getMonthlyCharges(patientId, start, end)

        _state.update {
            it.copy(
                monthlySessions = sessions,
                presentCount = presentCount,
                absentCount = absentCount,
                monthlyCharges = charges,
                selectedYear = year,
                selectedMonth = month
            )
        }
    }

    private suspend fun loadTotals(patient: PatientEntity) {
        val allPresentSessions = sessionRepository.getAllSessionsForPatientOnce(patientId)
            .filter { it.status == "Present" }
        val totalBilled = allPresentSessions.sumOf { it.charge }
        val totalPaid = paymentRepository.getTotalPaymentsForPatient(patientId)

        _state.update {
            it.copy(
                totalBilled = totalBilled,
                totalPaid = totalPaid,
                outstanding = totalBilled - totalPaid
            )
        }
    }

    fun changeMonth(year: Int, month: Int) {
        viewModelScope.launch {
            val patient = _state.value.patient ?: return@launch
            loadMonthlyData(patient, year, month)
        }
    }

    fun updateSession(session: SessionEntity) {
        viewModelScope.launch {
            sessionRepository.update(session)
            val patient = _state.value.patient ?: return@launch
            loadMonthlyData(patient, _state.value.selectedYear, _state.value.selectedMonth)
            loadTotals(patient)
        }
    }

    fun deleteSession(session: SessionEntity) {
        viewModelScope.launch {
            sessionRepository.delete(session)
            val patient = _state.value.patient ?: return@launch
            loadMonthlyData(patient, _state.value.selectedYear, _state.value.selectedMonth)
            loadTotals(patient)
        }
    }

    class Factory(
        private val patientId: Long,
        private val patientRepository: PatientRepository,
        private val sessionRepository: SessionRepository,
        private val paymentRepository: PaymentRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return PatientProfileViewModel(patientId, patientRepository, sessionRepository, paymentRepository) as T
        }
    }
}
