package com.physiocare.manager.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "patients")
data class PatientEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val fullName: String,
    val mobileNumber: String,
    val age: Int,
    val gender: String, // Male, Female, Other
    val condition: String, // diagnosis
    val referredBy: String? = null,
    val startDate: Long, // epoch millis
    val status: String = "Active", // Active, Completed, Dropped
    val perSessionCharge: Int, // in rupees
    val plannedSessionsPerWeek: Int? = null,
    val totalSessionsPlanned: Int? = null,
    val notes: String? = null,
    val isHomeVisitAvailable: Boolean = false,
    val homeVisitCharge: Int? = null,
    val nextAppointmentDate: Long? = null,
    val packageSessionsTotal: Int? = null, // e.g., 12 for 12-session package
    val packageSessionsUsed: Int = 0,
    val packageAmount: Int? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
