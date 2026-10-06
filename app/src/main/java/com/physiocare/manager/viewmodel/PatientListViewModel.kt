package com.physiocare.manager.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.physiocare.manager.data.local.entity.PatientEntity
import com.physiocare.manager.data.repository.PatientRepository
import com.physiocare.manager.data.repository.PaymentRepository
import com.physiocare.manager.data.repository.SessionRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class PatientListState(
    val patients: List<PatientEntity> = emptyList(),
    val outstandingMap: Map<Long, Int> = emptyMap(), // patientId -> due amount
    val searchQuery: String = "",
    val statusFilter: String = "All", // All, Active, Completed, WithDues
    val isLoading: Boolean = true
)

class PatientListViewModel(
    private val patientRepository: PatientRepository,
    private val sessionRepository: SessionRepository,
    private val paymentRepository: PaymentRepository
) : ViewModel() {

    private val _state = MutableStateFlow(PatientListState())
    val state: StateFlow<PatientListState> = _state.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    private val _statusFilter = MutableStateFlow("All")

    init {
        observePatients()
    }

    private fun observePatients() {
        viewModelScope.launch {
            combine(_searchQuery, _statusFilter) { query, filter ->
                Pair(query, filter)
            }.collect { (query, filter) ->
                patientRepository.getAllPatients().collect { patients ->
                    val filtered = when (filter) {
                        "Active" -> patients.filter { it.status == "Active" }
                        "Completed" -> patients.filter { it.status == "Completed" }
                        "WithDues" -> {
                            // Compute dues for all patients
                            val duePatients = mutableListOf<PatientEntity>()
                            for (p in patients) {
                                val dueSessions = sessionRepository.getDueSessionsForPatientOnce(p.id)
                                if (dueSessions.isNotEmpty()) duePatients.add(p)
                            }
                            duePatients
                        }
                        else -> patients
                    }

                    val searchFiltered = if (query.isBlank()) filtered else filtered.filter {
                        it.fullName.contains(query, ignoreCase = true) ||
                        it.mobileNumber.contains(query) ||
                        it.condition.contains(query, ignoreCase = true)
                    }

                    // Calculate outstanding for each patient
                    val outstandingMap = mutableMapOf<Long, Int>()
                    for (patient in searchFiltered) {
                        val totalCharges = sessionRepository.getAllSessionsForPatientOnce(patient.id)
                            .filter { it.status == "Present" }
                            .sumOf { it.charge }
                        val totalPaid = paymentRepository.getTotalPaymentsForPatient(patient.id)
                        val outstanding = totalCharges - totalPaid
                        if (outstanding > 0) {
                            outstandingMap[patient.id] = outstanding
                        }
                    }

                    _state.update {
                        it.copy(
                            patients = searchFiltered,
                            outstandingMap = outstandingMap,
                            isLoading = false
                        )
                    }
                }
            }
        }
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun onStatusFilterChange(filter: String) {
        _statusFilter.value = filter
    }

    fun deletePatient(patient: PatientEntity) {
        viewModelScope.launch {
            patientRepository.delete(patient)
        }
    }

    class Factory(
        private val patientRepository: PatientRepository,
        private val sessionRepository: SessionRepository,
        private val paymentRepository: PaymentRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return PatientListViewModel(patientRepository, sessionRepository, paymentRepository) as T
        }
    }
}
