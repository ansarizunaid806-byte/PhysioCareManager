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
import androidx.compose.ui.Modifier
import androidx.datastore.preferences.core.Preferences
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

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize Firebase
        FirebaseApp.initializeApp(this)

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

            PhysioCareTheme(darkTheme = darkTheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    when {
                        // Not logged in → Show login screen
                        !authState.isLoggedIn -> {
                            LoginSignupScreen(viewModel = authVm)
                        }
                        // Logged in but not onboarded → Show onboarding
                        !settingsState.onboardingComplete && !settingsState.isLoading -> {
                            OnboardingScreen(
                                onComplete = { clinicName, therapistName ->
                                    settingsVm.completeOnboarding(clinicName, therapistName)
                                }
                            )
                        }
                        // Logged in and onboarded → Show main app
                        !settingsState.isLoading -> {
                            val navController = rememberNavController()
                            AppNavigation(navController = navController)
                        }
                    }
                }
            }
        }
    }
}
