package com.medtrack.presentation

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri

@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    onEditProfile: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    var showAddPatientForm by remember { mutableStateOf(false) }
    var subPage by remember { mutableStateOf("overview") }
    var openedPatientId by remember { mutableStateOf<Long?>(null) }

    val role = state.user?.profileRole?.lowercase().orEmpty()

    val selectedPatient = state.selectedPatientId?.let { selectedId ->
        state.patients.firstOrNull { it.patientId == selectedId }
    }

    val profileBitmap = remember(state.user?.profilePhotoUri) {
        state.user?.profilePhotoUri?.takeIf { it.isNotBlank() }?.let { uriString ->
            runCatching {
                context.contentResolver.openInputStream(uriString.toUri())?.use { inputStream ->
                    BitmapFactory.decodeStream(inputStream)
                }
            }.getOrNull()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF667EEA),
                        Color(0xFF764BA2)
                    )
                )
            )
    ) {
        if (state.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Loading dashboard...",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
            return@Box
        }

        if (subPage == "health_calendar") {
            HealthCalendarScreen(
                viewModel = viewModel,
                onBack = { subPage = "overview" }
            )
            return@Box
        }

        if (subPage == "appointments") {
            AppointmentsScreen(
                viewModel = viewModel,
                onBack = { subPage = "overview" }
            )
            return@Box
        }

        if (subPage == "treatments") {
            TreatmentsScreen(
                viewModel = viewModel,
                onBack = { subPage = "overview" }
            )
            return@Box
        }

        if (subPage == "journal") {
            JournalScreen(
                viewModel = viewModel,
                onBack = { subPage = "overview" }
            )
            return@Box
        }

        openedPatientId?.let { patientId ->
            PatientDetailsScreen(
                patientId = patientId,
                viewModel = viewModel,
                onBack = { openedPatientId = null },
                onOpenTreatments = {
                    openedPatientId = null
                    subPage = "treatments"
                },
                onOpenJournal = {
                    openedPatientId = null
                    subPage = "journal"
                }
            )
            return@Box
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Dashboard",
                color = Color.White,
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = if (role == "caretaker") {
                    "Manage your patients, treatments and medical journal."
                } else {
                    "Track your profile, treatment and medical journal."
                },
                color = Color.White.copy(alpha = 0.85f),
                fontSize = 14.sp
            )

            ProfileSummaryCard(
                state = state,
                profileBitmap = profileBitmap,
                onEdit = onEditProfile
            )

            RemoteDashboardCards(
                healthTip = state.healthTip,
                motivationQuote = state.motivationQuote,
                isLoading = state.isLoadingRemoteCards,
                onRefresh = viewModel::loadRemoteDashboardCards
            )

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(
                    onClick = { subPage = "treatments" },
                    enabled = state.selectedPatientId != null,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White)
                ) {
                    Text(
                        text = if (role == "caretaker") "Selected patient treatments" else "My treatments",
                        color = Color(0xFF667EEA)
                    )
                }

                Button(
                    onClick = { subPage = "journal" },
                    enabled = state.selectedPatientId != null,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White)
                ) {
                    Text(
                        text = if (role == "caretaker") "Selected patient journal" else "My journal",
                        color = Color(0xFF667EEA)
                    )
                }

                Button(
                    onClick = { subPage = "appointments" },
                    enabled = state.selectedPatientId != null,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White)
                ) {
                    Text(
                        text = "Appointments",
                        color = Color(0xFF667EEA)
                    )
                }

                if (role == "patient") {
                    Button(
                        onClick = { subPage = "health_calendar" },
                        enabled = state.selectedPatientId != null,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White)
                    ) {
                        Text(
                            text = "Health Calendar",
                            color = Color(0xFF667EEA)
                        )
                    }
                }
            }

            state.errorMessage?.let { message ->
                Text(
                    text = message,
                    color = Color(0xFFFFD9D9),
                    fontWeight = FontWeight.SemiBold
                )
            }

            state.successMessage?.let { message ->
                Text(
                    text = message,
                    color = Color(0xFFE5FFD9),
                    fontWeight = FontWeight.SemiBold
                )
            }

            if (role == "caretaker") {
                CaretakerPatientsCard(
                    patients = state.patients,
                    selectedPatientId = state.selectedPatientId,
                    onPatientSelected = viewModel::selectPatient,
                    onPatientOpen = { patientId ->
                        viewModel.selectPatient(patientId)
                        openedPatientId = patientId
                    },
                    onToggleAddPatient = {
                        showAddPatientForm = !showAddPatientForm
                        viewModel.clearMessages()
                    },
                    showAddPatientForm = showAddPatientForm,
                    viewModel = viewModel
                )
            } else if (role == "patient") {
                PatientBadgeCard(selectedPatient)
            }

            TreatmentCard(
                title = if (role == "caretaker") {
                    "Selected patient treatment"
                } else {
                    "My treatment"
                },
                activePlans = state.activePlans,
                selectedPatient = selectedPatient
            )

            JournalCard(
                title = if (role == "caretaker") {
                    "Selected patient journal"
                } else {
                    "My journal"
                },
                logs = state.logs,
                selectedPatient = selectedPatient
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun ProfileSummaryCard(
    state: DashboardUiState,
    profileBitmap: android.graphics.Bitmap?,
    onEdit: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onEdit() },
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.94f)
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Profile",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    modifier = Modifier.size(84.dp),
                    shape = CircleShape,
                    color = Color(0xFFF0F4FF)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        if (profileBitmap != null) {
                            Image(
                                bitmap = profileBitmap.asImageBitmap(),
                                contentDescription = "Profile photo",
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Text("👤", fontSize = 34.sp)
                        }
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = state.user?.fullName.orEmpty(),
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = state.user?.email.orEmpty(),
                        color = Color.Gray
                    )

                    Text(
                        text = when (state.user?.profileRole?.lowercase()) {
                            "caretaker" -> "Caretaker"
                            else -> "Patient"
                        },
                        color = Color(0xFF667EEA),
                        fontWeight = FontWeight.SemiBold
                    )

                    Text(
                        text = "Age: ${state.user?.age?.toString() ?: "-"} | Sex: ${state.user?.sex ?: "-"}",
                        color = Color.DarkGray,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun PatientBadgeCard(
    patient: com.medtrack.data.local.entity.PatientEntity?
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.94f)
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "My patient record",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            if (patient == null) {
                Text(
                    text = "No patient record found yet.",
                    color = Color.Gray
                )
            } else {
                Text(
                    text = patient.fullName,
                    fontWeight = FontWeight.Bold
                )
                Text(text = "Birth date: ${patient.birthDate ?: "-"}")
                Text(text = "Gender: ${patient.gender ?: "-"}")
            }
        }
    }
}

