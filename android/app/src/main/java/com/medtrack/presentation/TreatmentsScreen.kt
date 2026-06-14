package com.medtrack.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.unit.dp

@Composable
fun TreatmentsScreen(
    viewModel: DashboardViewModel,
    onBack: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val selectedPatient = state.selectedPatientId?.let { pid -> state.patients.firstOrNull { it.patientId == pid } }

    var medName by remember { mutableStateOf("") }
    var dose by remember { mutableStateOf("") }
    var freq by remember { mutableStateOf("") }
    var editingPlanId by remember { mutableStateOf<Long?>(null) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        TextButton(onClick = onBack) {
            Text("Back")
        }

        Text(text = "Treatments for: ${selectedPatient?.fullName ?: "-"}")

        if (selectedPatient == null) {
            Text(text = "No patient selected.", color = androidx.compose.ui.graphics.Color.Red)
            return@Column
        }

        LazyColumn(modifier = Modifier.fillMaxWidth()) {
            items(state.activePlans) { plan ->
                Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors()) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Medication ID: ${plan.medicationId}")
                        Text("Dose: ${plan.dosageAmount} ${plan.dosageUnit}")
                        Text("Frequency: ${plan.frequency}")
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(onClick = {
                                // populate fields for edit
                                medName = "Edited med ${plan.medicationId}"
                                dose = plan.dosageAmount.toString()
                                freq = plan.frequency
                                editingPlanId = plan.patientMedicationId
                            }) {
                                Text("Edit")
                            }
                            Button(onClick = { viewModel.deleteTreatment(plan.patientMedicationId) }) {
                                Text("Delete")
                            }
                        }
                    }
                }
            }
        }

        // state variables moved to top

        OutlinedTextField(value = medName, onValueChange = { medName = it }, label = { Text("Medication name") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = dose, onValueChange = { dose = it }, label = { Text("Dose") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = freq, onValueChange = { freq = it }, label = { Text("Frequency") }, modifier = Modifier.fillMaxWidth())

        if (editingPlanId == null) {
            Button(onClick = {
                viewModel.addTreatment(medName, dose, freq)
                medName = ""
                dose = ""
                freq = ""
            }, enabled = true, colors = ButtonDefaults.buttonColors()) {
                Text(text = "Add treatment")
            }
        } else {
            Button(onClick = {
                val updated = com.medtrack.data.local.entity.PatientMedicationEntity(
                    patientMedicationId = editingPlanId!!,
                    patientId = selectedPatient.patientId,
                    medicationId = 0,
                    dosageAmount = dose.toDoubleOrNull() ?: 0.0,
                    dosageUnit = "mg",
                    frequency = freq,
                    startDate = java.time.LocalDate.now().toString(),
                    instructions = null,
                    isActive = true
                )
                viewModel.updateTreatment(updated)
                editingPlanId = null
                medName = ""
                dose = ""
                freq = ""
            }, enabled = editingPlanId != null, colors = ButtonDefaults.buttonColors()) {
                Text(text = "Update treatment")
            }
        }
    }
}





