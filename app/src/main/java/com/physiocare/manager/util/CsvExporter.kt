package com.physiocare.manager.util

import android.content.Context
import com.physiocare.manager.data.local.entity.PatientEntity
import com.physiocare.manager.data.local.entity.PaymentEntity
import com.physiocare.manager.data.local.entity.SessionEntity
import java.io.File
import java.io.FileOutputStream

class CsvExporter(private val context: Context) {

    fun exportMonthlyReport(
        patients: List<PatientEntity>,
        sessions: List<SessionEntity>,
        payments: List<PaymentEntity>,
        monthYear: String
    ): File {
        val sb = StringBuilder()

        // Header
        sb.appendLine("Monthly Income Report - $monthYear")
        sb.appendLine()
        sb.appendLine("Patient Name,Mobile,Condition,Sessions Attended,Total Charges,Payments Received,Balance Due")

        for (patient in patients) {
            val patientSessions = sessions.filter { it.patientId == patient.id && it.status == "Present" }
            val patientPayments = payments.filter { it.patientId == patient.id }
            val totalCharges = patientSessions.sumOf { it.charge }
            val totalPaid = patientPayments.sumOf { it.amount }
            val balance = totalCharges - totalPaid

            sb.appendLine(
                "${patient.fullName},${patient.mobileNumber},${patient.condition}," +
                "${patientSessions.size},$totalCharges,$totalPaid,$balance"
            )
        }

        // Summary
        val totalAllCharges = sessions.filter { it.status == "Present" }.sumOf { it.charge }
        val totalAllPaid = payments.sumOf { it.amount }
        sb.appendLine()
        sb.appendLine("SUMMARY")
        sb.appendLine("Total Sessions,${sessions.count { it.status == "Present" }}")
        sb.appendLine("Total Charges,$totalAllCharges")
        sb.appendLine("Total Collected,$totalAllPaid")
        sb.appendLine("Total Pending,${totalAllCharges - totalAllPaid}")

        val exportDir = File(context.cacheDir, "reports")
        if (!exportDir.exists()) exportDir.mkdirs()

        val fileName = "monthly_report_${monthYear.replace(" ", "_")}.csv"
        val file = File(exportDir, fileName)
        FileOutputStream(file).use { fos ->
            fos.write(sb.toString().toByteArray())
        }
        return file
    }
}
