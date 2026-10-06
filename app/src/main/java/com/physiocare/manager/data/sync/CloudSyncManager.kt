package com.physiocare.manager.data.sync

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.physiocare.manager.data.local.AppDatabase
import com.physiocare.manager.data.local.entity.PatientEntity
import com.physiocare.manager.data.local.entity.PaymentEntity
import com.physiocare.manager.data.local.entity.SessionEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

/**
 * CloudSyncManager handles syncing local Room database with Firebase Firestore.
 * Strategy: Upload all local data to Firestore, then listen for remote changes.
 */
class CloudSyncManager {

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    /**
     * Upload all local data to Firestore for the current user.
     * Call this after login or when data changes significantly.
     */
    suspend fun syncToCloud(
        patients: List<PatientEntity>,
        sessions: List<SessionEntity>,
        payments: List<PaymentEntity>
    ) = withContext(Dispatchers.IO) {
        val userId = auth.currentUser?.uid ?: return@withContext

        try {
            // Upload patients
            for (patient in patients) {
                val data = hashMapOf(
                    "localId" to patient.id,
                    "fullName" to patient.fullName,
                    "mobileNumber" to patient.mobileNumber,
                    "age" to patient.age,
                    "gender" to patient.gender,
                    "condition" to patient.condition,
                    "referredBy" to patient.referredBy,
                    "startDate" to patient.startDate,
                    "status" to patient.status,
                    "perSessionCharge" to patient.perSessionCharge,
                    "plannedSessionsPerWeek" to patient.plannedSessionsPerWeek,
                    "totalSessionsPlanned" to patient.totalSessionsPlanned,
                    "notes" to patient.notes,
                    "isHomeVisitAvailable" to patient.isHomeVisitAvailable,
                    "homeVisitCharge" to patient.homeVisitCharge,
                    "nextAppointmentDate" to patient.nextAppointmentDate,
                    "packageSessionsTotal" to patient.packageSessionsTotal,
                    "packageSessionsUsed" to patient.packageSessionsUsed,
                    "packageAmount" to patient.packageAmount,
                    "createdAt" to patient.createdAt,
                    "updatedAt" to patient.updatedAt,
                    "lastSyncedAt" to System.currentTimeMillis()
                )
                firestore.collection("users").document(userId)
                    .collection("patients").document(patient.id.toString())
                    .set(data).await()
            }

            // Upload sessions
            for (session in sessions) {
                val data = hashMapOf(
                    "localId" to session.id,
                    "patientId" to session.patientId,
                    "date" to session.date,
                    "status" to session.status,
                    "charge" to session.charge,
                    "paymentStatus" to session.paymentStatus,
                    "isHomeVisit" to session.isHomeVisit,
                    "note" to session.note,
                    "painLevel" to session.painLevel,
                    "createdAt" to session.createdAt,
                    "lastSyncedAt" to System.currentTimeMillis()
                )
                firestore.collection("users").document(userId)
                    .collection("sessions").document(session.id.toString())
                    .set(data).await()
            }

            // Upload payments
            for (payment in payments) {
                val data = hashMapOf(
                    "localId" to payment.id,
                    "patientId" to payment.patientId,
                    "amount" to payment.amount,
                    "date" to payment.date,
                    "mode" to payment.mode,
                    "note" to payment.note,
                    "createdAt" to payment.createdAt,
                    "lastSyncedAt" to System.currentTimeMillis()
                )
                firestore.collection("users").document(userId)
                    .collection("payments").document(payment.id.toString())
                    .set(data).await()
            }

        } catch (e: Exception) {
            // Will retry on next sync
            android.util.Log.e("CloudSync", "Upload failed: ${e.message}", e)
        }
    }