@Composable
private fun CaretakerPatientsCard(
    patients: List<com.medtrack.data.local.entity.PatientEntity>,
    selectedPatientId: Long?,
    onPatientSelected: (Long) -> Unit,
    onPatientOpen: (Long) -> Unit,
    onToggleAddPatient: () -> Unit,
    showAddPatientForm: Boolean,
    viewModel: DashboardViewModel
) {
    val state by viewModel.uiState.collectAsState()

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.94f)
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Patients under your care",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${patients.size} patient(s)",
                        color = Color.Gray,
                        fontSize = 12.sp
                    )
                }

                Button(onClick = onToggleAddPatient) {
                    Text(if (showAddPatientForm) "Close" else "Add")
                }
            }

            if (patients.isEmpty()) {
                Text(
                    text = "No patients added yet.",
                    color = Color.Gray
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    patients.forEach { patient ->
                        PatientFriendCard(
                            patient = patient,
                            isSelected = selectedPatientId == patient.patientId,
                            lastStatus = state.logs.firstOrNull()?.status,
                            onClick = {
                                onPatientSelected(patient.patientId)
                                onPatientOpen(patient.patientId)
                            }
                        )
                    }
                }
            }

            if (showAddPatientForm) {
                AddPatientForm(viewModel = viewModel)
            }
        }
    }
}

