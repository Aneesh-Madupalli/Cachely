package com.cachely.app.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.cachely.app.accessibility.AccessibilityHelper
import com.cachely.app.data.AppCacheItem
import com.cachely.app.data.AppScanner
import com.cachely.app.data.CacheCleaner
import com.cachely.app.data.CleaningProgress
import com.cachely.app.data.CleaningResult
import com.cachely.app.util.TimeFormatter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HomeUiState(
    val appList: List<AppCacheItem> = emptyList(),
    val selectedPackageNames: Set<String> = emptySet(),
    val excludeSystemApps: Boolean = true,
    val excludeZeroCache: Boolean = false,
    val isScanning: Boolean = false,
    val isCleaning: Boolean = false,
    val progress: CleaningProgress? = null,
    val lastCleaned: String? = null,
    val result: CleaningResult? = null,
    val accessibilityGranted: Boolean = false,
    val installedAppsAvailable: Boolean = true
)

class HomeViewModel(
    private val cacheCleaner: CacheCleaner,
    private val appScanner: AppScanner
) : ViewModel() {

    private val _state = MutableStateFlow(HomeUiState())
    val state: StateFlow<HomeUiState> = _state.asStateFlow()

    private val _navigateToPermission = MutableStateFlow(false)
    val navigateToPermission: StateFlow<Boolean> = _navigateToPermission.asStateFlow()

    init {
        loadApps()
    }

    fun setAccessibilityGranted(granted: Boolean) {
        _state.update { it.copy(accessibilityGranted = granted) }
    }

    fun loadApps() {
        viewModelScope.launch {
            _state.update { it.copy(isScanning = true) }
            val list = appScanner.scan(
                excludeSystemApps = _state.value.excludeSystemApps,
                excludeZeroCache = _state.value.excludeZeroCache
            )
            _state.update {
                it.copy(
                    appList = list,
                    isScanning = false,
                    installedAppsAvailable = list.isNotEmpty()
                )
            }
        }
    }

    fun setFilters(excludeSystemApps: Boolean, excludeZeroCache: Boolean) {
        _state.update {
            it.copy(
                excludeSystemApps = excludeSystemApps,
                excludeZeroCache = excludeZeroCache
            )
        }
        loadApps()
    }

    fun toggleSelection(packageName: String) {
        _state.update { state ->
            val next = if (packageName in state.selectedPackageNames) {
                state.selectedPackageNames - packageName
            } else {
                state.selectedPackageNames + packageName
            }
            state.copy(selectedPackageNames = next)
        }
    }

    /** User tapped "Clean Selected". Permission gate: if not granted, navigate to permission; else start cleaning. */
    fun onCleanSelected(context: android.content.Context) {
        val state = _state.value
        if (state.selectedPackageNames.isEmpty()) return
        val granted = AccessibilityHelper(context).isAccessibilityServiceEnabled()
        _state.update { it.copy(accessibilityGranted = granted) }
        if (!granted) {
            _navigateToPermission.value = true
            return
        }
        startCleaning()
    }

    fun clearNavigateToPermission() {
        _navigateToPermission.value = false
    }

    fun startCleaning() {
        val state = _state.value
        val selected = state.selectedPackageNames.toList()
        if (selected.isEmpty()) return
        val nameResolver: (String) -> String = { pkg ->
            state.appList.find { it.packageName == pkg }?.appName ?: pkg
        }
        viewModelScope.launch {
            _state.update {
                it.copy(isCleaning = true, result = null, progress = null)
            }
            val result = cacheCleaner.cleanCache(
                selectedPackages = selected,
                appNameResolver = nameResolver,
                onProgress = { progress ->
                    withContext(Dispatchers.Main.immediate) {
                        _state.update { it.copy(progress = progress) }
                    }
                },
                isCancelled = { false }
            )
            _state.update {
                it.copy(
                    isCleaning = false,
                    progress = null,
                    lastCleaned = TimeFormatter.formatRelative(System.currentTimeMillis()),
                    result = result
                )
            }
        }
    }

    fun clearResult() {
        _state.update { it.copy(result = null, lastCleaned = null) }
    }
}

class HomeViewModelFactory(
    private val cacheCleaner: CacheCleaner,
    private val appScanner: AppScanner
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            return HomeViewModel(cacheCleaner, appScanner) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
