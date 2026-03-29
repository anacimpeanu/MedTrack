package com.medtrack.presentation

import android.database.sqlite.SQLiteConstraintException
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.medtrack.data.local.entity.UserEntity
import com.medtrack.domain.repository.MedTrackRepository
import java.security.MessageDigest
import java.time.Instant
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class RegisterUiState(
    val fullName: String = "",
    val email: String = "",
    val phone: String = "",
    val password: String = "",
    val isSubmitting: Boolean = false,
    val successMessage: String? = null,
    val errorMessage: String? = null
)

class RegisterViewModel(
    private val repository: MedTrackRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    fun onFullNameChange(value: String) {
        _uiState.update { it.copy(fullName = value, successMessage = null, errorMessage = null) }
    }

    fun onEmailChange(value: String) {
        _uiState.update { it.copy(email = value, successMessage = null, errorMessage = null) }
    }

    fun onPhoneChange(value: String) {
        _uiState.update { it.copy(phone = value, successMessage = null, errorMessage = null) }
    }

    fun onPasswordChange(value: String) {
        _uiState.update { it.copy(password = value, successMessage = null, errorMessage = null) }
    }

    fun register() {
        val current = _uiState.value
        val fullName = current.fullName.trim()
        val email = current.email.trim()
        val phone = current.phone.trim()
        val password = current.password

        if (fullName.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Name is required.", successMessage = null) }
            return
        }

        if (!email.contains("@") || email.length < 5) {
            _uiState.update { it.copy(errorMessage = "Enter a valid email.", successMessage = null) }
            return
        }

        if (password.length < 6) {
            _uiState.update { it.copy(errorMessage = "Password must be at least 6 characters.", successMessage = null) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, errorMessage = null, successMessage = null) }
            try {
                val now = Instant.now().toString()
                repository.addUser(
                    UserEntity(
                        fullName = fullName,
                        email = email,
                        passwordHash = password.sha256(),
                        phone = phone.ifBlank { null },
                        createdAt = now,
                        updatedAt = now
                    )
                )

                _uiState.update {
                    it.copy(
                        fullName = "",
                        email = "",
                        phone = "",
                        password = "",
                        isSubmitting = false,
                        successMessage = "Account created successfully.",
                        errorMessage = null
                    )
                }
            } catch (_: SQLiteConstraintException) {
                _uiState.update {
                    it.copy(
                        isSubmitting = false,
                        successMessage = null,
                        errorMessage = "Email already exists."
                    )
                }
            } catch (_: Exception) {
                _uiState.update {
                    it.copy(
                        isSubmitting = false,
                        successMessage = null,
                        errorMessage = "Could not create account. Try again."
                    )
                }
            }
        }
    }
}

private fun String.sha256(): String {
    val digest = MessageDigest.getInstance("SHA-256")
    val hash = digest.digest(toByteArray())
    return hash.joinToString("") { byte -> "%02x".format(byte) }
}

