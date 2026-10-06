package com.physiocare.manager.di

import android.content.Context
import com.physiocare.manager.data.local.AppDatabase
import com.physiocare.manager.data.repository.PatientRepository
import com.physiocare.manager.data.repository.PaymentRepository
import com.physiocare.manager.data.repository.SessionRepository
import com.physiocare.manager.util.BackupManager
import com.physiocare.manager.util.CsvExporter
import com.physiocare.manager.util.PdfGenerator

class AppContainer(context: Context) {
    private val database = AppDatabase.getInstance(context)

    val patientRepository = PatientRepository(database.patientDao())
    val sessionRepository = SessionRepository(database.sessionDao())
    val paymentRepository = PaymentRepository(database.paymentDao())
    val pdfGenerator = PdfGenerator(context)
    val csvExporter = CsvExporter(context)
    val backupManager = BackupManager(
        context, patientRepository, sessionRepository, paymentRepository
    )
}
