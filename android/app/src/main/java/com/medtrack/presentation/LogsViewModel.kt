package com.medtrack.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.medtrack.data.local.entity.MedicationLogEntity
import com.medtrack.domain.repository.MedTrackRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant

data class LogsUiState(
    val logs: List<MedicationLogEntity> = emptyList()
)

class LogsViewModel(
    private val repository: MedTrackRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(LogsUiState())
    val uiState: StateFlow<LogsUiState> = _uiState.asStateFlow()

    init {
        observeLogs()
    }

    private fun observeLogs() {
        viewModelScope.launch {
            repository.observeLogsByPatient(DEMO_PATIENT_ID).collect { logs ->
                _uiState.update { it.copy(logs = logs) }
            }
        }
    }

    fun addTakenLog() {
        viewModelScope.launch {
            repository.addLog(
                MedicationLogEntity(
                    scheduleId = DEMO_SCHEDULE_ID,
                    patientId = DEMO_PATIENT_ID,
                    takenAt = Instant.now().toString(),
                    status = "taken",
                    notes = "Logged from Logs screen"
                )
            )
        }
    }
}

