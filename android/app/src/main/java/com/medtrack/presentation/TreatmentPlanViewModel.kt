package com.medtrack.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.medtrack.data.local.entity.MedicationEntity
import com.medtrack.data.local.entity.MedicationScheduleEntity
import com.medtrack.data.local.entity.PatientMedicationEntity
import com.medtrack.domain.repository.MedTrackRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

data class TreatmentPlanUiState(
    val activePlans: List<PatientMedicationEntity> = emptyList(),
    val schedules: List<MedicationScheduleEntity> = emptyList()
)

class TreatmentPlanViewModel(
    private val repository: MedTrackRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(TreatmentPlanUiState())
    val uiState: StateFlow<TreatmentPlanUiState> = _uiState.asStateFlow()

    init {
        observePlans()
        observeSchedules()
    }

    private fun observePlans() {
        viewModelScope.launch {
            repository.observeActivePlans(DEMO_PATIENT_ID).collect { plans ->
                _uiState.update { it.copy(activePlans = plans) }
            }
        }
    }

    private fun observeSchedules() {
        viewModelScope.launch {
            repository.observeSchedules(DEMO_PLAN_ID).collect { schedules ->
                _uiState.update { it.copy(schedules = schedules) }
            }
        }
    }

    fun addDemoTreatmentPlan() {
        viewModelScope.launch {
            val suffix = (System.currentTimeMillis() % 10000).toString()
            val medicationId = repository.addMedication(
                MedicationEntity(
                    name = "Demo Med $suffix",
                    description = "Generated from Treatment Plan screen",
                    type = "tablet",
                    manufacturer = "MedTrack",
                    defaultDoseUnit = "mg"
                )
            )

            val planId = repository.addPatientMedication(
                PatientMedicationEntity(
                    patientId = DEMO_PATIENT_ID,
                    medicationId = medicationId,
                    dosageAmount = 10.0,
                    dosageUnit = "mg",
                    frequency = "once_daily",
                    startDate = LocalDate.now().toString(),
                    instructions = "Take with water",
                    isActive = true
                )
            )

            repository.addSchedule(
                MedicationScheduleEntity(
                    patientMedicationId = planId,
                    intakeTime = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm")),
                    daysOfWeek = "1,2,3,4,5,6,0",
                    reminderEnabled = true,
                    createdAt = LocalDate.now().toString()
                )
            )
        }
    }
}

