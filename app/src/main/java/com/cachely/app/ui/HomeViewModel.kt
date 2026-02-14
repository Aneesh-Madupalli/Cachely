package com.cachely.app.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.cachely.app.data.CacheCleaner
import com.cachely.app.data.CleaningResult
import com.cachely.app.util.TimeFormatter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HomeUiState(
    val isCleaning: Boolean = false,
    val lastCleaned: String? = null,
    val assistedEnabled: Boolean = false,
    val result: CleaningResult? = null
)

class HomeViewModel(
    private val cacheCleaner: CacheCleaner
) : ViewModel() {

    private val _state = MutableStateFlow(HomeUiState())
    val state: StateFlow<HomeUiState> = _state.asStateFlow()

    fun setAssistedEnabled(enabled: Boolean) {
        _state.update { it.copy(assistedEnabled = enabled) }
    }

    fun startCleaning() {
        viewModelScope.launch {
            _state.update { it.copy(isCleaning = true, result = null) }
            val result = cacheCleaner.cleanCache()
            _state.update {
                it.copy(
                    isCleaning = false,
                    lastCleaned = TimeFormatter.formatRelative(System.currentTimeMillis()),
                    result = result
                )
            }
        }
    }
}

class HomeViewModelFactory(
    private val cacheCleaner: com.cachely.app.data.CacheCleaner
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            return HomeViewModel(cacheCleaner) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
