package com.physiocare.manager.ui.screens.settings

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import com.physiocare.manager.util.BackupManager
import com.physiocare.manager.viewmodel.SettingsViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    backupManager: BackupManager,
    context: Context,
    onNavigateBack: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    val scope = rememberCoroutineScope()

    var clinicNameDialog by remember { mutableStateOf(false) }
    var reminderDialog by remember { mutableStateOf(false) }
    var pinDialog by remember { mutableStateOf(false) }
    var themeExpanded by remember { mutableStateOf(false) }
    var clinicNameInput by remember { mutableStateOf("") }
    var reminderHour by remember { mutableStateOf(20) }
    var reminderMinute by remember { mutableStateOf(0) }
    var pinInput by remember { mutableStateOf("") }

    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        uri?.let {
            scope.launch {
                try {
                    val backupFile = backupManager.createBackup()
                    context.contentResolver.openOutputStream(it)?.use { os ->
                        os.write(backupFile.readBytes())
                    }
                    Toast.makeText(context, "Backup saved!", Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    val importLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let {
            scope.launch {
                try {
                    val inputStream = context.contentResolver.openInputStream(it)
                    val tempFile = java.io.File(context.cacheDir, "restore_temp.json")
                    tempFile.writeText(inputStream?.bufferedReader()?.readText() ?: "")
                    backupManager.restoreBackup(tempFile)
                    Toast.makeText(context, "Backup restored! Restart the app.", Toast.LENGTH_LONG).show()
                } catch (e: Exception) {
                    Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings", fontWeight = FontWeight.Bold) },
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
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Clinic Info
            Text(
                "Clinic Information",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                onClick = {
                    clinicNameInput = state.clinicName
                    clinicNameDialog = true
                }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Business, null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Clinic Name", style = MaterialTheme.typography.bodySmall)
                        Text(state.clinicName, fontWeight = FontWeight.SemiBold)
                    }
                    Icon(Icons.Default.ChevronRight, null)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Reminders
            Text(
                "Reminders",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                onClick = {
                    reminderHour = state.reminderHour
                    reminderMinute = state.reminderMinute
                    reminderDialog = true
                }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Notifications, null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Daily Attendance Reminder", style = MaterialTheme.typography.bodySmall)
                        val ampm = if (reminderHour < 12) "AM" else "PM"
                        val h = if (reminderHour == 0) 12 else if (reminderHour > 12) reminderHour - 12 else reminderHour
                        Text("${h}:${String.format("%02d", reminderMinute)} $ampm", fontWeight = FontWeight.SemiBold)
                    }
                    Icon(Icons.Default.ChevronRight, null)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Appearance
            Text(
                "Appearance",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.DarkMode, null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Theme", style = MaterialTheme.typography.bodySmall)
                        Text(
                            when (state.darkMode) {
                                "light" -> "Light"
                                "dark" -> "Dark"
                                else -> "System Default"
                            },
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    ExposedDropdownMenuBox(
                        expanded = themeExpanded,
                        onExpandedChange = { themeExpanded = it }
                    ) {
                        OutlinedButton(
                            onClick = { themeExpanded = true },
                            modifier = Modifier.menuAnchor()
                        ) {
                            Text(
                                when (state.darkMode) {
                                    "light" -> "Light"
                                    "dark" -> "Dark"
                                    else -> "Auto"
                                }
                            )
                        }
                        ExposedDropdownMenu(
                            expanded = themeExpanded,
                            onDismissRequest = { themeExpanded = false }
                        ) {
                            listOf("system" to "Auto", "light" to "Light", "dark" to "Dark").forEach { (value, label) ->
                                DropdownMenuItem(
                                    text = { Text(label) },
                                    onClick = {
                                        viewModel.setDarkMode(value)
                                        themeExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Security
            Text(
                "Security",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                onClick = {
                    pinInput = ""
                    pinDialog = true
                }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Lock, null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("App Lock (PIN)", style = MaterialTheme.typography.bodySmall)
                        Text(
                            if (state.appLockEnabled) "Enabled" else "Disabled",
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Switch(
                        checked = state.appLockEnabled,
                        onCheckedChange = { viewModel.toggleAppLock(it) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Backup & Restore
            Text(
                "Backup & Restore",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                onClick = { exportLauncher.launch("physiocare_backup.json") }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.CloudUpload, null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Export Backup", fontWeight = FontWeight.SemiBold)
                        Text("Save all data as JSON file", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                onClick = { importLauncher.launch(arrayOf("application/json")) }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.CloudDownload, null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Restore Backup", fontWeight = FontWeight.SemiBold)
                        Text("Import data from a backup file", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // About
            Text(
                "About",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("PhysioCare Manager", fontWeight = FontWeight.Bold)
                    Text("Version 1.0.0", style = MaterialTheme.typography.bodySmall)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "Offline-first physiotherapy practice management. Your data stays on your device.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    // Clinic Name Dialog
    if (clinicNameDialog) {
        AlertDialog(
            onDismissRequest = { clinicNameDialog = false },
            title = { Text("Clinic Name") },
            text = {
                OutlinedTextField(
                    value = clinicNameInput,
                    onValueChange = { clinicNameInput = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    label = { Text("Clinic name shown on bills") }
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.updateClinicName(clinicNameInput)
                    clinicNameDialog = false
                }) { Text("Save") }
            },
            dismissButton = {
                TextButton(onClick = { clinicNameDialog = false }) { Text("Cancel") }
            }
        )
    }

    // Reminder Dialog
    if (reminderDialog) {
        AlertDialog(
            onDismissRequest = { reminderDialog = false },
            title = { Text("Set Reminder Time") },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Hour
                        var hour by remember { mutableStateOf(reminderHour) }
                        var minute by remember { mutableStateOf(reminderMinute) }

                        Column {
                            Text("Hour", style = MaterialTheme.typography.labelSmall)
                            OutlinedTextField(
                                value = hour.toString(),
                                onValueChange = { hour = it.toIntOrNull()?.coerceIn(0, 23) ?: hour },
                                modifier = Modifier.width(80.dp),
                                singleLine = true
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text("Minute", style = MaterialTheme.typography.labelSmall)
                            OutlinedTextField(
                                value = minute.toString(),
                                onValueChange = { minute = it.toIntOrNull()?.coerceIn(0, 59) ?: minute },
                                modifier = Modifier.width(80.dp),
                                singleLine = true
                            )
                        }

                        LaunchedEffect(Unit) {
                            // Initialize from state
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.updateReminderTime(reminderHour, reminderMinute)
                    reminderDialog = false
                }) { Text("Save") }
            },
            dismissButton = {
                TextButton(onClick = { reminderDialog = false }) { Text("Cancel") }
            }
        )
    }

    // PIN Dialog
    if (pinDialog) {
        AlertDialog(
            onDismissRequest = { pinDialog = false },
            title = { Text("Set PIN") },
            text = {
                OutlinedTextField(
                    value = pinInput,
                    onValueChange = { if (it.length <= 6) pinInput = it.filter { c -> c.isDigit() } },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Enter 4-6 digit PIN") },
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.setAppLockPin(pinInput)
                    pinDialog = false
                }) { Text("Save") }
            },
            dismissButton = {
                TextButton(onClick = { pinDialog = false }) { Text("Cancel") }
            }
        )
    }
}
