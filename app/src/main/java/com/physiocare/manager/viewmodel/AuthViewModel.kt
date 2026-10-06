package com.physiocare.manager.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.FirebaseException
import com.google.firebase.auth.*
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.concurrent.TimeUnit

data class AuthState(
    val isLoggedIn: Boolean = false,
    val isLoading: Boolean = false,
    val userId: String? = null,
    val displayName: String? = null,
    val email: String? = null,
    val phone: String? = null,
    val error: String? = null,
    val verificationId: String? = null,
    val isPhoneMode: Boolean = true // true = phone, false = email
)

sealed class AuthEvent {
    data class CodeSent(val verificationId: String) : AuthEvent()
    data class Error(val message: String) : AuthEvent()
    object Success : AuthEvent()
}

class AuthViewModel : ViewModel() {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    private val _state = MutableStateFlow(AuthState())
    val state: StateFlow<AuthState> = _state.asStateFlow()

    private val _events = Channel<AuthEvent>()
    val events: Flow<AuthEvent> = _events.receiveAsFlow()

    private var storedVerificationId: String? = null
    private var resendToken: PhoneAuthProvider.ForceResendingToken? = null

    init {
        // Check if user is already logged in
        val currentUser = auth.currentUser
        if (currentUser != null) {
            _state.value = AuthState(
                isLoggedIn = true,
                userId = currentUser.uid,
                displayName = currentUser.displayName,
                email = currentUser.email,
                phone = currentUser.phoneNumber
            )
        }
    }

    fun setAuthMode(isPhoneMode: Boolean) {
        _state.update { it.copy(isPhoneMode = isPhoneMode, error = null) }
    }

    // ========== PHONE AUTH ==========

    fun sendOtp(phoneNumber: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                    // Auto-verification (instant verification)
                    signInWithPhoneCredential(credential)
                }

                override fun onVerificationFailed(e: FirebaseException) {
                    _state.update { it.copy(isLoading = false, error = "Verification failed: ${e.message}") }
                    viewModelScope.launch { _events.send(AuthEvent.Error(e.message ?: "Unknown error")) }
                }

                override fun onCodeSent(
                    verificationId: String,
                    token: PhoneAuthProvider.ForceResendingToken
                ) {
                    storedVerificationId = verificationId
                    resendToken = token
                    _state.update { it.copy(isLoading = false, verificationId = verificationId) }
                    viewModelScope.launch { _events.send(AuthEvent.CodeSent(verificationId)) }
                }
            }

            try {
                PhoneAuthProvider.getAuthInstance(auth)
                    .verifyPhoneNumber(
                        phoneNumber,
                        60, // Timeout duration
                        TimeUnit.SECONDS,
                        com.google.firebase.Firebase.app, // Use the activity
                        callbacks,
                        forceResendingToken = resendToken
                    )
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun verifyOtp(code: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            try {
                val credential = PhoneAuthProvider.getCredential(
                    storedVerificationId ?: return@launch,
                    code
                )
                signInWithPhoneCredential(credential)
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, error = "Invalid OTP") }
            }
        }
    }

    private fun signInWithPhoneCredential(credential: PhoneAuthCredential) {
        viewModelScope.launch {
            try {
                val result = auth.signInWithCredential(credential).await()
                val user = result.user
                if (user != null) {
                    // Create or fetch user profile in Firestore
                    createUserProfileIfNotExists(user.uid, phone = user.phoneNumber)
                    _state.update {
                        it.copy(
                            isLoggedIn = true,
                            isLoading = false,
                            userId = user.uid,
                            phone = user.phoneNumber,
                            verificationId = null
                        )
                    }
                    _events.send(AuthEvent.Success)
                }
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, error = "Sign in failed: ${e.message}") }
            }
        }
    }

    // ========== EMAIL AUTH ==========

    fun signUpWithEmail(email: String, password: String, name: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            try {
                val result = auth.createUserWithEmailAndPassword(email, password).await()
                val user = result.user
                if (user != null) {
                    // Update display name
                    user.updateProfile(
                        userProfileChangeRequest { setDisplayName(name) }
                    ).await()

                    // Create user profile in Firestore
                    createUserProfileIfNotExists(user.uid, email = email, name = name)

                    _state.update {
                        it.copy(
                            isLoggedIn = true,
                            isLoading = false,
                            userId = user.uid,
                            displayName = name,
                            email = email
                        )
                    }
                    _events.send(AuthEvent.Success)
                }
            } catch (e: FirebaseAuthUserCollisionException) {
                _state.update { it.copy(isLoading = false, error = "Email already registered. Try logging in.") }
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, error = e.message ?: "Sign up failed") }
            }
        }
    }

    fun loginWithEmail(email: String, password: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            try {
                val result = auth.signInWithEmailAndPassword(email, password).await()
                val user = result.user
                if (user != null) {
                    _state.update {
                        it.copy(
                            isLoggedIn = true,
                            isLoading = false,
                            userId = user.uid,
                            displayName = user.displayName,
                            email = user.email
                        )
                    }
                    _events.send(AuthEvent.Success)
                }
            } catch (e: FirebaseAuthInvalidUserException) {
                _state.update { it.copy(isLoading = false, error = "No account found with this email") }
            } catch (e: FirebaseAuthInvalidCredentialsException) {
                _state.update { it.copy(isLoading = false, error = "Invalid password") }
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, error = e.message ?: "Login failed") }
            }
        }
    }

    // ========== COMMON ==========

    private suspend fun createUserProfileIfNotExists(
        uid: String,
        email: String? = null,
        phone: String? = null,
        name: String? = null
    ) {
        try {
            val userDoc = firestore.collection("users").document(uid).get().await()
            if (!userDoc.exists()) {
                val userProfile = hashMapOf(
                    "uid" to uid,
                    "email" to email,
                    "phone" to phone,
                    "displayName" to name,
                    "createdAt" to System.currentTimeMillis(),
                    "lastLoginAt" to System.currentTimeMillis()
                )
                firestore.collection("users").document(uid).set(userProfile).await()
            } else {
                // Update last login
                firestore.collection("users").document(uid)
                    .update("lastLoginAt", System.currentTimeMillis()).await()
            }
        } catch (e: Exception) {
            // Non-critical, continue
        }
    }

    fun logout() {
        auth.signOut()
        _state.value = AuthState()
    }

    fun clearError() {
        _state.update { it.copy(error = null) }
    }
}

// Helper extension for UserProfileChangeRequest
private inline fun userProfileChangeRequest(block: UserProfileChangeRequest.Builder.() -> Unit): UserProfileChangeRequest {
    val builder = UserProfileChangeRequest.Builder()
    builder.block()
    return builder.build()
}
