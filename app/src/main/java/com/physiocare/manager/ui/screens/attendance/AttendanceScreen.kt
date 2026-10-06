package com.physiocare.manager.ui.screens.attendance

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
import com.physiocare.manager.util.CurrencyUtils
import com.physiocare.manager.util.DateUtils
import com.physiocare.manager.viewmodel.PatientProfileViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttendanceScreen(
    viewModel: PatientProfileViewModel,
    onNavigateBack: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    var showDeleteConfirm by remember { mutableStateOf<Long?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Attendance", fontWeight = FontWeight.Bold)
                        Text(
                            state.patient?.fullName ?: "",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                Text(
                    "Total: ${state.allSessions.size} sessions",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            items(state.allSessions, key = { it.id }) { session ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                DateUtils.formatEpoch(session.date),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            if (session.note != null) {
                                Text(
                                    session.note,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            if (session.isHomeVisit) {
                                Text(
                                    "🏠 Home Visit",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            SessionStatusBadge(session.status)
                            if (session.status == "Present") {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    CurrencyUtils.format(session.charge),
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.SemiBold
                                )
                                PaymentStatusBadge(session.paymentStatus)
                            }
                        }

                        if (session.status != "Present") {
                            IconButton(onClick = { showDeleteConfirm = session.id }) {
                                Icon(Icons.Default.Delete, "Delete", tint = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
            }
        }
    }

    // Delete confirmation
    showDeleteConfirm?.let { sessionId ->
        val session = state.allSessions.find { it.id == sessionId }
        if (session != null) {
            ConfirmDialog(
                title = "Delete Session",
                message = "Delete session on ${DateUtils.formatEpoch(session.date)}? This cannot be undone.",
                confirmText = "Delete",
                onConfirm = {
                    viewModel.deleteSession(session)
                    showDeleteConfirm = null
                },
                onDismiss = { showDeleteConfirm = null }
            )
        }
    }
}
