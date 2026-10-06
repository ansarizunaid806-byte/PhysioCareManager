package com.physiocare.manager.ui.screens.legal

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacyPolicyScreen(onNavigateBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Privacy Policy", fontWeight = FontWeight.Bold) },
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
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                "Privacy Policy",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                "Last updated: October 2026",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                "PhysioCare Manager is committed to protecting your privacy. This privacy policy explains how we handle your data.",
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(16.dp))

            SectionTitle("1. Data Storage")
            BodyText("All patient data, session records, and payment information is stored LOCALLY on your device using an encrypted SQLite database. No data is sent to any server or cloud service.")

            Spacer(modifier = Modifier.height(12.dp))

            SectionTitle("2. No Internet Required")
            BodyText("PhysioCare Manager works completely offline. The app does not require an internet connection for any of its core features. Your data never leaves your device.")

            Spacer(modifier = Modifier.height(12.dp))

            SectionTitle("3. No Data Collection")
            BodyText("We do not collect, store, or transmit any personal information. We do not use analytics services, crash reporting tools, or any third-party data collection.")

            Spacer(modifier = Modifier.height(12.dp))

            SectionTitle("4. Backup Files")
            BodyText("When you create a backup, the backup file is stored locally on your device. You have full control over these files and can delete them at any time.")

            Spacer(modifier = Modifier.height(12.dp))

            SectionTitle("5. App Lock")
            BodyText("The app provides an optional PIN-based lock feature to protect patient data from unauthorized access on your device.")

            Spacer(modifier = Modifier.height(12.dp))

            SectionTitle("6. Third-Party Services")
            BodyText("When you share a bill via WhatsApp or other apps, only the specific file you choose to share is transmitted. We do not have access to this data.")

            Spacer(modifier = Modifier.height(12.dp))

            SectionTitle("7. Children's Privacy")
            BodyText("This app is designed for healthcare professionals and is not intended for use by children under 13 years of age.")

            Spacer(modifier = Modifier.height(12.dp))

            SectionTitle("8. Changes to This Policy")
            BodyText("We may update this privacy policy from time to time. Any changes will be reflected in the app with an updated 'Last updated' date.")

            Spacer(modifier = Modifier.height(12.dp))

            SectionTitle("9. Contact")
            BodyText("If you have any questions about this privacy policy, please contact us through the app's Settings page.")

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.primary
    )
    Spacer(modifier = Modifier.height(4.dp))
}

@Composable
private fun BodyText(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurface,
        lineHeight = 20.sp
    )
}
