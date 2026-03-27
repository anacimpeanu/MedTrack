package com.medtrack.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.weight
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
import com.medtrack.data.local.entity.MedicationScheduleEntity
import com.medtrack.data.local.entity.PatientMedicationEntity

@Composable
fun TreatmentPlanScreen(viewModel: TreatmentPlanViewModel, modifier: Modifier = Modifier) {
    val state by viewModel.uiState.collectAsState()

    Column(
        modifier = modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Button(onClick = { viewModel.addDemoTreatmentPlan() }) {
            Text("Add demo treatment")
        }

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                Text("Active plans", style = MaterialTheme.typography.titleMedium)
            }
            items(state.activePlans) { plan ->
                PlanCard(plan)
            }
            item {
                Text("Schedule", style = MaterialTheme.typography.titleMedium)
            }
            items(state.schedules) { schedule ->
                ScheduleCard(schedule)
            }
        }
    }
}

@Composable
private fun PlanCard(plan: PatientMedicationEntity) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("Medication ID: ${plan.medicationId}", style = MaterialTheme.typography.titleSmall)
            Text("Dose: ${plan.dosageAmount} ${plan.dosageUnit}")
            Text("Frequency: ${plan.frequency}")
        }
    }
}

@Composable
private fun ScheduleCard(schedule: MedicationScheduleEntity) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("Time: ${schedule.intakeTime}", style = MaterialTheme.typography.titleSmall)
            Text("Days: ${schedule.daysOfWeek}")
        }
    }
}


