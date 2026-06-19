package com.medtrack.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.medtrack.data.local.entity.MedicationLogEntity
import com.medtrack.data.local.entity.PatientEntity
import com.medtrack.data.local.entity.PatientMedicationEntity
import com.medtrack.data.local.entity.UserEntity
import com.medtrack.domain.repository.MedTrackRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class DashboardUiState(
    val isLoading: Boolean = true,
    val isSavingPatient: Boolean = false,
    val user: UserEntity? = null,

    // pentru patient = propriul patient record
    // pentru caretaker = pacienții luați în grijă
    val patients: List<PatientEntity> = emptyList(),

    // pacienți fără îngrijitor, disponibili pentru caretaker
    val availablePatients: List<PatientEntity> = emptyList(),

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
    val successMessage: String? = null,

    val appointments: List<com.medtrack.data.local.entity.AppointmentEntity> = emptyList(),
    val healthTip: String = "",
    val motivationQuote: String = "",
    val isLoadingRemoteCards: Boolean = false,
    val patientPhotoMap: Map<Long, String?> = emptyMap(),
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
        observeAvailablePatients()
        observePlans()
        observeLogs()
        observeAppointments()
        loadRemoteDashboardCards()
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

                if (user != null && user.profileRole.lowercase() == "patient") {
                    selectedPatientIdFlow.value = user.userId
                    _uiState.update {
                        it.copy(selectedPatientId = user.userId)
                    }
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
            val user = repository.getUserById(userId)

            if (user?.profileRole?.lowercase() == "caretaker") {
                repository.observePatientsByCaretaker(userId).collect { patients ->
                    _uiState.update {
                        it.copy(patients = patients)
                    }

                    loadPatientPhotos(patients)

                    val selectedId = selectedPatientIdFlow.value
                    val stillExists = selectedId != null &&
                            patients.any { patient ->
                                patient.patientId == selectedId
                            }

                    if (!stillExists) {
                        val firstPatientId = patients.firstOrNull()?.patientId
                        selectedPatientIdFlow.value = firstPatientId

                        _uiState.update {
                            it.copy(selectedPatientId = firstPatientId)
                        }
                    }
                }
            } else {
                repository.observePatientsByUser(userId).collect { patients ->
                    _uiState.update {
                        it.copy(patients = patients)
                    }

                    val firstPatientId = patients.firstOrNull()?.patientId
                    if (firstPatientId != null) {
                        selectedPatientIdFlow.value = firstPatientId
                        _uiState.update {
                            it.copy(selectedPatientId = firstPatientId)
                        }
                    }
                }
            }
        }
    }

    private fun observeAvailablePatients() {
        viewModelScope.launch {
            repository.observeAvailablePatients().collect { patients ->
                _uiState.update {
                    it.copy(availablePatients = patients)
                }
                loadPatientPhotos(patients)
            }
        }
    }

    private fun observePlans() {
        viewModelScope.launch {
            selectedPatientIdFlow
                .filterNotNull()
                .distinctUntilChanged()
                .flatMapLatest { patientId ->
                    repository.observeActivePlans(patientId)
                }
                .collect { plans ->
                    _uiState.update {
                        it.copy(activePlans = plans)
                    }
                }
        }
    }

    private fun observeLogs() {
        viewModelScope.launch {
            selectedPatientIdFlow
                .filterNotNull()
                .distinctUntilChanged()
                .flatMapLatest { patientId ->
                    repository.observeLogsByPatient(patientId)
                }
                .collect { logs ->
                    _uiState.update {
                        it.copy(logs = logs)
                    }
                }
        }
    }

    fun selectPatient(patientId: Long) {
        selectedPatientIdFlow.value = patientId

        _uiState.update {
            it.copy(
                selectedPatientId = patientId,
                successMessage = null,
                errorMessage = null
            )
        }
    }

    fun assignPatientToCaretaker(patientId: Long) {
        val user = _uiState.value.user

        if (user == null || user.profileRole.lowercase() != "caretaker") {
            _uiState.update {
                it.copy(errorMessage = "Only caretakers can add patients.")
            }
            return
        }

        viewModelScope.launch {
            try {
                repository.assignCaretakerToPatient(
                    patientId = patientId,
                    caretakerId = user.userId
                )

                selectedPatientIdFlow.value = patientId

                _uiState.update {
                    it.copy(
                        selectedPatientId = patientId,
                        successMessage = "Patient added to your care.",
                        errorMessage = null
                    )
                }
            } catch (_: Exception) {
                _uiState.update {
                    it.copy(errorMessage = "Could not add patient.")
                }
            }
        }
    }

    fun clearMessages() {
        _uiState.update {
            it.copy(
                errorMessage = null,
                successMessage = null
            )
        }
    }

    fun addTreatment(
        medName: String,
        dose: String,
        unit: String,
        freq: String,
        times: String
    ) {
        val current = _uiState.value
        val patientId = current.selectedPatientId ?: return
        val user = current.user ?: return

        val addedByText = when (user.profileRole.lowercase()) {
            "caretaker" -> "Added by caretaker: ${user.fullName}"
            else -> "Added by patient: ${user.fullName}"
        }

        viewModelScope.launch {
            try {
                val medicationId = repository.addMedication(
                    com.medtrack.data.local.entity.MedicationEntity(
                        name = medName,
                        description = "",
                        type = "medicine",
                        manufacturer = "",
                        defaultDoseUnit = unit.ifBlank { "mg" }
                    )
                )

                val planId = repository.addPatientMedication(
                    com.medtrack.data.local.entity.PatientMedicationEntity(
                        patientId = patientId,
                        medicationId = medicationId,
                        dosageAmount = dose.toDoubleOrNull() ?: 0.0,
                        dosageUnit = unit.ifBlank { "mg" },
                        frequency = freq,
                        startDate = java.time.LocalDate.now().toString(),
                        instructions = """
                            Medication: $medName
                            Times: $times
                            $addedByText
                        """.trimIndent(),
                        isActive = true
                    )
                )

                times
                    .split(",", ";")
                    .map { it.trim() }
                    .filter { it.isNotBlank() }
                    .forEach { time ->
                        repository.addSchedule(
                            com.medtrack.data.local.entity.MedicationScheduleEntity(
                                patientMedicationId = planId,
                                intakeTime = time,
                                daysOfWeek = "1,2,3,4,5,6,0",
                                reminderEnabled = true,
                                createdAt = java.time.LocalDate.now().toString()
                            )
                        )
                    }

                _uiState.update {
                    it.copy(successMessage = "Treatment added with schedule.")
                }
            } catch (_: Exception) {
                _uiState.update {
                    it.copy(errorMessage = "Could not add treatment.")
                }
            }
        }
    }

    fun updateTreatment(item: PatientMedicationEntity) {
        viewModelScope.launch {
            try {
                repository.updatePatientMedication(item)
                _uiState.update {
                    it.copy(successMessage = "Treatment updated.")
                }
            } catch (_: Exception) {
                _uiState.update {
                    it.copy(errorMessage = "Could not update treatment.")
                }
            }
        }
    }

    fun deleteTreatment(itemId: Long) {
        viewModelScope.launch {
            try {
                repository.deletePatientMedication(itemId)
                _uiState.update {
                    it.copy(successMessage = "Treatment deleted.")
                }
            } catch (_: Exception) {
                _uiState.update {
                    it.copy(errorMessage = "Could not delete treatment.")
                }
            }
        }
    }

    fun addJournalLog(note: String) {
        val patientId = _uiState.value.selectedPatientId ?: return

        viewModelScope.launch {
            try {
                repository.addLog(
                    MedicationLogEntity(
                        scheduleId = null,
                        patientId = patientId,
                        takenAt = java.time.Instant.now().toString(),
                        status = note.ifBlank { "note" },
                        notes = note.ifBlank { null }
                    )
                )

                _uiState.update {
                    it.copy(successMessage = "Journal entry added.")
                }
            } catch (_: Exception) {
                _uiState.update {
                    it.copy(errorMessage = "Could not add journal entry.")
                }
            }
        }
    }

    fun updateJournalLog(item: MedicationLogEntity) {
        viewModelScope.launch {
            try {
                repository.updateLog(item)
                _uiState.update {
                    it.copy(successMessage = "Journal updated.")
                }
            } catch (_: Exception) {
                _uiState.update {
                    it.copy(errorMessage = "Could not update journal.")
                }
            }
        }
    }

    fun deleteJournalLog(id: Long) {
        viewModelScope.launch {
            try {
                repository.deleteLog(id)
                _uiState.update {
                    it.copy(successMessage = "Journal entry deleted.")
                }
            } catch (_: Exception) {
                _uiState.update {
                    it.copy(errorMessage = "Could not delete journal entry.")
                }
            }
        }
    }

    fun addPatientOwnTreatment(
        medName: String,
        dose: String,
        unit: String,
        frequency: String,
        times: String
    ) {
        val current = _uiState.value
        val user = current.user

        if (user == null || user.profileRole.lowercase() != "patient") {
            _uiState.update {
                it.copy(errorMessage = "Only patients can add treatments to Health Calendar.")
            }
            return
        }

        val patientId = current.selectedPatientId ?: user.userId

        viewModelScope.launch {
            try {
                val medicationId = repository.addMedication(
                    com.medtrack.data.local.entity.MedicationEntity(
                        name = medName.trim(),
                        description = "",
                        type = "medicine",
                        manufacturer = "",
                        defaultDoseUnit = unit.ifBlank { "mg" }
                    )
                )

                val planId = repository.addPatientMedication(
                    com.medtrack.data.local.entity.PatientMedicationEntity(
                        patientId = patientId,
                        medicationId = medicationId,
                        dosageAmount = dose.toDoubleOrNull() ?: 0.0,
                        dosageUnit = unit.ifBlank { "mg" },
                        frequency = frequency.ifBlank { "daily" },
                        startDate = java.time.LocalDate.now().toString(),
                        instructions = medName.trim(),
                        isActive = true
                    )
                )

                times
                    .split(",", ";")
                    .map { it.trim() }
                    .filter { it.isNotBlank() }
                    .forEach { time ->
                        repository.addSchedule(
                            com.medtrack.data.local.entity.MedicationScheduleEntity(
                                patientMedicationId = planId,
                                intakeTime = time,
                                daysOfWeek = "1,2,3,4,5,6,0",
                                reminderEnabled = true,
                                createdAt = java.time.LocalDate.now().toString()
                            )
                        )
                    }

                _uiState.update {
                    it.copy(successMessage = "Treatment added to Health Calendar.")
                }
            } catch (_: Exception) {
                _uiState.update {
                    it.copy(errorMessage = "Could not add treatment to Health Calendar.")
                }
            }
        }
    }

    fun addMedicationIntakeLog(
        status: String,
        note: String
    ) {
        val current = _uiState.value
        val user = current.user

        if (user == null || user.profileRole.lowercase() != "patient") {
            _uiState.update {
                it.copy(errorMessage = "Only patients can mark medication intake.")
            }
            return
        }

        val patientId = current.selectedPatientId ?: user.userId

        viewModelScope.launch {
            try {
                repository.addLog(
                    MedicationLogEntity(
                        scheduleId = null,
                        patientId = patientId,
                        takenAt = java.time.Instant.now().toString(),
                        status = status,
                        notes = note
                    )
                )

                _uiState.update {
                    it.copy(successMessage = "Medication marked as $status.")
                }
            } catch (_: Exception) {
                _uiState.update {
                    it.copy(errorMessage = "Could not save medication status.")
                }
            }
        }
    }
    fun addDailyHealthEntry(
        tookMedication: String,
        medicationCount: String,
        medicationTime: String,
        feltBad: String,
        symptoms: String
    ) {
        val current = _uiState.value
        val user = current.user
        val patientId = current.selectedPatientId ?: return

        if (user == null || user.profileRole.lowercase() != "patient") {
            _uiState.update {
                it.copy(errorMessage = "Only patients can add daily health entries.")
            }
            return
        }

        val note = """
        Did you take your medication? $tookMedication
        How many did you take? $medicationCount
        At what time? $medicationTime
        Did you feel bad? $feltBad
        Symptoms / notes: ${symptoms.ifBlank { "-" }}
    """.trimIndent()

        viewModelScope.launch {
            try {
                repository.addLog(
                    MedicationLogEntity(
                        scheduleId = null,
                        patientId = patientId,
                        takenAt = java.time.Instant.now().toString(),
                        status = "daily_health_entry",
                        notes = note
                    )
                )

                _uiState.update {
                    it.copy(successMessage = "Daily health entry saved.")
                }
            } catch (_: Exception) {
                _uiState.update {
                    it.copy(errorMessage = "Could not save daily health entry.")
                }
            }
        }
    }
    private fun observeAppointments() {
        viewModelScope.launch {
            selectedPatientIdFlow
                .filterNotNull()
                .distinctUntilChanged()
                .flatMapLatest { patientId ->
                    repository.observeAppointments(patientId)
                }
                .collect { appointments ->
                    _uiState.update {
                        it.copy(appointments = appointments)
                    }
                }
        }
    }

    fun addAppointment(
        doctorName: String,
        specialty: String,
        appointmentDate: String,
        location: String,
        notes: String
    ) {
        val patientId = _uiState.value.selectedPatientId ?: return

        viewModelScope.launch {
            try {
                repository.addAppointment(
                    com.medtrack.data.local.entity.AppointmentEntity(
                        patientId = patientId,
                        doctorName = doctorName.trim(),
                        specialty = specialty.ifBlank { null },
                        appointmentDate = appointmentDate.trim(),
                        location = location.ifBlank { null },
                        notes = notes.ifBlank { null }
                    )
                )

                _uiState.update {
                    it.copy(successMessage = "Appointment added.")
                }
            } catch (_: Exception) {
                _uiState.update {
                    it.copy(errorMessage = "Could not add appointment.")
                }
            }
        }
    }
    fun updateAppointmentStatus(
        appointmentId: Long,
        status: String
    ) {
        viewModelScope.launch {
            try {
                repository.updateAppointmentStatus(
                    appointmentId = appointmentId,
                    status = status
                )

                _uiState.update {
                    it.copy(successMessage = "Appointment updated.")
                }
            } catch (_: Exception) {
                _uiState.update {
                    it.copy(errorMessage = "Could not update appointment.")
                }
            }
        }
    }
    fun addCaregiverNote(note: String) {
        val current = _uiState.value
        val user = current.user
        val patientId = current.selectedPatientId ?: return

        if (user == null || user.profileRole.lowercase() != "caretaker") {
            _uiState.update {
                it.copy(errorMessage = "Only caretakers can add caregiver notes.")
            }
            return
        }

        viewModelScope.launch {
            try {
                repository.addLog(
                    MedicationLogEntity(
                        scheduleId = null,
                        patientId = patientId,
                        takenAt = java.time.Instant.now().toString(),
                        status = "caregiver_note",
                        notes = "Caregiver note by ${user.fullName}: $note"
                    )
                )

                _uiState.update {
                    it.copy(successMessage = "Caregiver note added.")
                }
            } catch (_: Exception) {
                _uiState.update {
                    it.copy(errorMessage = "Could not add caregiver note.")
                }
            }
        }
    }
    fun loadRemoteDashboardCards() {
        viewModelScope.launch {
            _uiState.update {
                it.copy(isLoadingRemoteCards = true)
            }

            try {
                val tip = com.medtrack.data.remote.HealthRemoteDataSource.getHealthTip()
                val quote = com.medtrack.data.remote.HealthRemoteDataSource.getMotivationQuote()

                _uiState.update {
                    it.copy(
                        healthTip = tip,
                        motivationQuote = quote,
                        isLoadingRemoteCards = false
                    )
                }
            } catch (_: Exception) {
                _uiState.update {
                    it.copy(
                        healthTip = "Remember to drink water and take your medication on time.",
                        motivationQuote = "Small steps every day.",
                        isLoadingRemoteCards = false
                    )
                }
            }
        }
    }
    private fun loadPatientPhotos(patients: List<PatientEntity>) {
        viewModelScope.launch {
            val userIds = patients.map { it.userId }.distinct()

            if (userIds.isEmpty()) {
                _uiState.update {
                    it.copy(patientPhotoMap = emptyMap())
                }
                return@launch
            }

            val users = repository.getUsersByIds(userIds)

            val photoMap = users.associate { user ->
                user.userId to user.profilePhotoUri
            }

            _uiState.update {
                it.copy(patientPhotoMap = photoMap)
            }
        }
    }

    fun updateMedicalProfile(
        bloodType: String,
        allergies: String,
        chronicConditions: String,
        emergencyContact: String,
        emergencyPhone: String,
        familyDoctor: String,
        insuranceProvider: String
    ) {
        val patientId = _uiState.value.selectedPatientId ?: return

        viewModelScope.launch {
            try {
                repository.updateMedicalProfile(
                    patientId = patientId,
                    bloodType = bloodType.ifBlank { null },
                    allergies = allergies.ifBlank { null },
                    chronicConditions = chronicConditions.ifBlank { null },
                    emergencyContact = emergencyContact.ifBlank { null },
                    emergencyPhone = emergencyPhone.ifBlank { null },
                    familyDoctor = familyDoctor.ifBlank { null },
                    insuranceProvider = insuranceProvider.ifBlank { null }
                )

                _uiState.update {
                    it.copy(successMessage = "Medical profile updated.")
                }
            } catch (_: Exception) {
                _uiState.update {
                    it.copy(errorMessage = "Could not update medical profile.")
                }
            }
        }
    }
}