@Composable
private fun PatientFriendCard(
    patient: com.medtrack.data.local.entity.PatientEntity,
    isSelected: Boolean,
    lastStatus: String?,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) Color(0xFFEFF2FF) else Color(0xFFF8F8FF)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(58.dp),
                shape = CircleShape,
                color = Color(0xFF667EEA).copy(alpha = 0.18f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(text = "👤", fontSize = 28.sp)
                }
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Text(
                    text = patient.fullName,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )

                Text(
                    text = "Birth date: ${patient.birthDate ?: "-"}",
                    color = Color.DarkGray,
                    fontSize = 12.sp
                )

                Text(
                    text = "Gender: ${patient.gender ?: "-"}",
                    color = Color.DarkGray,
                    fontSize = 12.sp
                )

                Text(
                    text = when (lastStatus) {
                        "taken" -> "🟢 Last status: medication taken"
                        "skipped" -> "🟡 Last status: skipped"
                        "missed" -> "🔴 Last status: missed"
                        "daily_health_entry" -> "🔵 Last status: health entry"
                        else -> "⚪ No recent activity"
                    },
                    fontSize = 12.sp,
                    color = Color(0xFF667EEA),
                    fontWeight = FontWeight.SemiBold
                )
            }

            Text(
                text = "Open",
                color = Color(0xFF667EEA),
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun AddPatientForm(
    viewModel: DashboardViewModel
) {
    val state by viewModel.uiState.collectAsState()

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = "Available patients",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold
        )

        if (state.availablePatients.isEmpty()) {
            Text(
                text = "No available patients.",
                color = Color.Gray
            )
        } else {
            state.availablePatients.forEach { patient ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFF5F6FF)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = patient.fullName,
                                fontWeight = FontWeight.Bold
                            )

                            Text(text = "Birth date: ${patient.birthDate ?: "-"}")
                            Text(text = "Gender: ${patient.gender ?: "-"}")
                        }

                        Button(
                            onClick = {
                                viewModel.assignPatientToCaretaker(patient.patientId)
                            }
                        ) {
                            Text("Add")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TreatmentCard(
    title: String,
    activePlans: List<com.medtrack.data.local.entity.PatientMedicationEntity>,
    selectedPatient: com.medtrack.data.local.entity.PatientEntity?
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.94f)
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            if (selectedPatient == null) {
                Text(
                    text = "Select a patient to see treatment.",
                    color = Color.Gray
                )
            } else if (activePlans.isEmpty()) {
                Text(
                    text = "No active treatments yet.",
                    color = Color.Gray
                )
            } else {
                activePlans.forEach { plan ->
                    Text(
                        text = plan.instructions ?: "Medication ID: ${plan.medicationId}",
                        fontWeight = FontWeight.Bold
                    )
                    Text("Dose: ${plan.dosageAmount} ${plan.dosageUnit}")
                    Text("Frequency: ${plan.frequency}")
                    Spacer(modifier = Modifier.height(4.dp))
                }
            }
        }
    }
}
@Composable
private fun RemoteDashboardCards(
    healthTip: String,
    motivationQuote: String,
    isLoading: Boolean,
    onRefresh: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.94f)
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Daily Health Tip",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = if (isLoading) {
                    "Loading health tip..."
                } else {
                    healthTip.ifBlank { "No health tip loaded." }
                },
                color = Color.DarkGray
            )

            Text(
                text = "Daily Motivation",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = if (isLoading) {
                    "Loading motivation..."
                } else {
                    motivationQuote.ifBlank { "No motivation loaded." }
                },
                color = Color.DarkGray
            )

            Button(
                onClick = onRefresh,
                enabled = !isLoading,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF667EEA)
                )
            ) {
                Text("Refresh")
            }
        }
    }
}
@Composable
private fun JournalCard(
    title: String,
    logs: List<com.medtrack.data.local.entity.MedicationLogEntity>,
    selectedPatient: com.medtrack.data.local.entity.PatientEntity?
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.94f)
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            if (selectedPatient == null) {
                Text(
                    text = "Select a patient to see journal entries.",
                    color = Color.Gray
                )
            } else if (logs.isEmpty()) {
                Text(
                    text = "No journal entries yet.",
                    color = Color.Gray
                )
            } else {
                logs.forEach { log ->
                    Text(
                        text = "Status: ${log.status}",
                        fontWeight = FontWeight.Bold
                    )
                    Text("Taken at: ${log.takenAt}")
                    Text("Notes: ${log.notes ?: "-"}")
                    Spacer(modifier = Modifier.height(4.dp))
                }
            }
        }
    }
}