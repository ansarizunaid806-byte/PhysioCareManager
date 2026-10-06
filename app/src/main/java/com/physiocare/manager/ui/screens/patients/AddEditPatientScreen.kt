package com.physiocare.manager.ui.screens.patients

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.physiocare.manager.data.local.entity.PatientEntity
import com.physiocare.manager.data.repository.PatientRepository
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditPatientScreen(
    patientId: Long?,
    patientRepository: PatientRepository,
    onNavigateBack: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val isEdit = patientId != null

    var fullName by remember { mutableStateOf("") }
    var mobileNumber by remember { mutableStateOf("") }
    var age by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("Male") }
    var condition by remember { mutableStateOf("") }
    var referredBy by remember { mutableStateOf("") }
    var perSessionCharge by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var plannedSessionsPerWeek by remember { mutableStateOf("") }
    var totalSessionsPlanned by remember { mutableStateOf("") }
    var isHomeVisitAvailable by remember { mutableStateOf(false) }
    var homeVisitCharge by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }
    var isSaving by remember { mutableStateOf(false) }
    var genderExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(patientId) {
        if (isEdit) {
            patientRepository.getPatientByIdOnce(patientId!!)?.let { patient ->
                fullName = patient.fullName
                mobileNumber = patient.mobileNumber
                age = patient.age.toString()
                gender = patient.gender
                condition = patient.condition
                referredBy = patient.referredBy ?: ""
                perSessionCharge = patient.perSessionCharge.toString()
                notes = patient.notes ?: ""
                plannedSessionsPerWeek = patient.plannedSessionsPerWeek?.toString() ?: ""
                totalSessionsPlanned = patient.totalSessionsPlanned?.toString() ?: ""
                isHomeVisitAvailable = patient.isHomeVisitAvailable
                homeVisitCharge = patient.homeVisitCharge?.toString() ?: ""
            }
        }
        isLoading = false
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (isEdit) "Edit Patient" else "Add Patient",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            if (fullName.isBlank() || mobileNumber.isBlank()) return@TextButton
                            isSaving = true
                            scope.launch {
                                val patient = PatientEntity(
                                    id = patientId ?: 0,
                                    fullName = fullName.trim(),
                                    mobileNumber = mobileNumber.trim(),
                                    age = age.toIntOrNull() ?: 0,
                                    gender = gender,
                                    condition = condition.trim(),
                                    referredBy = referredBy.trim().ifBlank { null },
                                    startDate = System.currentTimeMillis(),
                                    perSessionCharge = perSessionCharge.toIntOrNull() ?: 500,
                                    plannedSessionsPerWeek = plannedSessionsPerWeek.toIntOrNull(),
                                    totalSessionsPlanned = totalSessionsPlanned.toIntOrNull(),
                                    notes = notes.trim().ifBlank { null },
                                    isHomeVisitAvailable = isHomeVisitAvailable,
                                    homeVisitCharge = homeVisitCharge.toIntOrNull(),
                                    updatedAt = System.currentTimeMillis()
                                )
                                if (isEdit) {
                                    patientRepository.update(patient)
                                } else {
                                    patientRepository.insert(patient)
                                }
                                onNavigateBack()
                            }
                        },
                        enabled = fullName.isNotBlank() && !isSaving
                    ) {
                        Text("Save")
                    }
                }
            )
        }
    ) { padding ->
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = androidx.compose.ui.Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Required fields
                Text("Basic Information", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)

                OutlinedTextField(
                    value = fullName,
                    onValueChange = { fullName = it },
                    label = { Text("Full Name *") },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = { Icon(Icons.Default.Person, null) },
                    singleLine = true
                )

                OutlinedTextField(
                    value = mobileNumber,
                    onValueChange = { mobileNumber = it },
                    label = { Text("Mobile Number *") },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = { Icon(Icons.Default.Phone, null) },
                    singleLine = true,
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        keyboardType = androidx.compose.ui.text.input.KeyboardType.Phone
                    )
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = age,
                        onValueChange = { age = it.filter { c -> c.isDigit() } },
                        label = { Text("Age") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )

                    ExposedDropdownMenuBox(
                        expanded = genderExpanded,
                        onExpandedChange = { genderExpanded = it },
                        modifier = Modifier.weight(1f)
                    ) {
                        OutlinedTextField(
                            value = gender,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Gender") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = genderExpanded) },
                            modifier = Modifier.menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = genderExpanded,
                            onDismissRequest = { genderExpanded = false }
                        ) {
                            listOf("Male", "Female", "Other").forEach { g ->
                                DropdownMenuItem(
                                    text = { Text(g) },
                                    onClick = { gender = g; genderExpanded = false }
                                )
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = condition,
                    onValueChange = { condition = it },
                    label = { Text("Condition / Diagnosis") },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = { Icon(Icons.Default.MedicalServices, null) },
                    placeholder = { Text("e.g., Knee pain – post surgery") }
                )

                OutlinedTextField(
                    value = referredBy,
                    onValueChange = { referredBy = it },
                    label = { Text("Referred By (Doctor)") },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = { Icon(Icons.Default.PersonAdd, null) },
                    singleLine = true
                )

                HorizontalDivider()

                Text("Treatment Details", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)

                OutlinedTextField(
                    value = perSessionCharge,
                    onValueChange = { perSessionCharge = it.filter { c -> c.isDigit() } },
                    label = { Text("Per-Session Charge (₹) *") },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = { Icon(Icons.Default.CurrencyRupee, null) },
                    singleLine = true
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = plannedSessionsPerWeek,
                        onValueChange = { plannedSessionsPerWeek = it.filter { c -> c.isDigit() } },
                        label = { Text("Sessions/Week") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = totalSessionsPlanned,
                        onValueChange = { totalSessionsPlanned = it.filter { c -> c.isDigit() } },
                        label = { Text("Total Planned") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                Row(
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Switch(
                        checked = isHomeVisitAvailable,
                        onCheckedChange = { isHomeVisitAvailable = it }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Home visits available")
                }

                if (isHomeVisitAvailable) {
                    OutlinedTextField(
                        value = homeVisitCharge,
                        onValueChange = { homeVisitCharge = it.filter { c -> c.isDigit() } },
                        label = { Text("Home Visit Charge (₹)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    maxLines = 5
                )

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}
