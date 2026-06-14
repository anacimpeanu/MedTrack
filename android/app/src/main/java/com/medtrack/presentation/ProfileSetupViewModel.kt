package com.medtrack.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.medtrack.data.local.entity.PatientEntity
import com.medtrack.data.local.entity.UserEntity
import com.medtrack.domain.repository.MedTrackRepository
import java.time.Instant
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class ProfileSetupUiState(
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val fullName: String = "",
    val email: String = "",
    val profileRole: String = "patient",
    val profilePhotoUri: String = "",
    val age: String = "",
    val birthDate: String = "",
    val cnp: String = "",
    val heightCm: String = "",
    val weightKg: String = "",
    val sex: String = "",
    val errorMessage: String? = null,
    val successUserId: Long? = null
)

class ProfileSetupViewModel(
    private val repository: MedTrackRepository,
    private val userId: Long
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileSetupUiState())
    val uiState: StateFlow<ProfileSetupUiState> = _uiState.asStateFlow()

    private var loadedUser: UserEntity? = null

    init {
        loadUser()
    }

    private fun loadUser() {
        viewModelScope.launch {
            try {
                val user = repository.getUserById(userId)
                if (user == null) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "Profile data could not be loaded."
                        )
                    }
                    return@launch
                }

                loadedUser = user
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        fullName = user.fullName,
                        email = user.email,
                        profileRole = user.profileRole.ifBlank { "patient" },
                        profilePhotoUri = user.profilePhotoUri.orEmpty(),
                        age = user.age?.toString().orEmpty(),
                        birthDate = user.birthDate.orEmpty(),
                        cnp = user.cnp.orEmpty(),
                        heightCm = user.heightCm?.toString().orEmpty(),
                        weightKg = user.weightKg?.toString().orEmpty(),
                        sex = user.sex.orEmpty()
                    )
                }
            } catch (_: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Profile data could not be loaded."
                    )
                }
            }
        }
    }

    fun onProfileRoleChange(value: String) {
        _uiState.update { it.copy(profileRole = value, errorMessage = null) }
    }

    fun onProfilePhotoUriChange(value: String) {
        _uiState.update { it.copy(profilePhotoUri = value, errorMessage = null) }
    }

    fun onAgeChange(value: String) {
        _uiState.update { it.copy(age = value, errorMessage = null) }
    }

    fun onBirthDateChange(value: String) {
        _uiState.update { it.copy(birthDate = value, errorMessage = null) }
    }

    fun onCnpChange(value: String) {
        _uiState.update { it.copy(cnp = value, errorMessage = null) }
    }

    fun onHeightChange(value: String) {
        _uiState.update { it.copy(heightCm = value, errorMessage = null) }
    }

    fun onWeightChange(value: String) {
        _uiState.update { it.copy(weightKg = value, errorMessage = null) }
    }

    fun onSexChange(value: String) {
        _uiState.update { it.copy(sex = value, errorMessage = null) }
    }

    fun saveProfile() {
        val current = _uiState.value
        val baseUser = loadedUser

        if (baseUser == null) {
            _uiState.update { it.copy(errorMessage = "Profile data could not be loaded.") }
            return
        }

        val ageValue = current.age.trim().toIntOrNull()
        val heightValue = current.heightCm.trim().toIntOrNull()
        val weightValue = current.weightKg.trim().toDoubleOrNull()
        val cnpValue = current.cnp.trim()
        val birthDateValue = current.birthDate.trim()
        val sexValue = current.sex.trim()
        val roleValue = current.profileRole.trim().lowercase()
        val photoUriValue = current.profilePhotoUri.trim()

        when {
            roleValue != "patient" && roleValue != "caretaker" -> {
                _uiState.update { it.copy(errorMessage = "Choose patient or caretaker.") }
                return
            }
            ageValue == null || ageValue <= 0 -> {
                _uiState.update { it.copy(errorMessage = "Enter a valid age.") }
                return
            }
            birthDateValue.isBlank() -> {
                _uiState.update { it.copy(errorMessage = "Birth date is required.") }
                return
            }
            cnpValue.isBlank() -> {
                _uiState.update { it.copy(errorMessage = "CNP is required.") }
                return
            }
            cnpValue.length != 13 -> {
                _uiState.update { it.copy(errorMessage = "CNP must have 13 digits.") }
                return
            }
            heightValue == null || heightValue <= 0 -> {
                _uiState.update { it.copy(errorMessage = "Enter a valid height in cm.") }
                return
            }
            weightValue == null || weightValue <= 0 -> {
                _uiState.update { it.copy(errorMessage = "Enter a valid weight.") }
                return
            }
            sexValue.isBlank() -> {
                _uiState.update { it.copy(errorMessage = "Select sex.") }
                return
            }
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null, successUserId = null) }
            try {
                val now = Instant.now().toString()
                if (roleValue == "patient") {
                    val existingPatient = repository.getPatientById(baseUser.userId)
                    val patientEntity = PatientEntity(
                        patientId = baseUser.userId,
                        userId = baseUser.userId,
                        fullName = baseUser.fullName,
                        birthDate = birthDateValue,
                        gender = sexValue,
                        bloodType = existingPatient?.bloodType,
                        allergies = existingPatient?.allergies,
                        chronicConditions = existingPatient?.chronicConditions,
                        createdAt = existingPatient?.createdAt ?: now
                    )

                    if (existingPatient == null) {
                        repository.addPatient(patientEntity)
                    } else {
                        repository.updatePatient(patientEntity)
                    }
                }

                repository.updateUser(
                    baseUser.copy(
                        profileRole = roleValue,
                        profilePhotoUri = photoUriValue.ifBlank { null },
                        age = ageValue,
                        birthDate = birthDateValue,
                        cnp = cnpValue.ifBlank { null },
                        heightCm = heightValue,
                        weightKg = weightValue,
                        sex = sexValue,
                        profileCompleted = true,
                        updatedAt = now
                    )
                )

                _uiState.update {
                    it.copy(
                        isSaving = false,
                        successUserId = baseUser.userId,
                        errorMessage = null
                    )
                }
            } catch (_: Exception) {
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        errorMessage = "Profile could not be saved. Please try again."
                    )
                }
            }
        }
    }

    // Called when the UI has prepared a final photo path (e.g. copied to internal storage).
    fun saveProfileWithPhotoPath(photoPath: String?) {
        if (!photoPath.isNullOrBlank()) {
            _uiState.update { it.copy(profilePhotoUri = photoPath) }
        }
        saveProfile()
    }

    fun clearSaveResult() {
        _uiState.update { it.copy(successUserId = null) }
    }
}




