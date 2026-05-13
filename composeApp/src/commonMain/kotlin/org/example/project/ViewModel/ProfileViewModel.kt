package org.example.project.ViewModel

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import org.example.project.Model.ProfileUiState

class ProfileViewModel {
    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    fun toggleDarkMode(isDark: Boolean) {
        _uiState.update { it.copy(isDarkMode = isDark) }
    }

    fun setEditingMode(isEditing: Boolean) {
        _uiState.update { it.copy(isEditing = isEditing) }
    }


    fun saveProfile(newName: String, newBio: String, newEmail: String, newPhone: String, newLocation: String) {
        _uiState.update {
            it.copy(
                name = newName,
                bio = newBio,
                email = newEmail,
                phone = newPhone,
                location = newLocation,
                isEditing = false
            )
        }
    }
}