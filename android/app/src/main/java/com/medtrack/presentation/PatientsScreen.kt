package com.medtrack.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.medtrack.data.local.entity.PatientEntity
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.CardDefaults
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.shape.RoundedCornerShape

@Composable
fun PatientsScreen(viewModel: PatientsViewModel, modifier: Modifier = Modifier) {
    val state by viewModel.uiState.collectAsState()

    Column(
        modifier = modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Button(onClick = { viewModel.addDemoPatient() }) {
            Text("Add demo patient")
        }

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(state.patients) { patient ->
                PatientCard(patient)
            }
        }
    }
}
@Composable
private fun PatientCard(patient: PatientEntity) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {

            Text(
                text = patient.fullName,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Patient Profile",
                color = Color(0xFF667EEA),
                fontWeight = FontWeight.SemiBold
            )

            HorizontalDivider()

            Text("👤 Gender: ${patient.gender ?: "-"}")

            Text("🎂 Birth date: ${patient.birthDate ?: "-"}")

            Text("🩸 Blood type: ${patient.bloodType ?: "-"}")

            Text("⚠️ Allergies: ${patient.allergies ?: "-"}")

            Text("🏥 Conditions: ${patient.chronicConditions ?: "-"}")


            Text(
                text = "Patient ID: ${patient.patientId}",
                color = Color.Gray,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

