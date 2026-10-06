package com.physiocare.manager.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "sessions",
    foreignKeys = [
        ForeignKey(
            entity = PatientEntity::class,
            parentColumns = ["id"],
            childColumns = ["patientId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("patientId"), Index("date")]
)
data class SessionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val patientId: Long,
    val date: Long, // epoch millis for the date
    val status: String, // Present, Absent, Cancelled
    val charge: Int = 0, // charge for this session
    val paymentStatus: String = "Due", // Paid, Due
    val isHomeVisit: Boolean = false,
    val note: String? = null,
    val painLevel: Int? = null, // 1-10
    val createdAt: Long = System.currentTimeMillis()
)
