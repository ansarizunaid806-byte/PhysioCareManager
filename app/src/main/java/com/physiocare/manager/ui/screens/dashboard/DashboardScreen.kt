package com.physiocare.manager.ui.screens.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.physiocare.manager.ui.components.*
import com.physiocare.manager.ui.theme.*
import com.physiocare.manager.util.CurrencyUtils
import com.physiocare.manager.viewmodel.DashboardViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    onNavigateToPatients: () -> Unit,
    onNavigateToPatient: (Long) -> Unit,
    onNavigateToDues: () -> Unit,
    onNavigateToReports: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "PhysioCare Manager",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "Today's Dashboard",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Default.Settings, "Settings")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            Column(horizontalAlignment = Alignment.End) {
                SmallFloatingActionButton(
                    onClick = onNavigateToDues,
                    containerColor = if (state.patientsWithDues.isNotEmpty()) MaterialTheme.colorScheme.error
                    else MaterialTheme.colorScheme.secondaryContainer
                ) {
                    Badge { Text(state.patientsWithDues.size.toString()) }
                }
                Spacer(modifier = Modifier.height(12.dp))
                FloatingActionButton(
                    onClick = onNavigateToPatients,
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(Icons.Default.People, "Patients")
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Monthly Stats
            item {
                Text(
                    "This Month",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        StatCard(
                            title = "Sessions",
                            value = state.monthlySessions.toString(),
                            icon = Icons.Default.EventAvailable,
                            modifier = Modifier.weight(1f)
                        )
                        StatCard(
                            title = "Billed",
                            value = CurrencyUtils.format(state.monthlyBilled),
                            icon = Icons.Default.Receipt,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        StatCard(
                            title = "Collected",
                            value = CurrencyUtils.format(state.monthlyCollected),
                            icon = Icons.Default.AccountBalanceWallet,
                            modifier = Modifier.weight(1f),
                            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                            contentColor = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                        StatCard(
                            title = "Pending",
                            value = CurrencyUtils.format(state.monthlyPending),
                            icon = Icons.Default.Warning,
                            modifier = Modifier.weight(1f),
                            containerColor = if (state.monthlyPending > 0)
                                MaterialTheme.colorScheme.errorContainer
                            else MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = if (state.monthlyPending > 0)
                                MaterialTheme.colorScheme.onErrorContainer
                            else MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
            }

            // Quick navigation
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onNavigateToDues,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.MoneyOff, null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Dues (${state.patientsWithDues.size})")
                    }
                    OutlinedButton(
                        onClick = onNavigateToReports,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.BarChart, null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Reports")
                    }
                }
            }

            // Alerts
            if (state.absentPatients.isNotEmpty()) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                "⚠️ Dropout Risk",
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "${state.absentPatients.size} patient(s) absent for 7+ days",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                }
            }

            // Today's attendance
            item {
                Text(
                    "Today's Attendance",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            if (state.todaysPatients.isEmpty()) {
                item {
                    EmptyState(
                        icon = Icons.Default.EventBusy,
                        title = "No active patients",
                        subtitle = "Add patients to start tracking attendance"
                    )
                }
            }

            items(state.todaysPatients.take(20)) { patient ->
                AttendanceQuickAction(
                    patient = patient,
                    currentStatus = state.todaysSessions[patient.id]?.status,
                    onMarkPresent = { viewModel.markAttendance(patient.id, "Present", patient) },
                    onMarkAbsent = { viewModel.markAttendance(patient.id, "Absent", patient) },
                    onMarkCancelled = { viewModel.markAttendance(patient.id, "Cancelled", patient) }
                )
            }

            if (state.todaysPatients.size > 20) {
                item {
                    TextButton(
                        onClick = onNavigateToPatients,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("View all ${state.todaysPatients.size} patients →")
                    }
                }
            }

            // Spacer for FAB
            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}
