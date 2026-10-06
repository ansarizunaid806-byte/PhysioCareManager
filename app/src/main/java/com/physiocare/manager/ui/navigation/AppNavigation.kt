package com.physiocare.manager.ui.navigation

import androidx.compose.animation.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.physiocare.manager.PhysioCareApp
import com.physiocare.manager.ui.screens.attendance.AttendanceScreen
import com.physiocare.manager.ui.screens.billing.BillStatementScreen
import com.physiocare.manager.ui.screens.dashboard.DashboardScreen
import com.physiocare.manager.ui.screens.dues.DuesScreen
import com.physiocare.manager.ui.screens.patients.AddEditPatientScreen
import com.physiocare.manager.ui.screens.patients.PatientListScreen
import com.physiocare.manager.ui.screens.patientprofile.PatientProfileScreen
import com.physiocare.manager.ui.screens.payment.RecordPaymentScreen
import com.physiocare.manager.ui.screens.reports.ReportsScreen
import com.physiocare.manager.ui.screens.settings.SettingsScreen
import com.physiocare.manager.viewmodel.*

@Composable
fun AppNavigation(navController: NavHostController) {
    val context = LocalContext.current
    val app = context.applicationContext as PhysioCareApp
    val container = app.appContainer

    NavHost(
        navController = navController,
        startDestination = Screen.Dashboard.route
    ) {
        composable(Screen.Dashboard.route) {
            val vm: DashboardViewModel = viewModel(
                factory = DashboardViewModel.Factory(
                    container.patientRepository,
                    container.sessionRepository,
                    container.paymentRepository
                )
            )
            DashboardScreen(
                viewModel = vm,
                onNavigateToPatients = { navController.navigate(Screen.PatientList.route) },
                onNavigateToPatient = { id -> navController.navigate(Screen.PatientProfile.createRoute(id)) },
                onNavigateToDues = { navController.navigate(Screen.Dues.route) },
                onNavigateToReports = { navController.navigate(Screen.Reports.route) },
                onNavigateToSettings = { navController.navigate(Screen.Settings.route) }
            )
        }

        composable(Screen.PatientList.route) {
            val vm: PatientListViewModel = viewModel(
                factory = PatientListViewModel.Factory(
                    container.patientRepository,
                    container.sessionRepository,
                    container.paymentRepository
                )
            )
            PatientListScreen(
                viewModel = vm,
                onNavigateToPatient = { id -> navController.navigate(Screen.PatientProfile.createRoute(id)) },
                onNavigateToAddPatient = { navController.navigate(Screen.AddEditPatient.createRoute()) },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.AddEditPatient.route,
            arguments = listOf(navArgument("id") { type = NavType.LongType; defaultValue = -1L })
        ) { backStackEntry ->
            val patientId = backStackEntry.arguments?.getLong("id") ?: -1L
            AddEditPatientScreen(
                patientId = if (patientId == -1L) null else patientId,
                patientRepository = container.patientRepository,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.PatientProfile.route,
            arguments = listOf(navArgument("patientId") { type = NavType.LongType })
        ) { backStackEntry ->
            val patientId = backStackEntry.arguments?.getLong("patientId") ?: return@composable
            val vm: PatientProfileViewModel = viewModel(
                factory = PatientProfileViewModel.Factory(
                    patientId,
                    container.patientRepository,
                    container.sessionRepository,
                    container.paymentRepository
                )
            )
            PatientProfileScreen(
                viewModel = vm,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToEditPatient = { navController.navigate(Screen.AddEditPatient.createRoute(patientId)) },
                onNavigateToPayment = { navController.navigate(Screen.RecordPayment.createRoute(patientId)) },
                onNavigateToBill = { navController.navigate(Screen.BillStatement.createRoute(patientId)) },
                onNavigateToAttendance = { navController.navigate(Screen.Attendance.createRoute(patientId)) }
            )
        }

        composable(
            route = Screen.RecordPayment.route,
            arguments = listOf(navArgument("patientId") { type = NavType.LongType })
        ) { backStackEntry ->
            val patientId = backStackEntry.arguments?.getLong("patientId") ?: return@composable
            val vm: PaymentViewModel = viewModel(
                factory = PaymentViewModel.Factory(
                    patientId,
                    container.sessionRepository,
                    container.paymentRepository
                )
            )
            RecordPaymentScreen(
                viewModel = vm,
                patientId = patientId,
                patientRepository = container.patientRepository,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Dues.route) {
            val vm: DuesViewModel = viewModel(
                factory = DuesViewModel.Factory(
                    container.patientRepository,
                    container.sessionRepository,
                    container.paymentRepository
                )
            )
            DuesScreen(
                viewModel = vm,
                onNavigateToPatient = { id -> navController.navigate(Screen.PatientProfile.createRoute(id)) },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Reports.route) {
            val vm: ReportsViewModel = viewModel(
                factory = ReportsViewModel.Factory(
                    container.patientRepository,
                    container.sessionRepository,
                    container.paymentRepository
                )
            )
            ReportsScreen(
                viewModel = vm,
                csvExporter = container.csvExporter,
                context = context,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Settings.route) {
            val vm: SettingsViewModel = viewModel(
                factory = SettingsViewModel.Factory(context.dataStore)
            )
            SettingsScreen(
                viewModel = vm,
                backupManager = container.backupManager,
                context = context,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.Attendance.route,
            arguments = listOf(navArgument("patientId") { type = NavType.LongType })
        ) { backStackEntry ->
            val patientId = backStackEntry.arguments?.getLong("patientId") ?: return@composable
            val vm: PatientProfileViewModel = viewModel(
                factory = PatientProfileViewModel.Factory(
                    patientId,
                    container.patientRepository,
                    container.sessionRepository,
                    container.paymentRepository
                )
            )
            AttendanceScreen(
                viewModel = vm,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.BillStatement.route,
            arguments = listOf(navArgument("patientId") { type = NavType.LongType })
        ) { backStackEntry ->
            val patientId = backStackEntry.arguments?.getLong("patientId") ?: return@composable
            val vm: PatientProfileViewModel = viewModel(
                factory = PatientProfileViewModel.Factory(
                    patientId,
                    container.patientRepository,
                    container.sessionRepository,
                    container.paymentRepository
                )
            )
            BillStatementScreen(
                viewModel = vm,
                pdfGenerator = container.pdfGenerator,
                settingsViewModel = viewModel(factory = SettingsViewModel.Factory(context.dataStore)),
                context = context,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
