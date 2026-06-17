package com.medtrack.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun PatientDetailsScreen(
    patientId: Long,
    viewModel: DashboardViewModel,
    onBack: () -> Unit,
    onOpenTreatments: () -> Unit,
    onOpenJournal: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()

    val patient = state.patients.firstOrNull {
        it.patientId == patientId
    }

    if (patient == null) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            TextButton(onClick = onBack) {
                Text("Back")
            }

            Text("Patient not found.", color = Color.Red)
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        TextButton(onClick = onBack) {
            Text("Back")
        }

        Text(
            text = patient.fullName,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text("Patient details", fontWeight = FontWeight.Bold)
                Text("Birth date: ${patient.birthDate ?: "-"}")
                Text("Gender: ${patient.gender ?: "-"}")
                Text("Blood type: ${patient.bloodType ?: "-"}")
                Text("Allergies: ${patient.allergies ?: "-"}")
                Text("Chronic conditions: ${patient.chronicConditions ?: "-"}")
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Active treatments", fontWeight = FontWeight.Bold)
                    Text(state.activePlans.size.toString())
                }

                Column {
                    Text("Journal entries", fontWeight = FontWeight.Bold)
                    Text(state.logs.size.toString())
                }
            }
        }

        Button(
            onClick = {
                viewModel.selectPatient(patient.patientId)
                onOpenTreatments()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Manage treatments")
        }

        Button(
            onClick = {
                viewModel.selectPatient(patient.patientId)
                onOpenJournal()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Open journal")
        }
    }
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
                text = "Medical Summary",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium
            )

            Text("Blood Type: ${patient.bloodType ?: "-"}")

            Text(
                text = "Allergies:",
                fontWeight = FontWeight.SemiBold
            )
            Text(patient.allergies ?: "-")

            Text(
                text = "Conditions:",
                fontWeight = FontWeight.SemiBold
            )
            Text(patient.chronicConditions ?: "-")
        }
    }
}