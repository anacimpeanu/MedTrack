package com.medtrack.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.medtrack.data.local.entity.PatientEntity
import com.medtrack.domain.repository.MedTrackRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant

data class PatientsUiState(
    val patients: List<PatientEntity> = emptyList()
)

class PatientsViewModel(
    private val repository: MedTrackRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PatientsUiState())
    val uiState: StateFlow<PatientsUiState> = _uiState.asStateFlow()

    init {
        observePatients()
    }

    private fun observePatients() {
        viewModelScope.launch {
            repository.observePatientsByUser(DEMO_USER_ID).collect { patients ->
                _uiState.update { it.copy(patients = patients) }
            }
        }
    }

    fun addDemoPatient() {
        viewModelScope.launch {
            repository.addPatient(
                PatientEntity(
                    userId = DEMO_USER_ID,
                    fullName = "Patient ${System.currentTimeMillis() % 1000}",
                    gender = "unspecified",
                    bloodType = "O+",
                    createdAt = Instant.now().toString()
                )
            )
        }
    }
}


