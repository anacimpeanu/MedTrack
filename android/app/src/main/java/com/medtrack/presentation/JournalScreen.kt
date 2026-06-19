package com.medtrack.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.medtrack.data.local.entity.MedicationLogEntity

@Composable
fun JournalScreen(
    viewModel: DashboardViewModel,
    onBack: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val role = state.user?.profileRole?.lowercase().orEmpty()

    val selectedPatient = state.selectedPatientId?.let { patientId ->
        state.patients.firstOrNull { it.patientId == patientId }
    }

    var showEntryForm by remember { mutableStateOf(false) }
    var showCaregiverForm by remember { mutableStateOf(false) }

    var tookMedication by remember { mutableStateOf("") }
    var medicationCount by remember { mutableStateOf("") }
    var medicationTime by remember { mutableStateOf("") }
    var feltBad by remember { mutableStateOf("") }
    var symptoms by remember { mutableStateOf("") }
    var caregiverNote by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFF667EEA),
                        Color(0xFF764BA2)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            HeaderBar(onBack = onBack)

            AnimatedVisibility(
                visible = true,
                enter = fadeIn() + slideInVertically(initialOffsetY = { -40 })
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = if (role == "caretaker") {
                            "Patient Health Journal"
                        } else {
                            "My Health Journal"
                        },
                        color = Color.White,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = "Daily symptoms, medication notes and caregiver observations.",
                        color = Color.White.copy(alpha = 0.85f),
                        fontSize = 14.sp
                    )
                }
            }

            PatientJournalHeader(
                patientName = selectedPatient?.fullName ?: "No patient selected",
                role = role,
                entriesCount = state.logs.size
            )

            if (selectedPatient == null) {
                EmptyJournalCard(
                    title = "No patient selected",
                    message = "Please select a patient before opening the journal."
                )
                return@Column
            }

            if (role == "patient") {
                Button(
                    onClick = { showEntryForm = !showEntryForm },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White
                    )
                ) {
                    Text(
                        text = if (showEntryForm) "Close form" else "+ Add daily entry",
                        color = Color(0xFF667EEA),
                        fontWeight = FontWeight.Bold
                    )
                }

                AnimatedVisibility(
                    visible = showEntryForm,
                    enter = fadeIn() + slideInVertically(initialOffsetY = { 40 })
                ) {
                    DailyEntryCard(
                        tookMedication = tookMedication,
                        onTookMedicationChange = { tookMedication = it },
                        medicationCount = medicationCount,
                        onMedicationCountChange = { medicationCount = it },
                        medicationTime = medicationTime,
                        onMedicationTimeChange = { medicationTime = it },
                        feltBad = feltBad,
                        onFeltBadChange = { feltBad = it },
                        symptoms = symptoms,
                        onSymptomsChange = { symptoms = it },
                        onSave = {
                            viewModel.addDailyHealthEntry(
                                tookMedication = tookMedication,
                                medicationCount = medicationCount,
                                medicationTime = medicationTime,
                                feltBad = feltBad,
                                symptoms = symptoms
                            )

                            tookMedication = ""
                            medicationCount = ""
                            medicationTime = ""
                            feltBad = ""
                            symptoms = ""
                            showEntryForm = false
                        }
                    )
                }
            }

            if (role == "caretaker") {
                Button(
                    onClick = { showCaregiverForm = !showCaregiverForm },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White
                    )
                ) {
                    Text(
                        text = if (showCaregiverForm) "Close note" else "+ Add caregiver note",
                        color = Color(0xFF667EEA),
                        fontWeight = FontWeight.Bold
                    )
                }

                AnimatedVisibility(
                    visible = showCaregiverForm,
                    enter = fadeIn() + slideInVertically(initialOffsetY = { 40 })
                ) {
                    CaregiverNoteCard(
                        note = caregiverNote,
                        onNoteChange = { caregiverNote = it },
                        onSave = {
                            viewModel.addCaregiverNote(caregiverNote)
                            caregiverNote = ""
                            showCaregiverForm = false
                        }
                    )
                }
            }

            Text(
                text = "Journal entries",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )

            if (state.logs.isEmpty()) {
                EmptyJournalCard(
                    title = "No entries yet",
                    message = "Daily health entries and caregiver notes will appear here."
                )
            } else {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    state.logs.forEach { log ->
                        JournalEntryCard(log = log)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun HeaderBar(
    onBack: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        TextButton(onClick = onBack) {
            Text(
                text = "← Back",
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        }

        Text(
            text = "MedTrack",
            color = Color.White.copy(alpha = 0.9f),
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun PatientJournalHeader(
    patientName: String,
    role: String,
    entriesCount: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.16f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = Color.White.copy(alpha = 0.25f)
            ) {
                Text(
                    text = "📔",
                    modifier = Modifier.padding(12.dp),
                    fontSize = 24.sp
                )
            }

            Column {
                Text(
                    text = patientName,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp
                )

                Text(
                    text = if (role == "caretaker") {
                        "$entriesCount entry(s) • caregiver view"
                    } else {
                        "$entriesCount entry(s) • personal health tracking"
                    },
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 13.sp
                )
            }
        }
    }
}

@Composable
private fun DailyEntryCard(
    tookMedication: String,
    onTookMedicationChange: (String) -> Unit,
    medicationCount: String,
    onMedicationCountChange: (String) -> Unit,
    medicationTime: String,
    onMedicationTimeChange: (String) -> Unit,
    feltBad: String,
    onFeltBadChange: (String) -> Unit,
    symptoms: String,
    onSymptomsChange: (String) -> Unit,
    onSave: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.96f)
        )
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "Daily questions",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium
            )

            Text(
                text = "Complete this short check-in once per day.",
                color = Color.Gray,
                fontSize = 13.sp
            )

            PrettyJournalField(
                value = tookMedication,
                onValueChange = onTookMedicationChange,
                label = "Ți-ai luat medicamentele?",
                placeholder = "Da / Nu"
            )

            PrettyJournalField(
                value = medicationCount,
                onValueChange = onMedicationCountChange,
                label = "Câte medicamente ai luat?",
                placeholder = "Ex: 2"
            )

            TimePickerField(
                value = medicationTime,
                onValueChange = onMedicationTimeChange,
                label = "La ce oră le-ai luat?",
                placeholder = "Tap to select time"
            )

            PrettyJournalField(
                value = feltBad,
                onValueChange = onFeltBadChange,
                label = "Te-ai simțit rău după ele?",
                placeholder = "Da / Nu"
            )

            PrettyJournalField(
                value = symptoms,
                onValueChange = onSymptomsChange,
                label = "Simptome / observații",
                placeholder = "Ex: amețeală, greață, oboseală..."
            )

            Button(
                onClick = onSave,
                enabled = tookMedication.isNotBlank(),
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF667EEA)
                )
            ) {
                Text(
                    text = "Save daily entry",
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun CaregiverNoteCard(
    note: String,
    onNoteChange: (String) -> Unit,
    onSave: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.96f)
        )
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "Caregiver note",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium
            )

            Text(
                text = "Add a short observation about the patient's condition.",
                color = Color.Gray,
                fontSize = 13.sp
            )

            PrettyJournalField(
                value = note,
                onValueChange = onNoteChange,
                label = "Observation",
                placeholder = "Ex: Patient looked tired today."
            )

            Button(
                onClick = onSave,
                enabled = note.isNotBlank(),
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF667EEA)
                )
            ) {
                Text(
                    text = "Save caregiver note",
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun PrettyJournalField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        placeholder = { Text(placeholder) },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Color(0xFF667EEA),
            focusedLabelColor = Color(0xFF667EEA),
            cursorColor = Color(0xFF667EEA)
        )
    )
}

