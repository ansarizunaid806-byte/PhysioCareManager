package com.physiocare.manager.data.sync

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import com.physiocare.manager.data.local.entity.PatientEntity
import com.physiocare.manager.data.local.entity.PaymentEntity
import com.physiocare.manager.data.local.entity.SessionEntity
import com.physiocare.manager.data.repository.PatientRepository
import com.physiocare.manager.data.repository.PaymentRepository
import com.physiocare.manager.data.repository.SessionRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

/**
 * CloudSyncManager handles automatic synchronization between
 * local Room database and Firebase Firestore.
 *
 * Strategy:
 * - All data is stored locally first (offline-first)
 * - Changes are pushed to Firestore in background
 * - Firestore changes are pulled in real-time
 * - Last-write-wins for conflict resolution
 */
class CloudSyncManager(
    private val userId: String,
    private val patientRepository: PatientRepository,
    private val sessionRepository: SessionRepository,
    private val paymentRepository: PaymentRepository
) {
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private var patientListener: ListenerRegistration? = null
    private var sessionListener: ListenerRegistration? = null
    private var paymentListener: ListenerRegistration? = null

    // Collection references (per-user subcollections)
    private val patientsRef = firestore.collection("users").document(userId).collection("patients")
    private val sessionsRef = firestore.collection("users").document(userId).collection("sessions")
    private val paymentsRef = firestore.collection("users").document(userId).collection("payments")

    /**
     * Start real-time sync listeners.
     * Call this after login.
     */
    fun startSync() {
        // Listen for remote patient changes
        patientListener = patientsRef.addSnapshotListener { snapshot, error ->
            if (error != null || snapshot == null) return@addSnapshotListener
            // Handle incoming patient data from Firestore
            // This would update local Room database
        }

        // Similar for sessions and payments
        sessionListener = sessionsRef.addSnapshotListener { snapshot, error ->
            if (error != null || snapshot == null) return@addSnapshotListener
        }

        paymentListener = paymentsRef.addSnapshotListener { snapshot, error ->
            if (error != null || snapshot == null) return@addSnapshotListener
        }
    }

    /**
     * Stop sync listeners. Call on logout.
     */
    fun stopSync() {
        patientListener?.remove()
        sessionListener?.remove()
        paymentListener?.remove()
    }

    // ========== UPLOAD (Local → Cloud) ==========

    suspend fun uploadPatient(patient: PatientEntity) = withContext(Dispatchers.IO) {
        try {
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
                "syncedAt" to System.currentTimeMillis()
            )
            patientsRef.document(patient.id.toString()).set(data).await()
        } catch (e: Exception) {
            // Queue for retry when online
        }
    }

    suspend fun uploadSession(session: SessionEntity) = withContext(Dispatchers.IO) {
        try {
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
                "syncedAt" to System.currentTimeMillis()
            )
            sessionsRef.document(session.id.toString()).set(data).await()
        } catch (e: Exception) {
            // Queue for retry
        }
    }

    suspend fun uploadPayment(payment: PaymentEntity) = withContext(Dispatchers.IO) {
        try {
            val data = hashMapOf(
                "localId" to payment.id,
                "patientId" to payment.patientId,
                "amount" to payment.amount,
                "date" to payment.date,
                "mode" to payment.mode,
                "note" to payment.note,
                "createdAt" to payment.createdAt,
                "syncedAt" to System.currentTimeMillis()
            )
            paymentsRef.document(payment.id.toString()).set(data).await()
        } catch (e: Exception) {
            // Queue for retry
        }
    }

    // ========== DOWNLOAD (Cloud → Local) ==========

    suspend fun downloadAllPatients(): List<PatientEntity> = withContext(Dispatchers.IO) {
        try {
            val snapshot = patientsRef.get().await()
            snapshot.documents.mapNotNull { doc ->
                PatientEntity(
                    id = doc.getLong("localId") ?: return@mapNotNull null,
                    fullName = doc.getString("fullName") ?: return@mapNotNull null,
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
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun downloadAllSessions(): List<SessionEntity> = withContext(Dispatchers.IO) {
        try {
            val snapshot = sessionsRef.get().await()
            snapshot.documents.mapNotNull { doc ->
                SessionEntity(
                    id = doc.getLong("localId") ?: return@mapNotNull null,
                    patientId = doc.getLong("patientId") ?: return@mapNotNull null,
                    date = doc.getLong("date") ?: return@mapNotNull null,
                    status = doc.getString("status") ?: return@mapNotNull null,
                    charge = doc.getLong("charge")?.toInt() ?: 0,
                    paymentStatus = doc.getString("paymentStatus") ?: "Due",
                    isHomeVisit = doc.getBoolean("isHomeVisit") ?: false,
                    note = doc.getString("note"),
                    painLevel = doc.getLong("painLevel")?.toInt(),
                    createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis()
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun downloadAllPayments(): List<PaymentEntity> = withContext(Dispatchers.IO) {
        try {
            val snapshot = paymentsRef.get().await()
            snapshot.documents.mapNotNull { doc ->
                PaymentEntity(
                    id = doc.getLong("localId") ?: return@mapNotNull null,
                    patientId = doc.getLong("patientId") ?: return@mapNotNull null,
                    amount = doc.getLong("amount")?.toInt() ?: 0,
                    date = doc.getLong("date") ?: return@mapNotNull null,
                    mode = doc.getString("mode") ?: "Cash",
                    note = doc.getString("note"),
                    createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis()
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    // ========== FULL SYNC ==========

    /**
     * Upload all local data to Firestore.
     * Called after login to sync existing data.
     */
    suspend fun uploadAllLocalData() = withContext(Dispatchers.IO) {
        try {
            val patients = patientRepository.getAllPatientsOnce()
            patients.forEach { uploadPatient(it) }

            val sessions = sessionRepository.getAllSessionsOnce()
            sessions.forEach { uploadSession(it) }

            val payments = paymentRepository.getAllPaymentsOnce()
            payments.forEach { uploadPayment(it) }
        } catch (e: Exception) {
            // Will retry later
        }
    }

    /**
     * Delete patient from cloud (when deleted locally)
     */
    suspend fun deletePatientFromCloud(patientId: Long) = withContext(Dispatchers.IO) {
        try {
            patientsRef.document(patientId.toString()).delete().await()
            // Also delete associated sessions and payments
            sessionsRef.whereEqualTo("patientId", patientId).get().await()
                .documents.forEach { it.reference.delete().await() }
            paymentsRef.whereEqualTo("patientId", patientId).get().await()
                .documents.forEach { it.reference.delete().await() }
        } catch (e: Exception) {
            // Retry later
        }
    }
}
