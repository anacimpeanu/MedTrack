package com.medtrack.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import java.time.LocalDate

@Composable
fun HealthCalendarScreen(
    viewModel: DashboardViewModel,
    onBack: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val role = state.user?.profileRole?.lowercase().orEmpty()

    TreatmentTimelineCard(
        logs = state.logs
    )
    HealthStatsCard(
        logs = state.logs
    )
    if (role != "patient") {
        Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
            TextButton(onClick = onBack) {
                Text("Back")
            }

            Text(
                text = "Health Calendar is available only for patients.",
                color = Color.Red,
                fontWeight = FontWeight.Bold
            )
        }
        return
    }

    var medName by remember { mutableStateOf("") }
    var dose by remember { mutableStateOf("") }
    var unit by remember { mutableStateOf("mg") }
    var frequency by remember { mutableStateOf("daily") }
    var times by remember { mutableStateOf("08:00") }


    val today = LocalDate.now()

    Column(
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
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        TextButton(onClick = onBack) {
            Text("Back", color = Color.White)
        }

        Text(
            text = "Health Calendar",
            color = Color.White,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "Today: $today",
            color = Color.White.copy(alpha = 0.85f)
        )

        WeekCalendarRow(today)

        TodaysRemindersCard(
            plans = state.activePlans,
            logs = state.logs,
            onTaken = { medicationName ->
                viewModel.addMedicationIntakeLog(
                    status = "taken",
                    note = "$medicationName marked as taken."
                )
            },
            onSkipped = { medicationName ->
                viewModel.addMedicationIntakeLog(
                    status = "skipped",
                    note = "$medicationName skipped."
                )
            },
            onMissed = { medicationName ->
                viewModel.addMedicationIntakeLog(
                    status = "missed",
                    note = "$medicationName missed."
                )
            }
        )

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
                    text = "Add treatment",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium
                )

                OutlinedTextField(
                    value = medName,
                    onValueChange = { medName = it },
                    label = { Text("Medication name") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = dose,
                    onValueChange = { dose = it },
                    label = { Text("Dose") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = unit,
                    onValueChange = { unit = it },
                    label = { Text("Unit: mg / ml / tablet") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = frequency,
                    onValueChange = { frequency = it },
                    label = { Text("Frequency") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = times,
                    onValueChange = { times = it },
                    label = { Text("Times: 08:00, 14:00, 20:00") },
                    modifier = Modifier.fillMaxWidth()
                )

                Button(
                    onClick = {
                        viewModel.addPatientOwnTreatment(
                            medName = medName,
                            dose = dose,
                            unit = unit,
                            frequency = frequency,
                            times = times
                        )

                        medName = ""
                        dose = ""
                        unit = "mg"
                        frequency = "daily"
                        times = "08:00"
                    },
                    enabled = medName.isNotBlank() && dose.isNotBlank(),
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF667EEA)
                    )
                ) {
                    Text("Add to Health Calendar")
                }
            }
        }
    }
}

@Composable
private fun TodaysRemindersCard(
    plans: List<com.medtrack.data.local.entity.PatientMedicationEntity>,
    logs: List<com.medtrack.data.local.entity.MedicationLogEntity>,
    onTaken: (String) -> Unit,
    onSkipped: (String) -> Unit,
    onMissed: (String) -> Unit
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
                text = "Today's reminders",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium
            )

            if (plans.isEmpty()) {
                Text(
                    text = "No reminders for today.",
                    color = Color.Gray
                )
            } else {
                LazyColumn(
                    modifier = Modifier.height(260.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(plans) { plan ->
                        val medicationName = extractMedicationName(plan.instructions, plan.medicationId)
                        val timeText = extractTimes(plan.instructions)
                        val latestLog = logs.firstOrNull {
                            it.notes?.contains(medicationName) == true
                        }

                        ReminderItemCard(
                            time = timeText,
                            medicationName = medicationName,
                            dose = "${plan.dosageAmount} ${plan.dosageUnit}",
                            status = latestLog?.status ?: "pending",
                            onTaken = { onTaken(medicationName) },
                            onSkipped = { onSkipped(medicationName) },
                            onMissed = { onMissed(medicationName) }
                        )
                    }
                }
            }
        }
    }
}



