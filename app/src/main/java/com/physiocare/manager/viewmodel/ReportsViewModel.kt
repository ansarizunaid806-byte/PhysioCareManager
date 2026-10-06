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
import com.physiocare.manager.util.DateUtils
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class ReportMonthData(
    val patient: PatientEntity,
    val sessionsAttended: Int,
    val totalCharges: Int,
    val totalPaid: Int,
    val balance: Int
)

data class ReportsState(
    val monthReport: List<ReportMonthData> = emptyList(),
    val totalSessions: Int = 0,
    val totalCharges: Int = 0,
    val totalCollected: Int = 0,
    val totalPending: Int = 0,
    val selectedYear: Int = DateUtils.getYear(System.currentTimeMillis()),
    val selectedMonth: Int = DateUtils.getMonth(System.currentTimeMillis()),
    val isLoading: Boolean = true
)

class ReportsViewModel(
    private val patientRepository: PatientRepository,
    private val sessionRepository: SessionRepository,
    private val paymentRepository: PaymentRepository
) : ViewModel() {

    private val _state = MutableStateFlow(ReportsState())
    val state: StateFlow<ReportsState> = _state.asStateFlow()

    // Expose data for CSV export
    private val _patientsForExport = MutableStateFlow<List<PatientEntity>>(emptyList())
    val patientsForExport: StateFlow<List<PatientEntity>> = _patientsForExport.asStateFlow()

    private val _sessionsForExport = MutableStateFlow<List<SessionEntity>>(emptyList())
    val sessionsForExport: StateFlow<List<SessionEntity>> = _sessionsForExport.asStateFlow()

    private val _paymentsForExport = MutableStateFlow<List<PaymentEntity>>(emptyList())
    val paymentsForExport: StateFlow<List<PaymentEntity>> = _paymentsForExport.asStateFlow()

    init {
        loadReport()
    }

    fun loadReport() {
        viewModelScope.launch {
            val year = _state.value.selectedYear
            val month = _state.value.selectedMonth
            val startOfMonth = DateUtils.getStartOfMonth(year, month)
            val endOfMonth = DateUtils.getEndOfMonth(year, month)

            val patients = patientRepository.getAllPatientsOnce()
            _patientsForExport.value = patients

            val reportData = mutableListOf<ReportMonthData>()
            var totalSessions = 0
            var totalCharges = 0
            var totalPaid = 0

            for (patient in patients) {
                val sessions = sessionRepository.getPresentSessionsForPatientMonthOnce(
                    patient.id, startOfMonth, endOfMonth
                )
                val charges = sessions.sumOf { it.charge }
                val paid = paymentRepository.getMonthlyPayments(patient.id, startOfMonth, endOfMonth)
                val attended = sessions.size

                if (attended > 0 || paid > 0) {
                    reportData.add(
                        ReportMonthData(
                            patient = patient,
                            sessionsAttended = attended,
                            totalCharges = charges,
                            totalPaid = paid,
                            balance = charges - paid
                        )
                    )
                }
                totalSessions += attended
                totalCharges += charges
                totalPaid += paid
            }

            // Get sessions and payments for export
            val allSessions = sessionRepository.getAllSessionsOnce()
            val allPayments = paymentRepository.getAllPaymentsOnce()
            _sessionsForExport.value = allSessions
            _paymentsForExport.value = allPayments

            _state.update {
                it.copy(
                    monthReport = reportData,
                    totalSessions = totalSessions,
                    totalCharges = totalCharges,
                    totalCollected = totalPaid,
                    totalPending = totalCharges - totalPaid,
                    isLoading = false
                )
            }
        }
    }

    fun changeMonth(year: Int, month: Int) {
        _state.update { it.copy(selectedYear = year, selectedMonth = month) }
        loadReport()
    }

    class Factory(
        private val patientRepository: PatientRepository,
        private val sessionRepository: SessionRepository,
        private val paymentRepository: PaymentRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ReportsViewModel(patientRepository, sessionRepository, paymentRepository) as T
        }
    }
}
