package com.physiocare.manager

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import com.physiocare.manager.di.AppContainer

class PhysioCareApp : Application() {

    lateinit var appContainer: AppContainer

    override fun onCreate() {
        super.onCreate()
        appContainer = AppContainer(this)
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        val attendanceChannel = NotificationChannel(
            CHANNEL_ATTENDANCE,
            "Attendance Reminder",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Daily reminder to mark attendance"
        }

        val paymentChannel = NotificationChannel(
            CHANNEL_PAYMENT,
            "Payment Alerts",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Alerts for pending payments"
        }

        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(attendanceChannel)
        manager.createNotificationChannel(paymentChannel)
    }

    companion object {
        const val CHANNEL_ATTENDANCE = "attendance_reminder"
        const val CHANNEL_PAYMENT = "payment_alert"
    }
}
