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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.medtrack.data.local.entity.PatientMedicationEntity

@Composable
fun TreatmentsScreen(
    viewModel: DashboardViewModel,
    onBack: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()

    val selectedPatient = state.selectedPatientId?.let { patientId ->
        state.patients.firstOrNull { it.patientId == patientId }
    }

    var showAddForm by remember { mutableStateOf(false) }

    var medName by remember { mutableStateOf("") }
    var dose by remember { mutableStateOf("") }
    var unit by remember { mutableStateOf("mg") }
    var freq by remember { mutableStateOf("") }
    var times by remember { mutableStateOf("") }

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
            TextButton(onClick = onBack) {
                Text(
                    text = "← Back",
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }

            Text(
                text = "Treatments",
                color = Color.White,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Manage medication plans and daily schedules.",
                color = Color.White.copy(alpha = 0.85f),
                fontSize = 14.sp
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(26.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White.copy(alpha = 0.16f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = selectedPatient?.fullName ?: "No patient selected",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp
                    )

                    Text(
                        text = "${state.activePlans.size} active treatment(s)",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 13.sp
                    )
                }
            }

            if (selectedPatient == null) {
                EmptyTreatmentCard(
                    title = "No patient selected",
                    message = "Please select a patient before opening treatments."
                )
                return@Column
            }

            Button(
                onClick = { showAddForm = !showAddForm },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White
                )
            ) {
                Text(
                    text = if (showAddForm) "Close form" else "+ Add treatment",
                    color = Color(0xFF667EEA),
                    fontWeight = FontWeight.Bold
                )
            }

            AnimatedVisibility(
                visible = showAddForm,
                enter = fadeIn() + slideInVertically(initialOffsetY = { 40 })
            ) {
                AddTreatmentCard(
                    medName = medName,
                    onMedNameChange = { medName = it },
                    dose = dose,
                    onDoseChange = { dose = it },
                    unit = unit,
                    onUnitChange = { unit = it },
                    freq = freq,
                    onFreqChange = { freq = it },
                    times = times,
                    onTimesChange = { times = it },
                    onSave = {
                        viewModel.addTreatment(
                            medName = medName,
                            dose = dose,
                            unit = unit,
                            freq = freq,
                            times = times
                        )

                        medName = ""
                        dose = ""
                        unit = "mg"
                        freq = ""
                        times = ""
                        showAddForm = false
                    }
                )
            }

            Text(
                text = "Treatment list",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )

            if (state.activePlans.isEmpty()) {
                EmptyTreatmentCard(
                    title = "No treatments yet",
                    message = "Treatments will appear here after they are added."
                )
            } else {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    state.activePlans.forEach { plan ->
                        TreatmentItemCard(
                            plan = plan,
                            onDelete = {
                                viewModel.deleteTreatment(plan.patientMedicationId)
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
private fun AddTreatmentCard(
    medName: String,
    onMedNameChange: (String) -> Unit,
    dose: String,
    onDoseChange: (String) -> Unit,
    unit: String,
    onUnitChange: (String) -> Unit,
    freq: String,
    onFreqChange: (String) -> Unit,
    times: String,
    onTimesChange: (String) -> Unit,
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
                text = "Add new treatment",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium
            )

            Text(
                text = "Add medication name, dose, frequency and intake hours.",
                color = Color.Gray,
                fontSize = 13.sp
            )

            PrettyTreatmentField(
                value = medName,
                onValueChange = onMedNameChange,
                label = "Medication name",
                placeholder = "Ex: Aspirin"
            )

            PrettyTreatmentField(
                value = dose,
                onValueChange = onDoseChange,
                label = "Dose",
                placeholder = "Ex: 500"
            )

            PrettyTreatmentField(
                value = unit,
                onValueChange = onUnitChange,
                label = "Unit",
                placeholder = "mg / ml / tablet"
            )

            PrettyTreatmentField(
                value = freq,
                onValueChange = onFreqChange,
                label = "Frequency",
                placeholder = "daily / twice daily / weekly"
            )

            TimePickerField(
                value = times,
                onValueChange = onTimesChange,
                label = "Times",
                placeholder = "Tap to select one or more times"
            )

            Button(
                onClick = onSave,
                enabled = medName.isNotBlank() && dose.isNotBlank() && freq.isNotBlank(),
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF667EEA)
                )
            ) {
                Text(
                    text = "Save treatment",
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun PrettyTreatmentField(
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
private fun TreatmentItemCard(
    plan: PatientMedicationEntity,
    onDelete: () -> Unit
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
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = "💊",
                fontSize = 28.sp
            )

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = extractTreatmentName(plan.instructions, plan.medicationId),
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )

                Text(
                    text = "Dose: ${plan.dosageAmount} ${plan.dosageUnit}",
                    color = Color.DarkGray,
                    fontSize = 13.sp
                )

                Text(
                    text = "Frequency: ${plan.frequency}",
                    color = Color.DarkGray,
                    fontSize = 13.sp
                )

                Text(
                    text = "Start date: ${plan.startDate}",
                    color = Color.DarkGray,
                    fontSize = 13.sp
                )

                Text(
                    text = if (plan.isActive) "Status: Active" else "Status: Inactive",
                    color = if (plan.isActive) Color(0xFF2E7D32) else Color.Gray,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )

                if (!plan.instructions.isNullOrBlank()) {
                    Text(
                        text = plan.instructions,
                        color = Color.Gray,
                        fontSize = 12.sp
                    )
                }

                Button(
                    onClick = onDelete,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFC62828)
                    )
                ) {
                    Text("Delete")
                }
            }
        }
    }
}

private fun extractTreatmentName(
    instructions: String?,
    medicationId: Long
): String {
    if (instructions.isNullOrBlank()) {
        return "Medication ID: $medicationId"
    }

    val line = instructions
        .lines()
        .firstOrNull { it.startsWith("Medication:") }

    return line
        ?.removePrefix("Medication:")
        ?.trim()
        ?.ifBlank { null }
        ?: instructions.lines().firstOrNull()?.trim()
        ?: "Medication ID: $medicationId"
}

@Composable
private fun EmptyTreatmentCard(
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