@Composable
private fun JournalEntryCard(
    log: MedicationLogEntity
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.96f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.Top
        ) {
            Surface(
                shape = CircleShape,
                color = Color(0xFF667EEA).copy(alpha = 0.18f)
            ) {
                Text(
                    text = when (log.status) {
                        "daily_health_entry" -> "📝"
                        "taken" -> "✅"
                        "skipped" -> "⚠️"
                        "missed" -> "❌"
                        "caregiver_note" -> "👥"
                        else -> "📌"
                    },
                    modifier = Modifier.padding(12.dp),
                    fontSize = 22.sp
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = entryTitle(log.status),
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )

                Text(
                    text = formatDateText(log.takenAt),
                    color = Color(0xFF667EEA),
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp
                )

                Text(
                    text = log.notes ?: "-",
                    color = Color.DarkGray,
                    fontSize = 13.sp
                )
            }
        }
    }
}

private fun entryTitle(status: String): String {
    return when (status) {
        "daily_health_entry" -> "Daily health entry"
        "taken" -> "Medication taken"
        "skipped" -> "Medication skipped"
        "missed" -> "Medication missed"
        "caregiver_note" -> "Caregiver note"
        else -> status
    }
}

private fun formatDateText(value: String): String {
    return value
        .replace("T", " ")
        .take(16)
}

@Composable
private fun EmptyJournalCard(
    title: String,
    message: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.96f)
        )
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = title,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = message,
                color = Color.Gray,
                fontSize = 13.sp
            )
        }
    }
}