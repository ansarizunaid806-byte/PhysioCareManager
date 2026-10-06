package com.physiocare.manager.ui.screens.patients

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.physiocare.manager.ui.components.*
import com.physiocare.manager.viewmodel.PatientListViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PatientListScreen(
    viewModel: PatientListViewModel,
    onNavigateToPatient: (Long) -> Unit,
    onNavigateToAddPatient: () -> Unit,
    onNavigateBack: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    var showDeleteDialog by remember { mutableStateOf<Long?>(null) }
    val filterOptions = listOf("All", "Active", "Completed", "WithDues")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Patients", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToAddPatient,
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.PersonAdd, "Add Patient")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Search
            OutlinedTextField(
                value = state.searchQuery,
                onValueChange = viewModel::onSearchQueryChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                placeholder = { Text("Search by name, mobile, condition...") },
                leadingIcon = { Icon(Icons.Default.Search, null) },
                trailingIcon = {
                    if (state.searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.onSearchQueryChange("") }) {
                            Icon(Icons.Default.Clear, "Clear")
                        }
                    }
                },
                singleLine = true,
                shape = MaterialTheme.shapes.large
            )

            // Filter chips
            ScrollableTabRow(
                selectedTabIndex = filterOptions.indexOf(state.statusFilter).coerceAtLeast(0),
                modifier = Modifier.padding(horizontal = 16.dp),
                edgePadding = 0.dp,
                divider = {}
            ) {
                filterOptions.forEach { filter ->
                    Tab(
                        selected = state.statusFilter == filter,
                        onClick = { viewModel.onStatusFilterChange(filter) },
                        text = {
                            Text(
                                when (filter) {
                                    "All" -> "All"
                                    "Active" -> "Active"
                                    "Completed" -> "Completed"
                                    "WithDues" -> "With Dues"
                                    else -> filter
                                }
                            )
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (state.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (state.patients.isEmpty()) {
                EmptyState(
                    icon = Icons.Default.PeopleOutline,
                    title = "No patients found",
                    subtitle = if (state.searchQuery.isNotEmpty()) "Try a different search"
                    else "Tap + to add your first patient"
                )
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        Text(
                            "${state.patients.size} patient(s)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    items(state.patients, key = { it.id }) { patient ->
                        PatientListItem(
                            patient = patient,
                            outstandingAmount = state.outstandingMap[patient.id] ?: 0,
                            onClick = { onNavigateToPatient(patient.id) }
                        )
                    }

                    item {
                        Spacer(modifier = Modifier.height(80.dp))
                    }
                }
            }
        }
    }

    // Delete confirmation
    showDeleteDialog?.let { patientId ->
        val patient = state.patients.find { it.id == patientId }
        if (patient != null) {
            ConfirmDialog(
                title = "Delete Patient",
                message = "Are you sure you want to delete ${patient.fullName}? All sessions and payment records will also be deleted. This cannot be undone.",
                confirmText = "Delete",
                onConfirm = {
                    viewModel.deletePatient(patient)
                    showDeleteDialog = null
                },
                onDismiss = { showDeleteDialog = null }
            )
        }
    }
}
