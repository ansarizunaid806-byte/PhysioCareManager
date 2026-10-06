package com.physiocare.manager.ui.screens.reports

import android.content.Context
import android.content.Intent
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import com.physiocare.manager.ui.components.*
import com.physiocare.manager.ui.theme.*
import com.physiocare.manager.util.CurrencyUtils
import com.physiocare.manager.util.CsvExporter
import com.physiocare.manager.util.DateUtils
import com.physiocare.manager.viewmodel.ReportsViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(
    viewModel: ReportsViewModel,
    csvExporter: CsvExporter,
    context: Context,
    onNavigateBack: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Monthly Report", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        scope.launch {
                            try {
                                val monthYear = DateUtils.formatMonthYear(
                                    DateUtils.getStartOfMonth(state.selectedYear, state.selectedMonth)
                                )
                                val file = csvExporter.exportMonthlyReport(
                                    patients = viewModel.patientsForExport.value,
                                    sessions = viewModel.sessionsForExport.value,
                                    payments = viewModel.paymentsForExport.value,
                                    monthYear = monthYear
                                )
                                val uri = FileProvider.getUriForFile(
                                    context,
                                    "${context.packageName}.fileprovider",
                                    file
                                )
                                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/csv"
                                    putExtra(Intent.EXTRA_STREAM, uri)
                                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                }
                                context.startActivity(Intent.createChooser(shareIntent, "Export Report"))
                            } catch (e: Exception) {
                                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }) {
                        Icon(Icons.Default.Share, "Export CSV")
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
                Card(shape = RoundedCornerShape(12.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = {
                            var m = state.selectedMonth - 1
                            var y = state.selectedYear
                            if (m < 0) { m = 11; y-- }
                            viewModel.changeMonth(y, m)
                        }) {
                            Icon(Icons.Default.ChevronLeft, "Previous")
                        }
                        Text(
                            DateUtils.formatMonthYear(
                                DateUtils.getStartOfMonth(state.selectedYear, state.selectedMonth)
                            ),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        IconButton(onClick = {
                            var m = state.selectedMonth + 1
                            var y = state.selectedYear
                            if (m > 11) { m = 0; y++ }
                            viewModel.changeMonth(y, m)
                        }) {
                            Icon(Icons.Default.ChevronRight, "Next")
                        }
                    }
                }
            }

            // Summary cards
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StatCard(
                        title = "Sessions",
                        value = state.totalSessions.toString(),
                        icon = Icons.Default.EventAvailable,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "Charges",
                        value = CurrencyUtils.format(state.totalCharges),
                        icon = Icons.Default.Receipt,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StatCard(
                        title = "Collected",
                        value = CurrencyUtils.format(state.totalCollected),
                        icon = Icons.Default.AccountBalanceWallet,
                        modifier = Modifier.weight(1f),
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                        contentColor = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                    StatCard(
                        title = "Pending",
                        value = CurrencyUtils.format(state.totalPending),
                        icon = Icons.Default.Warning,
                        modifier = Modifier.weight(1f),
                        containerColor = if (state.totalPending > 0)
                            MaterialTheme.colorScheme.errorContainer
                        else MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = if (state.totalPending > 0)
                            MaterialTheme.colorScheme.onErrorContainer
                        else MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }

            // Per-patient breakdown
            item {
                Text(
                    "Patient Breakdown",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }

            items(state.monthReport) { report ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    report.patient.fullName,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    "${report.sessionsAttended} sessions × ₹${report.patient.perSessionCharge}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    CurrencyUtils.format(report.totalCharges),
                                    fontWeight = FontWeight.SemiBold
                                )
                                if (report.balance > 0) {
                                    Text(
                                        "Due: ${CurrencyUtils.format(report.balance)}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = PaymentDue
                                    )
                                }
                            }
                        }
                    }
                }
            }

            if (state.monthReport.isEmpty() && !state.isLoading) {
                item {
                    EmptyState(
                        icon = Icons.Default.BarChart,
                        title = "No data for this month",
                        subtitle = "Sessions and payments will appear here"
                    )
                }
            }
        }
    }
}
