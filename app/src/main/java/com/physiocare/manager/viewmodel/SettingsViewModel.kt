package com.physiocare.manager.viewmodel

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

object SettingsKeys {
    val CLINIC_NAME = stringPreferencesKey("clinic_name")
    val REMINDER_HOUR = intPreferencesKey("reminder_hour")
    val REMINDER_MINUTE = intPreferencesKey("reminder_minute")
    val APP_LOCK_ENABLED = booleanPreferencesKey("app_lock_enabled")
    val APP_LOCK_PIN = stringPreferencesKey("app_lock_pin")
    val DARK_MODE = stringPreferencesKey("dark_mode") // "system", "light", "dark"
}

data class SettingsState(
    val clinicName: String = "PhysioCare Clinic",
    val reminderHour: Int = 20, // 8 PM default
    val reminderMinute: Int = 0,
    val appLockEnabled: Boolean = false,
    val appLockPin: String = "",
    val darkMode: String = "system",
    val isLoading: Boolean = true
)

class SettingsViewModel(
    private val dataStore: DataStore<Preferences>
) : ViewModel() {

    private val _state = MutableStateFlow(SettingsState())
    val state: StateFlow<SettingsState> = _state.asStateFlow()

    init {
        observeSettings()
    }

    private fun observeSettings() {
        viewModelScope.launch {
            dataStore.data.collect { prefs ->
                _state.update {
                    it.copy(
                        clinicName = prefs[SettingsKeys.CLINIC_NAME] ?: "PhysioCare Clinic",
                        reminderHour = prefs[SettingsKeys.REMINDER_HOUR] ?: 20,
                        reminderMinute = prefs[SettingsKeys.REMINDER_MINUTE] ?: 0,
                        appLockEnabled = prefs[SettingsKeys.APP_LOCK_ENABLED] ?: false,
                        appLockPin = prefs[SettingsKeys.APP_LOCK_PIN] ?: "",
                        darkMode = prefs[SettingsKeys.DARK_MODE] ?: "system",
                        isLoading = false
                    )
                }
            }
        }
    }

    fun updateClinicName(name: String) {
        viewModelScope.launch {
            dataStore.edit { it[SettingsKeys.CLINIC_NAME] = name }
        }
    }

    fun updateReminderTime(hour: Int, minute: Int) {
        viewModelScope.launch {
            dataStore.edit {
                it[SettingsKeys.REMINDER_HOUR] = hour
                it[SettingsKeys.REMINDER_MINUTE] = minute
            }
        }
    }

    fun toggleAppLock(enabled: Boolean) {
        viewModelScope.launch {
            dataStore.edit { it[SettingsKeys.APP_LOCK_ENABLED] = enabled }
        }
    }

    fun setAppLockPin(pin: String) {
        viewModelScope.launch {
            dataStore.edit {
                it[SettingsKeys.APP_LOCK_PIN] = pin
                it[SettingsKeys.APP_LOCK_ENABLED] = pin.isNotEmpty()
            }
        }
    }

    fun setDarkMode(mode: String) {
        viewModelScope.launch {
            dataStore.edit { it[SettingsKeys.DARK_MODE] = mode }
        }
    }

    class Factory(private val dataStore: DataStore<Preferences>) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return SettingsViewModel(dataStore) as T
        }
    }
}
