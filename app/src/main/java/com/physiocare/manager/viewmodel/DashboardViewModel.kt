package com.physiocare.manager.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.physiocare.manager.data.local.entity.PatientEntity
import com.physiocare.manager.data.local.entity.SessionEntity
import com.physiocare.manager.data.repository.PatientRepository
import com.physiocare.manager.data.repository.PaymentRepository
import com.physiocare.manager.data.repository.SessionRepository
import com.physiocare.manager.util.DateUtils
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar

data class DashboardState(
    val todaysPatients: List<PatientEntity> = emptyList(),
    val todaysSessions: Map<Long, SessionEntity> = emptyMap(), // patientId -> session
    val monthlySessions: Int = 0,
    val monthlyBilled: Int = 0,
    val monthlyCollected: Int = 0,
    val monthlyPending: Int = 0,
    val patientsWithDues: List<PatientEntity> = emptyList(),
    val absentPatients: List<PatientEntity> = emptyList(),
    val isLoading: Boolean = true
)

class DashboardViewModel(
    private val patientRepository: PatientRepository,
    private val sessionRepository: SessionRepository,
    private val paymentRepository: PaymentRepository
) : ViewModel() {

    private val _state = MutableStateFlow(DashboardState())
    val state: StateFlow<DashboardState> = _state.asStateFlow()

    private val startOfDay = DateUtils.getStartOfDay()
    private val endOfDay = DateUtils.getEndOfDay()
    private val startOfMonth = DateUtils.getCurrentMonthStart()
    private val endOfMonth = DateUtils.getCurrentMonthEnd()

    init {
        loadDashboardData()
    }

    private fun loadDashboardData() {
        viewModelScope.launch {
            // Today's patients (all active patients)
            patientRepository.getAllPatients().combine(
                sessionRepository.getSessionsForDate(startOfDay, endOfDay)
            ) { patients, sessions ->
                val todayPatients = patients.filter { it.status == "Active" }
                val sessionMap = sessions
                    .groupBy { it.patientId }
                    .mapValues { it.value.first() }
                DashboardState(
                    todaysPatients = todayPatients,
                    todaysSessions = sessionMap,
                    isLoading = false
                )
            }.collect { todayState ->
                _state.update { current ->
                    current.copy(
                        todaysPatients = todayState.todaysPatients,
                        todaysSessions = todayState.todaysSessions,
                        isLoading = false
                    )
                }
            }
        }

        viewModelScope.launch {
            val sessions = sessionRepository.getTotalMonthlySessions(startOfMonth, endOfMonth)
            val charges = sessionRepository.getTotalMonthlyCharges(startOfMonth, endOfMonth)
            val collected = paymentRepository.getTotalMonthlyPayments(startOfMonth, endOfMonth)
            _state.update { it.copy(
                monthlySessions = sessions,
                monthlyBilled = charges,
                monthlyCollected = collected,
                monthlyPending = charges - collected
            )}
        }

        viewModelScope.launch {
            patientRepository.getPatientsWithDues().collect { patients ->
                _state.update { it.copy(patientsWithDues = patients) }
            }
        }

        // Patients absent for 7+ days
        viewModelScope.launch {
            val cal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -7) }
            val cutoff = DateUtils.getStartOfDay(cal)
            patientRepository.getPatientsAbsentSince(cutoff).collect { patients ->
                _state.update { it.copy(absentPatients = patients) }
            }
        }
    }

    fun markAttendance(patientId: Long, status: String, patient: PatientEntity) {
        viewModelScope.launch {
            val todayDate = startOfDay
            val existingSession = sessionRepository.getSessionForPatientDate(patientId, todayDate)

            if (existingSession != null) {
                val charge = if (status == "Present") existingSession.charge else 0
                sessionRepository.update(
                    existingSession.copy(
                        status = status,
                        charge = charge,
                        paymentStatus = if (status == "Present") "Due" else "Due"
                    )
                )
            } else {
                if (status == "Present") {
                    sessionRepository.insert(
                        SessionEntity(
                            patientId = patientId,
                            date = todayDate,
                            status = status,
                            charge = patient.perSessionCharge,
                            paymentStatus = "Due"
                        )
                    )
                } else {
                    sessionRepository.insert(
                        SessionEntity(
                            patientId = patientId,
                            date = todayDate,
                            status = status,
                            charge = 0,
                            paymentStatus = "Due"
                        )
                    )
                }
            }
        }
    }

    fun refresh() {
        loadDashboardData()
    }

    class Factory(
        private val patientRepository: PatientRepository,
        private val sessionRepository: SessionRepository,
        private val paymentRepository: PaymentRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return DashboardViewModel(patientRepository, sessionRepository, paymentRepository) as T
        }
    }
}
