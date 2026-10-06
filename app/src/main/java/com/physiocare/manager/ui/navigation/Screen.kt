package com.physiocare.manager.ui.navigation

sealed class Screen(val route: String) {
    object Dashboard : Screen("dashboard")
    object PatientList : Screen("patients")
    object AddEditPatient : Screen("add_patient?id={id}") {
        fun createRoute(patientId: Long? = null) = if (patientId != null) "add_patient?id=$patientId" else "add_patient"
    }
    object PatientProfile : Screen("patient/{patientId}") {
        fun createRoute(patientId: Long) = "patient/$patientId"
    }
    object RecordPayment : Screen("payment/{patientId}") {
        fun createRoute(patientId: Long) = "payment/$patientId"
    }
    object Dues : Screen("dues")
    object Reports : Screen("reports")
    object Settings : Screen("settings")
    object Attendance : Screen("attendance/{patientId}") {
        fun createRoute(patientId: Long) = "attendance/$patientId"
    }
    object BillStatement : Screen("bill/{patientId}") {
        fun createRoute(patientId: Long) = "bill/$patientId"
    }
    object PrivacyPolicy : Screen("privacy_policy")
}
