package com.physiocare.manager.ui.screens.billing

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import com.physiocare.manager.ui.components.*
import com.physiocare.manager.ui.theme.*
import com.physiocare.manager.util.CurrencyUtils
import com.physiocare.manager.util.DateUtils
import com.physiocare.manager.util.PdfGenerator
import com.physiocare.manager.viewmodel.PatientProfileViewModel
import com.physiocare.manager.viewmodel.SettingsViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BillStatementScreen(
    viewModel: PatientProfileViewModel,
    pdfGenerator: PdfGenerator,
    settingsViewModel: SettingsViewModel,
    context: Context,
    onNavigateBack: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    val settingsState by settingsViewModel.state.collectAsState()
    val scope = rememberCoroutineScope()

    var selectedYear by remember { mutableStateOf(DateUtils.getYear(System.currentTimeMillis())) }
    var selectedMonth by remember { mutableStateOf(DateUtils.getMonth(System.currentTimeMillis())) }
    var monthExpanded by remember { mutableStateOf(false) }

    val monthNames = listOf(
        "January", "February", "March", "April", "May", "June",
        "July", "August", "September", "October", "November", "December"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Bill Statement", fontWeight = FontWeight.Bold)
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
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Month selector
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = {
                        if (selectedMonth == 0) { selectedMonth = 11; selectedYear-- }
                        else selectedMonth--
                        viewModel.changeMonth(selectedYear, selectedMonth)
                    }) {
                        Icon(Icons.Default.ChevronLeft, "Previous")
                    }

                    Text(
                        "${monthNames[selectedMonth]} $selectedYear",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    IconButton(onClick = {
                        if (selectedMonth == 11) { selectedMonth = 0; selectedYear++ }
                        else selectedMonth++
                        viewModel.changeMonth(selectedYear, selectedMonth)
                    }) {
                        Icon(Icons.Default.ChevronRight, "Next")
                    }
                }
            }

            // Summary
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Sessions Attended", style = MaterialTheme.typography.bodyMedium)
                            Text(
                                "${state.presentCount}",
                                fontWeight = FontWeight.Bold
                            )
                        }
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Rate/Session", style = MaterialTheme.typography.bodyMedium)
                            Text(
                                CurrencyUtils.format(state.patient?.perSessionCharge ?: 0),
                                fontWeight = FontWeight.Bold
                            )
                        }
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Total Charges", style = MaterialTheme.typography.titleMedium)
                            Text(
                                CurrencyUtils.format(state.monthlyCharges),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Total Paid", style = MaterialTheme.typography.bodyMedium)
                            Text(
                                CurrencyUtils.format(state.totalPaid),
                                fontWeight = FontWeight.Bold,
                                color = PaymentPaid
                            )
                        }
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                "Balance Due",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                CurrencyUtils.format(state.outstanding),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (state.outstanding > 0) PaymentDue else PaymentPaid
                            )
                        }
                    }
                }
            }

            // Session list for this month
            item {
                Text(
                    "Session Details",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }

            val presentSessions = state.monthlySessions.filter { it.status == "Present" }
            if (presentSessions.isEmpty()) {
                item {
                    EmptyState(
                        icon = Icons.Default.EventBusy,
                        title = "No sessions this month"
                    )
                }
            }

            items(presentSessions) { session ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            DateUtils.formatEpoch(session.date),
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                CurrencyUtils.format(session.charge),
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            PaymentStatusBadge(session.paymentStatus)
                        }
                    }
                }
            }

            // Generate & Share PDF
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {
                        val patient = state.patient ?: return@Button
                        scope.launch {
                            try {
                                val monthYear = "${monthNames[selectedMonth]} $selectedYear"
                                val file = pdfGenerator.generateBill(
                                    patient = patient,
                                    sessions = presentSessions,
                                    monthYear = monthYear,
                                    totalCharges = state.monthlyCharges,
                                    totalPaid = state.totalPaid,
                                    clinicName = settingsState.clinicName
                                )

                                // Share via WhatsApp
                                val uri = FileProvider.getUriForFile(
                                    context,
                                    "${context.packageName}.fileprovider",
                                    file
                                )
                                val whatsappIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = "application/pdf"
                                    putExtra(Intent.EXTRA_STREAM, uri)
                                    putExtra(Intent.EXTRA_SUBJECT, "Bill - ${patient.fullName} - $monthYear")
                                    setPackage("com.whatsapp")
                                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                }
                                try {
                                    context.startActivity(whatsappIntent)
                                } catch (e: Exception) {
                                    // Fallback to general share
                                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                        type = "application/pdf"
                                        putExtra(Intent.EXTRA_STREAM, uri)
                                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                    }
                                    context.startActivity(
                                        Intent.createChooser(shareIntent, "Share Bill")
                                    )
                                }
                            } catch (e: Exception) {
                                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = MaterialTheme.shapes.large
                ) {
                    Icon(Icons.Default.Share, null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Generate & Share Bill via WhatsApp", style = MaterialTheme.typography.titleMedium)
                }
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }
}
