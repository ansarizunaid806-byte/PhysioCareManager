package com.physiocare.manager

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.google.firebase.FirebaseApp
import com.physiocare.manager.ui.navigation.AppNavigation
import com.physiocare.manager.ui.screens.auth.LoginSignupScreen
import com.physiocare.manager.ui.screens.onboarding.OnboardingScreen
import com.physiocare.manager.ui.theme.PhysioCareTheme
import com.physiocare.manager.viewmodel.AuthViewModel
import com.physiocare.manager.viewmodel.SettingsViewModel
import com.physiocare.manager.viewmodel.dataStore
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private lateinit var appContainer: com.physiocare.manager.di.AppContainer
    private var hasSyncedOnLogin = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize Firebase
        FirebaseApp.initializeApp(this)

        // Initialize App Container
        appContainer = (application as PhysioCareApp).appContainer

        setContent {
            val settingsVm: SettingsViewModel = viewModel(
                factory = SettingsViewModel.Factory(this.dataStore)
            )
            val settingsState by settingsVm.state.collectAsState()

            val authVm: AuthViewModel = viewModel()
            val authState by authVm.state.collectAsState()

            val darkTheme = when (settingsState.darkMode) {
                "light" -> false
                "dark" -> true
                else -> androidx.compose.foundation.isSystemInDarkTheme()
            }

            // Sync data bidirectionally when user logs in
            LaunchedEffect(authState.isLoggedIn) {
                if (authState.isLoggedIn && !hasSyncedOnLogin) {
                    hasSyncedOnLogin = true
                    performFullSync()
                } else if (!authState.isLoggedIn) {
                    hasSyncedOnLogin = false
                }
            }

            PhysioCareTheme(darkTheme = darkTheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    when {
                        !authState.isLoggedIn -> {
                            LoginSignupScreen(viewModel = authVm)
                        }
                        !settingsState.onboardingComplete && !settingsState.isLoading -> {
                            OnboardingScreen(
                                onComplete = { clinicName, therapistName ->
                                    settingsVm.completeOnboarding(clinicName, therapistName)
                                }
                            )
                        }
                        !settingsState.isLoading -> {
                            val navController = rememberNavController()
                            AppNavigation(navController = navController)
                        }
                    }
                }
            }
        }
    }

    /**
     * Full bidirectional sync:
     * 1. Upload all local data to Firestore (so cloud has everything)
     * 2. Download all data from Firestore (so local has everything from cloud)
     * This handles both "new device" and "existing device" scenarios.
     */
    private fun performFullSync() {
        lifecycleScope.launch {
            try {
                Log.d("PhysioCare", "Starting full cloud sync...")
                val database = com.physiocare.manager.data.local.AppDatabase.getInstance(this@MainActivity)

                // Step 1: Upload ALL local data to cloud first
                val localPatients = appContainer.patientRepository.getAllPatientsOnce()
                val localSessions = appContainer.sessionRepository.getAllSessionsOnce()
                val localPayments = appContainer.paymentRepository.getAllPaymentsOnce()

                Log.d("PhysioCare", "Uploading: ${localPatients.size} patients, ${localSessions.size} sessions, ${localPayments.size} payments")

                if (localPatients.isNotEmpty() || localSessions.isNotEmpty() || localPayments.isNotEmpty()) {
                    appContainer.cloudSyncManager.syncToCloud(localPatients, localSessions, localPayments)
                    Log.d("PhysioCare", "Upload to cloud complete")
                }

                // Step 2: Download ALL data from cloud
                val (cloudPatients, cloudSessions, cloudPayments) = appContainer.cloudSyncManager.syncFromCloud()

                Log.d("PhysioCare", "Downloaded: ${cloudPatients.size} patients, ${cloudSessions.size} sessions, ${cloudPayments.size} payments")

                // Step 3: Insert downloaded data into local database (REPLACE strategy handles duplicates)
                cloudPatients.forEach { database.patientDao().insert(it) }
                cloudSessions.forEach { database.sessionDao().insert(it) }
                cloudPayments.forEach { database.paymentDao().insert(it) }

                Log.d("PhysioCare", "Full sync complete!")
            } catch (e: Exception) {
                Log.e("PhysioCare", "Sync failed: ${e.message}", e)
            }
        }
    }
}
