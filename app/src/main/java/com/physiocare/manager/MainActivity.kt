package com.physiocare.manager

import android.os.Bundle
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
import androidx.datastore.preferences.core.Preferences
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.google.firebase.FirebaseApp
import com.physiocare.manager.data.sync.CloudSyncManager
import com.physiocare.manager.ui.navigation.AppNavigation
import com.physiocare.manager.ui.screens.auth.LoginSignupScreen
import com.physiocare.manager.ui.screens.onboarding.OnboardingScreen
import com.physiocare.manager.ui.theme.PhysioCareTheme
import com.physiocare.manager.viewmodel.AuthViewModel
import com.physiocare.manager.viewmodel.SettingsViewModel
import com.physiocare.manager.viewmodel.dataStore

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

            // Sync data from cloud when user logs in
            LaunchedEffect(authState.isLoggedIn) {
                if (authState.isLoggedIn && !hasSyncedOnLogin) {
                    hasSyncedOnLogin = true
                    syncDataFromCloud()
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

    private fun syncDataFromCloud() {
        Thread {
            try {
                val database = com.physiocare.manager.data.local.AppDatabase.getInstance(this)
                val (patients, sessions, payments) = appContainer.cloudSyncManager.syncFromCloud()

                // Insert downloaded data into local database
                if (patients.isNotEmpty()) {
                    patients.forEach { database.patientDao().insert(it) }
                }
                if (sessions.isNotEmpty()) {
                    sessions.forEach { database.sessionDao().insert(it) }
                }
                if (payments.isNotEmpty()) {
                    payments.forEach { database.paymentDao().insert(it) }
                }
            } catch (e: Exception) {
                // Silently fail
            }
        }.start()
    }
}
