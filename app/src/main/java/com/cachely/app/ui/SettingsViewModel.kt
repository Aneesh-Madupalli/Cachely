package com.cachely.app.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.cachely.app.data.PreferencesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val preferences: PreferencesRepository
) : ViewModel() {

    private val _assistedPreferred = MutableStateFlow(false)
    val assistedPreferred: StateFlow<Boolean> = _assistedPreferred.asStateFlow()

    init {
        viewModelScope.launch {
            preferences.assistedPreferred.collect { _assistedPreferred.value = it }
        }
    }

    fun setAssistedPreferred(enabled: Boolean) {
        viewModelScope.launch {
            preferences.setAssistedPreferred(enabled)
        }
    }
}

class SettingsViewModelFactory(
    private val preferences: PreferencesRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SettingsViewModel::class.java)) {
            return SettingsViewModel(preferences) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
