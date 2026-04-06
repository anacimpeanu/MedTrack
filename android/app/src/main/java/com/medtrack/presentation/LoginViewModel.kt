package com.medtrack.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.medtrack.domain.repository.MedTrackRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val isSubmitting: Boolean = false,
    val authenticatedUserId: Long? = null,
    val errorMessage: String? = null
)

class LoginViewModel(
    private val repository: MedTrackRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun onEmailChange(value: String) {
        _uiState.update { it.copy(email = value, errorMessage = null) }
    }

    fun onPasswordChange(value: String) {
        _uiState.update { it.copy(password = value, errorMessage = null) }
    }

    fun login() {
        val current = _uiState.value
        val email = current.email.trim()
        val password = current.password

        if (!email.contains("@") || email.length < 5) {
            _uiState.update { it.copy(errorMessage = "Enter a valid email.") }
            return
        }

        if (password.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Password is required.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, errorMessage = null) }

            try {
                val user = repository.getUserByEmail(email)
                if (user == null || user.passwordHash != password.sha256()) {
                    _uiState.update {
                        it.copy(
                            isSubmitting = false,
                            authenticatedUserId = null,
                            errorMessage = "Invalid email or password."
                        )
                    }
                    return@launch
                }

                _uiState.update {
                    it.copy(
                        password = "",
                        isSubmitting = false,
                        authenticatedUserId = user.userId,
                        errorMessage = null
                    )
                }
            } catch (_: Exception) {
                _uiState.update {
                    it.copy(
                        isSubmitting = false,
                        authenticatedUserId = null,
                        errorMessage = "Could not sign in. Try again."
                    )
                }
            }
        }
    }

    fun clearLoginSuccess() {
        _uiState.update { it.copy(authenticatedUserId = null) }
    }
}

