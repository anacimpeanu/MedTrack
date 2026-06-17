package com.medtrack.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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

@Composable
fun MedicalProfileScreen(
    viewModel: DashboardViewModel,
    onBack: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val role = state.user?.profileRole?.lowercase().orEmpty()
    val canEdit = role == "patient"

    val patient = state.selectedPatientId?.let { patientId ->
        state.patients.firstOrNull { it.patientId == patientId }
    }

    var bloodType by remember(patient?.patientId) { mutableStateOf(patient?.bloodType.orEmpty()) }
    var allergies by remember(patient?.patientId) { mutableStateOf(patient?.allergies.orEmpty()) }
    var chronicConditions by remember(patient?.patientId) { mutableStateOf(patient?.chronicConditions.orEmpty()) }
    var emergencyContact by remember(patient?.patientId) { mutableStateOf(patient?.emergencyContact.orEmpty()) }
    var emergencyPhone by remember(patient?.patientId) { mutableStateOf(patient?.emergencyPhone.orEmpty()) }
    var familyDoctor by remember(patient?.patientId) { mutableStateOf(patient?.familyDoctor.orEmpty()) }
    var insuranceProvider by remember(patient?.patientId) { mutableStateOf(patient?.insuranceProvider.orEmpty()) }

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
                text = "Medical Profile",
                color = Color.White,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = if (canEdit) {
                    "Complete your medical information for your caretaker."
                } else {
                    "View-only medical information for selected patient."
                },
                color = Color.White.copy(alpha = 0.85f),
                fontSize = 14.sp
            )

            if (patient == null) {
                InfoCard("No patient selected.")
                return@Column
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White.copy(alpha = 0.96f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = patient.fullName,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )

                    MedicalField(
                        value = bloodType,
                        onValueChange = { bloodType = it },
                        label = "Blood Type",
                        enabled = canEdit
                    )

                    MedicalField(
                        value = allergies,
                        onValueChange = { allergies = it },
                        label = "Allergies",
                        enabled = canEdit
                    )

                    MedicalField(
                        value = chronicConditions,
                        onValueChange = { chronicConditions = it },
                        label = "Chronic Conditions",
                        enabled = canEdit
                    )

                    MedicalField(
                        value = emergencyContact,
                        onValueChange = { emergencyContact = it },
                        label = "Emergency Contact",
                        enabled = canEdit
                    )

                    MedicalField(
                        value = emergencyPhone,
                        onValueChange = { emergencyPhone = it },
                        label = "Emergency Phone",
                        enabled = canEdit
                    )

                    MedicalField(
                        value = familyDoctor,
                        onValueChange = { familyDoctor = it },
                        label = "Family Doctor",
                        enabled = canEdit
                    )

                    MedicalField(
                        value = insuranceProvider,
                        onValueChange = { insuranceProvider = it },
                        label = "Insurance Provider",
                        enabled = canEdit
                    )

                    if (canEdit) {
                        Button(
                            onClick = {
                                viewModel.updateMedicalProfile(
                                    bloodType = bloodType,
                                    allergies = allergies,
                                    chronicConditions = chronicConditions,
                                    emergencyContact = emergencyContact,
                                    emergencyPhone = emergencyPhone,
                                    familyDoctor = familyDoctor,
                                    insuranceProvider = insuranceProvider
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF667EEA)
                            )
                        ) {
                            Text("Save Medical Profile")
                        }
                    } else {
                        Text(
                            text = "Caretaker has view-only access.",
                            color = Color.Gray,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun MedicalField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    enabled: Boolean
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        enabled = enabled,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp)
    )
}

@Composable
private fun InfoCard(
    text: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.96f)
        )
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(18.dp),
            color = Color.DarkGray
        )
    }
}