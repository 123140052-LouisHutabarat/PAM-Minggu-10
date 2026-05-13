package org.example.project.ViewModel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import org.example.project.data.AppSettings

data class SettingsUiState(
    val isDarkMode: Boolean = false,
    val sortOrder: String = "newest",
    val showFavoritesFirst: Boolean = false
)

class SettingsViewModel(private val appSettings: AppSettings) : ViewModel() {

    private val _uiState = MutableStateFlow(
        SettingsUiState(
            isDarkMode = appSettings.isDarkMode,
            sortOrder = appSettings.sortOrder,
            showFavoritesFirst = appSettings.showFavoritesFirst
        )
    )
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    fun toggleDarkMode(enabled: Boolean) {
        appSettings.isDarkMode = enabled
        _uiState.update { it.copy(isDarkMode = enabled) }
    }

    fun setSortOrder(order: String) {
        appSettings.sortOrder = order
        _uiState.update { it.copy(sortOrder = order) }
    }

    fun toggleShowFavoritesFirst(enabled: Boolean) {
        appSettings.showFavoritesFirst = enabled
        _uiState.update { it.copy(showFavoritesFirst = enabled) }
    }
}