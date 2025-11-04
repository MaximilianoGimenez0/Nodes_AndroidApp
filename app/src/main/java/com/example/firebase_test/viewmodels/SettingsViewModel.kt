package com.example.firebase_test.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.firebase_test.data.AppSettings
import com.example.firebase_test.data.SettingsManager
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val settingsManager: SettingsManager
) : ViewModel() {

    // 1. Expone el Flow de ajustes
    val appSettings: StateFlow<AppSettings> = settingsManager.appSettingsFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = AppSettings("Español", "Automático (Sistema)", true, 1) // Default 1 (Mediana)
        )

    // 2. Funciones para guardar (¡Actualizadas!)

    fun setLanguage(language: String) {
        viewModelScope.launch { settingsManager.setLanguage(language) }
    }

    fun setTheme(theme: String) {
        viewModelScope.launch { settingsManager.setTheme(theme) }
    }

    fun setAllowNotifications(isEnabled: Boolean) {
        viewModelScope.launch { settingsManager.setAllowNotifications(isEnabled) }
    }

    fun setFontSize(size: Int) { // <-- ¡AÑADIDO!
        viewModelScope.launch { settingsManager.setFontSize(size) }
    }
}

/**
 * Factory para crear el SettingsViewModel.
 * Le "inyectamos" el SettingsManager que, a su vez, necesita el Context.
 */
class SettingsViewModelFactory(
    private val settingsManager: SettingsManager
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SettingsViewModel::class.java)) {
            return SettingsViewModel(settingsManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}