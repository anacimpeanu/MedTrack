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
import com.medtrack.data.local.entity.AppointmentEntity

@Composable
fun AppointmentsScreen(
    viewModel: DashboardViewModel,
    onBack: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val role = state.user?.profileRole?.lowercase().orEmpty()

    val selectedPatient = state.selectedPatientId?.let { patientId ->
        state.patients.firstOrNull { it.patientId == patientId }
    }

    var showAddForm by remember { mutableStateOf(false) }

    var doctorName by remember { mutableStateOf("") }
    var specialty by remember { mutableStateOf("") }
    var appointmentDate by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

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
                        text = "Upcoming Appointments",
                        color = Color.White,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = "Organize doctor visits and medical checks in one place.",
                        color = Color.White.copy(alpha = 0.85f),
                        fontSize = 14.sp
                    )
                }
            }

            PatientHeaderCard(
                patientName = selectedPatient?.fullName ?: "No patient selected",
                role = role
            )

            if (selectedPatient == null) {
                EmptyStateCard(
                    title = "No patient selected",
                    message = "Please select a patient before opening appointments."
                )
                return@Column
            }

            if (role == "patient") {
                Button(
                    onClick = { showAddForm = !showAddForm },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White
                    )
                ) {
                    Text(
                        text = if (showAddForm) "Close form" else "+ Add appointment",
                        color = Color(0xFF667EEA),
                        fontWeight = FontWeight.Bold
                    )
                }

                AnimatedVisibility(
                    visible = showAddForm,
                    enter = fadeIn() + slideInVertically(initialOffsetY = { 40 })
                ) {
                    AddAppointmentCard(
                        doctorName = doctorName,
                        onDoctorNameChange = { doctorName = it },
                        specialty = specialty,
                        onSpecialtyChange = { specialty = it },
                        appointmentDate = appointmentDate,
                        onAppointmentDateChange = { appointmentDate = it },
                        location = location,
                        onLocationChange = { location = it },
                        notes = notes,
                        onNotesChange = { notes = it },
                        onSave = {
                            viewModel.addAppointment(
                                doctorName = doctorName,
                                specialty = specialty,
                                appointmentDate = appointmentDate,
                                location = location,
                                notes = notes
                            )

                            doctorName = ""
                            specialty = ""
                            appointmentDate = ""
                            location = ""
                            notes = ""
                            showAddForm = false
                        }
                    )
                }
            } else {
                ReadOnlyInfoCard()
            }

            Text(
                text = "Appointment list",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )

            if (state.appointments.isEmpty()) {
                EmptyStateCard(
                    title = "No appointments yet",
                    message = "Appointments will appear here after they are added."
                )
            } else {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    state.appointments.forEach { appointment ->
                        AppointmentTimelineCard(
                            appointment = appointment,
                            role = role,
                            onAttended = {
                                viewModel.updateAppointmentStatus(
                                    appointmentId = appointment.appointmentId,
                                    status = "attended"
                                )
                            },
                            onMissed = {
                                viewModel.updateAppointmentStatus(
                                    appointmentId = appointment.appointmentId,
                                    status = "missed"
                                )
                            }
                        )
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
private fun PatientHeaderCard(
    patientName: String,
    role: String
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
                modifier = Modifier.padding(2.dp),
                shape = CircleShape,
                color = Color.White.copy(alpha = 0.25f)
            ) {
                Text(
                    text = "🩺",
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
                        "Caretaker view: appointments are read-only"
                    } else {
                        "Patient view: add and track appointments"
                    },
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 13.sp
                )
            }
        }
    }
}

@Composable
private fun AddAppointmentCard(
    doctorName: String,
    onDoctorNameChange: (String) -> Unit,
    specialty: String,
    onSpecialtyChange: (String) -> Unit,
    appointmentDate: String,
    onAppointmentDateChange: (String) -> Unit,
    location: String,
    onLocationChange: (String) -> Unit,
    notes: String,
    onNotesChange: (String) -> Unit,
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
                text = "Add new appointment",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium
            )

            Text(
                text = "Add the doctor, date and location. After saving, it will appear in the list below.",
                color = Color.Gray,
                fontSize = 13.sp
            )

            PrettyTextField(
                value = doctorName,
                onValueChange = onDoctorNameChange,
                label = "Doctor name",
                placeholder = "Ex: Dr. Ionescu"
            )

            PrettyTextField(
                value = specialty,
                onValueChange = onSpecialtyChange,
                label = "Specialty",
                placeholder = "Ex: Cardiologist"
            )

            PrettyTextField(
                value = appointmentDate,
                onValueChange = onAppointmentDateChange,
                label = "Date and time",
                placeholder = "2026-06-20 10:30"
            )

            PrettyTextField(
                value = location,
                onValueChange = onLocationChange,
                label = "Location",
                placeholder = "Clinic / hospital / cabinet"
            )

            PrettyTextField(
                value = notes,
                onValueChange = onNotesChange,
                label = "Notes",
                placeholder = "Bring test results, questions, documents..."
            )

            Button(
                onClick = onSave,
                enabled = doctorName.isNotBlank() && appointmentDate.isNotBlank(),
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF667EEA)
                )
            ) {
                Text(
                    text = "Save appointment",
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun PrettyTextField(
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
private fun AppointmentTimelineCard(
    appointment: AppointmentEntity,
    role: String,
    onAttended: () -> Unit,
    onMissed: () -> Unit
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
                    text = "📅",
                    modifier = Modifier.padding(12.dp),
                    fontSize = 22.sp
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = appointment.doctorName,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )

                Text(
                    text = appointment.specialty ?: "General consultation",
                    color = Color(0xFF667EEA),
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp
                )

                Text(
                    text = "🕒 ${appointment.appointmentDate}",
                    color = Color.DarkGray,
                    fontSize = 13.sp
                )

                Text(
                    text = "📍 ${appointment.location ?: "-"}",
                    color = Color.DarkGray,
                    fontSize = 13.sp
                )

                if (!appointment.notes.isNullOrBlank()) {
                    Text(
                        text = "📝 ${appointment.notes}",
                        color = Color.Gray,
                        fontSize = 13.sp
                    )
                }

                StatusBadge(status = appointment.status)

                if (role == "patient" && appointment.status == "pending") {
                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = onAttended,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF2E7D32)
                            )
                        ) {
                            Text("✓ Attended")
                        }

                        Button(
                            onClick = onMissed,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFC62828)
                            )
                        ) {
                            Text("✕ Missed")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatusBadge(
    status: String
) {
    val text = when (status) {
        "attended" -> "✓ Attended"
        "missed" -> "✕ Missed"
        else -> "Pending"
    }

    val color = when (status) {
        "attended" -> Color(0xFF2E7D32)
        "missed" -> Color(0xFFC62828)
        else -> Color.Gray
    }

    Text(
        text = "Status: $text",
        color = color,
        fontWeight = FontWeight.Bold,
        fontSize = 13.sp
    )
}

@Composable
private fun ReadOnlyInfoCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.96f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = "View-only access",
                fontWeight = FontWeight.Bold,
                color = Color(0xFF667EEA)
            )

            Text(
                text = "Caretakers can view the patient's appointments and their status. Patients can add and update appointments from their account.",
                color = Color.DarkGray,
                fontSize = 13.sp
            )
        }
    }
}

@Composable
private fun EmptyStateCard(
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