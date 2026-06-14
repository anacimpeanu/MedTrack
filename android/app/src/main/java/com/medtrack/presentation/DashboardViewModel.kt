package com.medtrack.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.medtrack.data.local.entity.MedicationLogEntity
import com.medtrack.data.local.entity.PatientEntity
import com.medtrack.data.local.entity.PatientMedicationEntity
import com.medtrack.data.local.entity.UserEntity
import com.medtrack.domain.repository.MedTrackRepository
import java.time.Instant
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.ExperimentalCoroutinesApi

data class DashboardUiState(
    val isLoading: Boolean = true,
    val isSavingPatient: Boolean = false,
    val user: UserEntity? = null,
    val patients: List<PatientEntity> = emptyList(),
    val selectedPatientId: Long? = null,
    val activePlans: List<PatientMedicationEntity> = emptyList(),
    val logs: List<MedicationLogEntity> = emptyList(),
    val addPatientFullName: String = "",
    val addPatientBirthDate: String = "",
    val addPatientGender: String = "",
    val addPatientBloodType: String = "",
    val addPatientAllergies: String = "",
    val addPatientChronicConditions: String = "",
    val errorMessage: String? = null,
    val successMessage: String? = null
)

@OptIn(ExperimentalCoroutinesApi::class)
class DashboardViewModel(
    private val repository: MedTrackRepository,
    private val userId: Long
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    private val selectedPatientIdFlow = MutableStateFlow<Long?>(null)

    init {
        loadUser()
        observePatients()
        observePlans()
        observeLogs()
    }

    private fun loadUser() {
        viewModelScope.launch {
            try {
                val user = repository.getUserById(userId)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        user = user,
                        errorMessage = if (user == null) "Profile not found." else null
                    )
                }
                if (user != null) {
                    val selectedPatientId = when (user.profileRole.lowercase()) {
                        "patient" -> user.userId
                        else -> null
                    }
                    selectedPatientIdFlow.value = selectedPatientId
                    _uiState.update { it.copy(selectedPatientId = selectedPatientId) }
                }
            } catch (_: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Dashboard could not be loaded."
                    )
                }
            }
        }
    }

    private fun observePatients() {
        viewModelScope.launch {
            repository.observePatientsByUser(userId).collect { patients ->
                _uiState.update { current ->
                    current.copy(patients = patients)
                }

                val currentUser = _uiState.value.user
                if (currentUser?.profileRole?.lowercase() == "caretaker") {
                    val selectedId = selectedPatientIdFlow.value
                    val patientStillExists = selectedId != null && patients.any { it.patientId == selectedId }
                    if (!patientStillExists) {
                        val firstPatientId = patients.firstOrNull()?.patientId
                        selectedPatientIdFlow.value = firstPatientId
                        _uiState.update { it.copy(selectedPatientId = firstPatientId) }
                    }
                }
            }
        }
    }

    private fun observePlans() {
        viewModelScope.launch {
            selectedPatientIdFlow
                .filterNotNull()
                .distinctUntilChanged()
                .flatMapLatest { patientId -> repository.observeActivePlans(patientId) }
                .collect { plans ->
                    _uiState.update { it.copy(activePlans = plans) }
                }
        }
    }

    private fun observeLogs() {
        viewModelScope.launch {
            selectedPatientIdFlow
                .filterNotNull()
                .distinctUntilChanged()
                .flatMapLatest { patientId -> repository.observeLogsByPatient(patientId) }
                .collect { logs ->
                    _uiState.update { it.copy(logs = logs) }
                }
        }
    }

    fun selectPatient(patientId: Long) {
        selectedPatientIdFlow.value = patientId
        _uiState.update { it.copy(selectedPatientId = patientId, successMessage = null, errorMessage = null) }
    }

    fun onAddPatientFullNameChange(value: String) {
        _uiState.update { it.copy(addPatientFullName = value, errorMessage = null, successMessage = null) }
    }

    fun onAddPatientBirthDateChange(value: String) {
        _uiState.update { it.copy(addPatientBirthDate = value, errorMessage = null, successMessage = null) }
    }

    fun onAddPatientGenderChange(value: String) {
        _uiState.update { it.copy(addPatientGender = value, errorMessage = null, successMessage = null) }
    }

    fun onAddPatientBloodTypeChange(value: String) {
        _uiState.update { it.copy(addPatientBloodType = value, errorMessage = null, successMessage = null) }
    }

    fun onAddPatientAllergiesChange(value: String) {
        _uiState.update { it.copy(addPatientAllergies = value, errorMessage = null, successMessage = null) }
    }

    fun onAddPatientChronicConditionsChange(value: String) {
        _uiState.update { it.copy(addPatientChronicConditions = value, errorMessage = null, successMessage = null) }
    }

    fun addPatient() {
        val current = _uiState.value
        val user = current.user

        if (user == null || user.profileRole.lowercase() != "caretaker") {
            _uiState.update { it.copy(errorMessage = "Only caretakers can add patients.") }
            return
        }

        val fullName = current.addPatientFullName.trim()
        val birthDate = current.addPatientBirthDate.trim()
        val gender = current.addPatientGender.trim()
        val bloodType = current.addPatientBloodType.trim()
        val allergies = current.addPatientAllergies.trim()
        val chronicConditions = current.addPatientChronicConditions.trim()

        when {
            fullName.isBlank() -> {
                _uiState.update { it.copy(errorMessage = "Patient name is required.") }
                return
            }
            birthDate.isBlank() -> {
                _uiState.update { it.copy(errorMessage = "Birth date is required.") }
                return
            }
            gender.isBlank() -> {
                _uiState.update { it.copy(errorMessage = "Gender is required.") }
                return
            }
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSavingPatient = true, errorMessage = null, successMessage = null) }
            try {
                val patientId = repository.addPatient(
                    PatientEntity(
                        userId = user.userId,
                        fullName = fullName,
                        birthDate = birthDate,
                        gender = gender,
                        bloodType = bloodType.ifBlank { null },
                        allergies = allergies.ifBlank { null },
                        chronicConditions = chronicConditions.ifBlank { null },
                        createdAt = Instant.now().toString()
                    )
                )

                _uiState.update {
                    it.copy(
                        isSavingPatient = false,
                        addPatientFullName = "",
                        addPatientBirthDate = "",
                        addPatientGender = "",
                        addPatientBloodType = "",
                        addPatientAllergies = "",
                        addPatientChronicConditions = "",
                        successMessage = "Patient added successfully."
                    )
                }

                selectedPatientIdFlow.value = patientId
                _uiState.update { it.copy(selectedPatientId = patientId) }
            } catch (_: Exception) {
                _uiState.update {
                    it.copy(
                        isSavingPatient = false,
                        errorMessage = "Patient could not be added."
                    )
                }
            }
        }
    }

    fun clearMessages() {
        _uiState.update { it.copy(errorMessage = null, successMessage = null) }
    }

    fun addTreatment(medName: String, dose: String, freq: String) {
        val current = _uiState.value
        val patientId = current.selectedPatientId ?: return

        viewModelScope.launch {
            try {
                // create medication
                val medicationId = repository.addMedication(
                    com.medtrack.data.local.entity.MedicationEntity(
                        name = medName,
                        description = "",
                        type = "",
                        manufacturer = "",
                        defaultDoseUnit = "mg"
                    )
                )

                val planId = repository.addPatientMedication(
                    com.medtrack.data.local.entity.PatientMedicationEntity(
                        patientId = patientId,
                        medicationId = medicationId,
                        dosageAmount = dose.toDoubleOrNull() ?: 0.0,
                        dosageUnit = "mg",
                        frequency = freq,
                        startDate = java.time.LocalDate.now().toString(),
                        instructions = null,
                        isActive = true
                    )
                )

                _uiState.update { it.copy(successMessage = "Treatment added.") }
            } catch (_: Exception) {
                _uiState.update { it.copy(errorMessage = "Could not add treatment.") }
            }
        }
    }

    fun updateTreatment(item: PatientMedicationEntity) {
        viewModelScope.launch {
            try {
                repository.updatePatientMedication(item)
                _uiState.update { it.copy(successMessage = "Treatment updated.") }
            } catch (_: Exception) {
                _uiState.update { it.copy(errorMessage = "Could not update treatment.") }
            }
        }
    }

    fun deleteTreatment(itemId: Long) {
        viewModelScope.launch {
            try {
                repository.deletePatientMedication(itemId)
                _uiState.update { it.copy(successMessage = "Treatment deleted.") }
            } catch (_: Exception) {
                _uiState.update { it.copy(errorMessage = "Could not delete treatment.") }
            }
        }
    }

    fun addJournalLog(note: String) {
        val current = _uiState.value
        val patientId = current.selectedPatientId ?: return

        viewModelScope.launch {
            try {
                repository.addLog(
                    com.medtrack.data.local.entity.MedicationLogEntity(
                        scheduleId = 0,
                        patientId = patientId,
                        takenAt = java.time.Instant.now().toString(),
                        status = note.ifBlank { "note" },
                        notes = note.ifBlank { null }
                    )
                )

                _uiState.update { it.copy(successMessage = "Journal entry added.") }
            } catch (_: Exception) {
                _uiState.update { it.copy(errorMessage = "Could not add journal entry.") }
            }
        }
    }

    fun updateJournalLog(item: MedicationLogEntity) {
        viewModelScope.launch {
            try {
                repository.updateLog(item)
                _uiState.update { it.copy(successMessage = "Journal updated.") }
            } catch (_: Exception) {
                _uiState.update { it.copy(errorMessage = "Could not update journal.") }
            }
        }
    }

    fun deleteJournalLog(id: Long) {
        viewModelScope.launch {
            try {
                repository.deleteLog(id)
                _uiState.update { it.copy(successMessage = "Journal entry deleted.") }
            } catch (_: Exception) {
                _uiState.update { it.copy(errorMessage = "Could not delete journal entry.") }
            }
        }
    }
}






