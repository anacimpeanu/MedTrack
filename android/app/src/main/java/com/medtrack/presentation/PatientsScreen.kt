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
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(text = patient.fullName, style = MaterialTheme.typography.titleMedium)
            Text(text = "Gender: ${patient.gender ?: "unspecified"}")
            Text(text = "Blood type: ${patient.bloodType ?: "unknown"}")
        }
    }
}