@Composable
private fun ReminderItemCard(
    time: String,
    medicationName: String,
    dose: String,
    status: String,
    onTaken: () -> Unit,
    onSkipped: () -> Unit,
    onMissed: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFF5F6FF)
        )
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "$time  $medicationName",
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = "Dose: $dose",
                        color = Color.DarkGray
                    )
                }

                Text(
                    text = when (status) {
                        "taken" -> "✓ Taken"
                        "skipped" -> "Skipped"
                        "missed" -> "Missed"
                        else -> "Pending"
                    },
                    color = when (status) {
                        "taken" -> Color(0xFF2E7D32)
                        "skipped" -> Color(0xFFF9A825)
                        "missed" -> Color(0xFFC62828)
                        else -> Color.Gray
                    },
                    fontWeight = FontWeight.Bold
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = onTaken) {
                    Text("Taken")
                }

                Button(onClick = onSkipped) {
                    Text("Skipped")
                }

                Button(onClick = onMissed) {
                    Text("Missed")
                }
            }
        }
    }
}

@Composable
private fun TreatmentTimelineCard(
    logs: List<com.medtrack.data.local.entity.MedicationLogEntity>
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
                text = "Treatment timeline",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium
            )

            if (logs.isEmpty()) {
                Text(
                    text = "No activity yet.",
                    color = Color.Gray
                )
            } else {
                val todayLogs = logs.take(5)

                todayLogs.forEach { log ->
                    TimelineItem(log = log)
                }
            }
        }
    }
}


@Composable
private fun TimelineItem(
    log: com.medtrack.data.local.entity.MedicationLogEntity
) {
    val time = log.takenAt
        .substringAfter("T", log.takenAt)
        .take(5)

    val title = when (log.status) {
        "taken" -> "Medication taken ✓"
        "skipped" -> "Medication skipped"
        "missed" -> "Medication missed"
        "daily_health_entry" -> "Health entry ✓"
        else -> log.status
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = time,
            color = Color(0xFF667EEA),
            fontWeight = FontWeight.Bold
        )

        Column {
            Text(
                text = title,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = log.notes ?: "-",
                color = Color.DarkGray
            )
        }
    }
}
@Composable
private fun HealthStatsCard(
    logs: List<com.medtrack.data.local.entity.MedicationLogEntity>
) {
    val total = logs.count {
        it.status == "taken" || it.status == "skipped" || it.status == "missed"
    }

    val taken = logs.count {
        it.status == "taken"
    }

    val adherence = if (total == 0) {
        0
    } else {
        ((taken.toDouble() / total.toDouble()) * 100).toInt()
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
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "Statistics",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium
            )

            Text(
                text = "$adherence%",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.headlineMedium,
                color = Color(0xFF667EEA)
            )

            Text("Medication adherence")

            Text("Taken: $taken")
            Text("Total reminders answered: $total")
        }
    }
}
@Composable
private fun WeekCalendarRow(today: LocalDate) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        for (i in 0..6) {
            val date = today.plusDays(i.toLong())

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = date.dayOfWeek.name.take(3),
                    color = Color.White.copy(alpha = 0.8f)
                )

                Spacer(modifier = Modifier.height(6.dp))

                Surface(
                    shape = CircleShape,
                    color = if (i == 0) Color.White else Color.White.copy(alpha = 0.25f)
                ) {
                    Text(
                        text = date.dayOfMonth.toString(),
                        modifier = Modifier.padding(12.dp),
                        color = if (i == 0) Color(0xFF667EEA) else Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

private fun extractMedicationName(
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

private fun extractTimes(
    instructions: String?
): String {
    if (instructions.isNullOrBlank()) {
        return "-"
    }

    val line = instructions
        .lines()
        .firstOrNull { it.startsWith("Times:") }

    return line
        ?.removePrefix("Times:")
        ?.trim()
        ?.ifBlank { "-" }
        ?: "-"
}