    /**
     * Download all data from Firestore for the current user.
     * Call this after login to restore data on a new device.
     */
    suspend fun syncFromCloud(): Triple<List<PatientEntity>, List<SessionEntity>, List<PaymentEntity>> =
        withContext(Dispatchers.IO) {
            val userId = auth.currentUser?.uid ?: return@withContext Triple(emptyList(), emptyList(), emptyList())

            val patients = mutableListOf<PatientEntity>()
            val sessions = mutableListOf<SessionEntity>()
            val payments = mutableListOf<PaymentEntity>()

            try {
                // Download patients
                val patientDocs = firestore.collection("users").document(userId)
                    .collection("patients").get().await()
                for (doc in patientDocs.documents) {
                    patients.add(
                        PatientEntity(
                            id = doc.getLong("localId") ?: continue,
                            fullName = doc.getString("fullName") ?: "",
                            mobileNumber = doc.getString("mobileNumber") ?: "",
                            age = doc.getLong("age")?.toInt() ?: 0,
                            gender = doc.getString("gender") ?: "Other",
                            condition = doc.getString("condition") ?: "",
                            referredBy = doc.getString("referredBy"),
                            startDate = doc.getLong("startDate") ?: System.currentTimeMillis(),
                            status = doc.getString("status") ?: "Active",
                            perSessionCharge = doc.getLong("perSessionCharge")?.toInt() ?: 500,
                            plannedSessionsPerWeek = doc.getLong("plannedSessionsPerWeek")?.toInt(),
                            totalSessionsPlanned = doc.getLong("totalSessionsPlanned")?.toInt(),
                            notes = doc.getString("notes"),
                            isHomeVisitAvailable = doc.getBoolean("isHomeVisitAvailable") ?: false,
                            homeVisitCharge = doc.getLong("homeVisitCharge")?.toInt(),
                            nextAppointmentDate = doc.getLong("nextAppointmentDate"),
                            packageSessionsTotal = doc.getLong("packageSessionsTotal")?.toInt(),
                            packageSessionsUsed = doc.getLong("packageSessionsUsed")?.toInt() ?: 0,
                            packageAmount = doc.getLong("packageAmount")?.toInt(),
                            createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis(),
                            updatedAt = doc.getLong("updatedAt") ?: System.currentTimeMillis()
                        )
                    )
                }

                // Download sessions
                val sessionDocs = firestore.collection("users").document(userId)
                    .collection("sessions").get().await()
                for (doc in sessionDocs.documents) {
                    sessions.add(
                        SessionEntity(
                            id = doc.getLong("localId") ?: continue,
                            patientId = doc.getLong("patientId") ?: continue,
                            date = doc.getLong("date") ?: continue,
                            status = doc.getString("status") ?: "Present",
                            charge = doc.getLong("charge")?.toInt() ?: 0,
                            paymentStatus = doc.getString("paymentStatus") ?: "Due",
                            isHomeVisit = doc.getBoolean("isHomeVisit") ?: false,
                            note = doc.getString("note"),
                            painLevel = doc.getLong("painLevel")?.toInt(),
                            createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis()
                        )
                    )
                }

                // Download payments
                val paymentDocs = firestore.collection("users").document(userId)
                    .collection("payments").get().await()
                for (doc in paymentDocs.documents) {
                    payments.add(
                        PaymentEntity(
                            id = doc.getLong("localId") ?: continue,
                            patientId = doc.getLong("patientId") ?: continue,
                            amount = doc.getLong("amount")?.toInt() ?: 0,
                            date = doc.getLong("date") ?: continue,
                            mode = doc.getString("mode") ?: "Cash",
                            note = doc.getString("note"),
                            createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis()
                        )
                    )
                }

            } catch (e: Exception) {
                android.util.Log.e("CloudSync", "Download failed: ${e.message}", e)
            }

            Triple(patients, sessions, payments)
        }

    /**
     * Delete a patient from cloud
     */
    suspend fun deletePatientFromCloud(patientId: Long) = withContext(Dispatchers.IO) {
        val userId = auth.currentUser?.uid ?: return@withContext
        try {
            firestore.collection("users").document(userId)
                .collection("patients").document(patientId.toString()).delete().await()

            // Also delete associated sessions and payments
            val sessionDocs = firestore.collection("users").document(userId)
                .collection("sessions").whereEqualTo("patientId", patientId).get().await()
            for (doc in sessionDocs.documents) {
                doc.reference.delete().await()
            }

            val paymentDocs = firestore.collection("users").document(userId)
                .collection("payments").whereEqualTo("patientId", patientId).get().await()
            for (doc in paymentDocs.documents) {
                doc.reference.delete().await()
            }
        } catch (e: Exception) {
            // Silently fail
        }
    }

    /**
     * Clear all user data from cloud (on logout)
     */
    suspend fun clearCloudData() = withContext(Dispatchers.IO) {
        val userId = auth.currentUser?.uid ?: return@withContext
        try {
            firestore.collection("users").document(userId).delete().await()
        } catch (e: Exception) {
            // Silently fail
        }
    }
}
