package com.physiocare.manager.ui.screens.payment

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.physiocare.manager.data.repository.PatientRepository
import com.physiocare.manager.util.CurrencyUtils
import com.physiocare.manager.viewmodel.PaymentViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordPaymentScreen(
    viewModel: PaymentViewModel,
    patientId: Long,
    patientRepository: PatientRepository,
    onNavigateBack: () -> Unit
) {
    val outstanding by viewModel.outstanding.collectAsState()
    val dueSessions by viewModel.dueSessions.collectAsState()

    var patientName by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var mode by remember { mutableStateOf("Cash") }
    var note by remember { mutableStateOf("") }
    var modeExpanded by remember { mutableStateOf(false) }
    var showSuccess by remember { mutableStateOf(false) }

    LaunchedEffect(patientId) {
        patientRepository.getPatientByIdOnce(patientId)?.let {
            patientName = it.fullName
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Record Payment", fontWeight = FontWeight.Bold)
                        Text(
                            patientName,
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Outstanding balance card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = if (outstanding > 0)
                        MaterialTheme.colorScheme.errorContainer
                    else MaterialTheme.colorScheme.tertiaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "Outstanding Balance",
                        style = MaterialTheme.typography.titleSmall
                    )
                    Text(
                        CurrencyUtils.format(outstanding),
                        style = MaterialTheme.typography.displayMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (outstanding > 0)
                            MaterialTheme.colorScheme.onErrorContainer
                        else MaterialTheme.colorScheme.onTertiaryContainer
                    )
                    Text(
                        "${dueSessions.size} sessions unpaid",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            // Amount input
            OutlinedTextField(
                value = amount,
                onValueChange = { amount = it.filter { c -> c.isDigit() } },
                label = { Text("Amount (₹)") },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(Icons.Default.CurrencyRupee, null) },
                singleLine = true,
                textStyle = MaterialTheme.typography.headlineMedium
            )

            // Quick amounts
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val quickAmounts = listOf(500, 1000, 2000, outstanding)
                quickAmounts.filter { it > 0 }.distinct().take(4).forEach { amt ->
                    OutlinedButton(
                        onClick = { amount = amt.toString() },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("₹$amt")
                    }
                }
            }

            // Payment mode
            ExposedDropdownMenuBox(
                expanded = modeExpanded,
                onExpandedChange = { modeExpanded = it }
            ) {
                OutlinedTextField(
                    value = mode,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Payment Mode") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = modeExpanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded = modeExpanded,
                    onDismissRequest = { modeExpanded = false }
                ) {
                    listOf("Cash", "UPI", "Card", "Other").forEach { m ->
                        DropdownMenuItem(
                            text = { Text(m) },
                            onClick = { mode = m; modeExpanded = false }
                        )
                    }
                }
            }

            // Note
            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                label = { Text("Note (optional)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.weight(1f))

            // Submit
            Button(
                onClick = {
                    val amt = amount.toIntOrNull() ?: return@Button
                    if (amt <= 0) return@Button
                    viewModel.recordPayment(amt, mode, note.ifBlank { null })
                    amount = ""
                    note = ""
                    showSuccess = true
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = amount.toIntOrNull()?.let { it > 0 } == true,
                shape = MaterialTheme.shapes.large
            ) {
                Icon(Icons.Default.CheckCircle, null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Record Payment", style = MaterialTheme.typography.titleMedium)
            }
        }
    }

    if (showSuccess) {
        AlertDialog(
            onDismissRequest = { showSuccess = false },
            title = { Text("Payment Recorded") },
            text = { Text("Payment has been successfully recorded. Due sessions have been auto-marked as paid.") },
            confirmButton = {
                TextButton(onClick = { showSuccess = false }) {
                    Text("OK")
                }
            }
        )
    }
}
