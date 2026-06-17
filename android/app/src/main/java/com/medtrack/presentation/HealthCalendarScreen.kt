package com.medtrack.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
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
import java.time.LocalDate

@Composable
fun HealthCalendarScreen(
    viewModel: DashboardViewModel,
    onBack: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val role = state.user?.profileRole?.lowercase().orEmpty()

    val today = LocalDate.now()
    var selectedDate by remember { mutableStateOf(today) }

    if (role != "patient") {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
        ) {
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

    val selectedDateLogs = state.logs.filter { log ->
        log.takenAt.startsWith(selectedDate.toString())
    }

    val selectedDateAppointments = state.appointments.filter { appointment ->
        appointment.appointmentDate.startsWith(selectedDate.toString())
    }

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
                text = "Health Calendar",
                color = Color.White,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Select a day to see medication reminders, missed doses, health entries and future appointments.",
                color = Color.White.copy(alpha = 0.85f),
                fontSize = 14.sp
            )

            CalendarLegendCard()

            MonthCalendarGrid(
                today = today,
                selectedDate = selectedDate,
                logs = state.logs,
                appointments = state.appointments,
                onDateSelected = { selectedDate = it }
            )

            SelectedDayCard(
                selectedDate = selectedDate,
                isToday = selectedDate == today,
                logs = selectedDateLogs,
                appointments = selectedDateAppointments
            )

            DayStatusCard(
                logs = selectedDateLogs,
                appointments = selectedDateAppointments
            )

            DailyRemindersCard(
                selectedDate = selectedDate,
                today = today,
                plans = state.activePlans,
                logs = selectedDateLogs,
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

            AppointmentsForDayCard(
                appointments = selectedDateAppointments
            )

            TreatmentTimelineCard(
                logs = selectedDateLogs,
                selectedDate = selectedDate
            )

            HealthStatsCard(
                logs = state.logs
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun CalendarLegendCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.96f)
        )
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Legend",
                fontWeight = FontWeight.Bold,
                color = Color(0xFF667EEA)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("🟢 Taken", fontSize = 12.sp)
                Text("🟡 Skipped", fontSize = 12.sp)
                Text("🔴 Missed", fontSize = 12.sp)
                Text("🔵 Appointment", fontSize = 12.sp)
            }
        }
    }
}

@Composable
private fun MonthCalendarGrid(
    today: LocalDate,
    selectedDate: LocalDate,
    logs: List<com.medtrack.data.local.entity.MedicationLogEntity>,
    appointments: List<com.medtrack.data.local.entity.AppointmentEntity>,
    onDateSelected: (LocalDate) -> Unit
) {
    val days = remember(today) {
        (0..27).map { today.plusDays(it.toLong()) }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.16f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = "Next 4 weeks",
                color = Color.White,
                fontWeight = FontWeight.Bold
            )

            days.chunked(7).forEach { week ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    week.forEach { date ->
                        CalendarDayItem(
                            date = date,
                            isSelected = date == selectedDate,
                            isToday = date == today,
                            statusColor = getDayColor(
                                date = date,
                                logs = logs,
                                appointments = appointments
                            ),
                            onClick = { onDateSelected(date) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CalendarDayItem(
    date: LocalDate,
    isSelected: Boolean,
    isToday: Boolean,
    statusColor: Color,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }
    ) {
        Text(
            text = date.dayOfWeek.name.take(3),
            color = Color.White.copy(alpha = 0.8f),
            fontSize = 10.sp
        )

        Spacer(modifier = Modifier.height(5.dp))

        Surface(
            shape = CircleShape,
            color = if (isSelected) Color.White else statusColor.copy(alpha = 0.85f)
        ) {
            Text(
                text = date.dayOfMonth.toString(),
                modifier = Modifier.padding(10.dp),
                color = if (isSelected) Color(0xFF667EEA) else Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
            )
        }

        if (isToday) {
            Text(
                text = "Today",
                color = Color.White,
                fontSize = 9.sp
            )
        }
    }
}

private fun getDayColor(
    date: LocalDate,
    logs: List<com.medtrack.data.local.entity.MedicationLogEntity>,
    appointments: List<com.medtrack.data.local.entity.AppointmentEntity>
): Color {
    val dayLogs = logs.filter { it.takenAt.startsWith(date.toString()) }
    val hasAppointment = appointments.any { it.appointmentDate.startsWith(date.toString()) }

    return when {
        dayLogs.any { it.status == "missed" } -> Color(0xFFC62828)
        dayLogs.any { it.status == "skipped" } -> Color(0xFFF9A825)
        dayLogs.any { it.status == "taken" } -> Color(0xFF2E7D32)
        hasAppointment -> Color(0xFF1976D2)
        else -> Color.White.copy(alpha = 0.25f)
    }
}

@Composable
private fun SelectedDayCard(
    selectedDate: LocalDate,
    isToday: Boolean,
    logs: List<com.medtrack.data.local.entity.MedicationLogEntity>,
    appointments: List<com.medtrack.data.local.entity.AppointmentEntity>
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
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = if (isToday) "Today" else selectedDate.dayOfWeek.name.lowercase()
                    .replaceFirstChar { it.uppercase() },
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium
            )

            Text(
                text = selectedDate.toString(),
                color = Color(0xFF667EEA),
                fontWeight = FontWeight.SemiBold
            )

            Text(
                text = "${logs.size} health activity item(s) • ${appointments.size} appointment(s)",
                color = Color.Gray,
                fontSize = 13.sp
            )
        }
    }
}

@Composable
private fun DayStatusCard(
    logs: List<com.medtrack.data.local.entity.MedicationLogEntity>,
    appointments: List<com.medtrack.data.local.entity.AppointmentEntity>
) {
    val taken = logs.count { it.status == "taken" }
    val skipped = logs.count { it.status == "skipped" }
    val missed = logs.count { it.status == "missed" }

    val statusText = when {
        missed > 0 -> "Some medication was missed on this day."
        skipped > 0 -> "Some medication was skipped on this day."
        taken > 0 -> "Medication completed for this day."
        appointments.isNotEmpty() -> "You have appointment(s) on this day."
        else -> "No status recorded yet."
    }

    val color = when {
        missed > 0 -> Color(0xFFC62828)
        skipped > 0 -> Color(0xFFF9A825)
        taken > 0 -> Color(0xFF2E7D32)
        appointments.isNotEmpty() -> Color(0xFF1976D2)
        else -> Color.Gray
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.12f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = "Day status",
                fontWeight = FontWeight.Bold,
                color = color
            )

            Text(
                text = statusText,
                color = Color.DarkGray
            )

            Text(
                text = "Taken: $taken  •  Skipped: $skipped  •  Missed: $missed  •  Appointments: ${appointments.size}",
                color = color,
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp
            )
        }
    }
}

@Composable
private fun DailyRemindersCard(
    selectedDate: LocalDate,
    today: LocalDate,
    plans: List<com.medtrack.data.local.entity.PatientMedicationEntity>,
    logs: List<com.medtrack.data.local.entity.MedicationLogEntity>,
    onTaken: (String) -> Unit,
    onSkipped: (String) -> Unit,
    onMissed: (String) -> Unit
) {
    val canMarkStatus = selectedDate == today

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
                text = "Medication reminders",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium
            )

            if (plans.isEmpty()) {
                Text("No medication reminders.", color = Color.Gray)
            } else {
                plans.forEach { plan ->
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
                        canMarkStatus = canMarkStatus,
                        onTaken = { onTaken(medicationName) },
                        onSkipped = { onSkipped(medicationName) },
                        onMissed = { onMissed(medicationName) }
                    )
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
    canMarkStatus: Boolean,
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
                    Text("$time  $medicationName", fontWeight = FontWeight.Bold)
                    Text("Dose: $dose", color = Color.DarkGray, fontSize = 13.sp)
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

            if (canMarkStatus && status == "pending") {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = onTaken) { Text("Taken") }
                    Button(onClick = onSkipped) { Text("Skipped") }
                    Button(onClick = onMissed) { Text("Missed") }
                }
            }
        }
    }
}

@Composable
private fun AppointmentsForDayCard(
    appointments: List<com.medtrack.data.local.entity.AppointmentEntity>
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
                text = "Appointments",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium
            )

            if (appointments.isEmpty()) {
                Text("No appointments for this day.", color = Color.Gray)
            } else {
                appointments.forEach { appointment ->
                    AppointmentMiniCard(appointment)
                }
            }
        }
    }
}

@Composable
private fun AppointmentMiniCard(
    appointment: com.medtrack.data.local.entity.AppointmentEntity
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFEFF2FF)
        )
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Text(
                text = "📅 ${appointment.doctorName}",
                fontWeight = FontWeight.Bold
            )

            Text(
                text = appointment.specialty ?: "General consultation",
                color = Color(0xFF667EEA),
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp
            )

            Text("🕒 ${appointment.appointmentDate}", color = Color.DarkGray, fontSize = 13.sp)
            Text("📍 ${appointment.location ?: "-"}", color = Color.DarkGray, fontSize = 13.sp)

            if (!appointment.notes.isNullOrBlank()) {
                Text("📝 ${appointment.notes}", color = Color.Gray, fontSize = 13.sp)
            }
        }
    }
}

@Composable
private fun TreatmentTimelineCard(
    logs: List<com.medtrack.data.local.entity.MedicationLogEntity>,
    selectedDate: LocalDate
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
                text = "Activity timeline",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium
            )

            Text(
                text = selectedDate.toString(),
                color = Color(0xFF667EEA),
                fontWeight = FontWeight.SemiBold
            )

            if (logs.isEmpty()) {
                Text("No activity for this day.", color = Color.Gray)
            } else {
                logs.forEach { log ->
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
    val time = log.takenAt.substringAfter("T", log.takenAt).take(5)

    val title = when (log.status) {
        "taken" -> "Medication taken ✓"
        "skipped" -> "Medication skipped"
        "missed" -> "Medication missed"
        "daily_health_entry" -> "Health entry ✓"
        "caregiver_note" -> "Caregiver note"
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
            Text(title, fontWeight = FontWeight.Bold)
            Text(log.notes ?: "-", color = Color.DarkGray, fontSize = 13.sp)
        }
    }
}

@Composable
private fun HealthStatsCard(
    logs: List<com.medtrack.data.local.entity.MedicationLogEntity>
) {
    val medicationLogs = logs.filter {
        it.status == "taken" || it.status == "skipped" || it.status == "missed"
    }

    val total = medicationLogs.size
    val taken = medicationLogs.count { it.status == "taken" }
    val skipped = medicationLogs.count { it.status == "skipped" }
    val missed = medicationLogs.count { it.status == "missed" }

    val adherence = if (total == 0) 0 else ((taken.toDouble() / total) * 100).toInt()

    val message = when {
        total == 0 -> "No medication activity yet."
        adherence >= 90 -> "Excellent adherence. Keep it going!"
        adherence >= 70 -> "Good progress, but there is room to improve."
        else -> "Low adherence. Try to follow your treatment schedule."
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
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = "Health Statistics",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium
            )

            Text(
                text = "$adherence%",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.headlineLarge,
                color = when {
                    adherence >= 90 -> Color(0xFF2E7D32)
                    adherence >= 70 -> Color(0xFFF9A825)
                    else -> Color(0xFFC62828)
                }
            )

            Text(
                text = "Medication adherence",
                color = Color.DarkGray,
                fontWeight = FontWeight.SemiBold
            )

            Text(
                text = message,
                color = Color.Gray,
                fontSize = 13.sp
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatMiniCard("Taken", taken, Color(0xFF2E7D32))
                StatMiniCard("Skipped", skipped, Color(0xFFF9A825))
                StatMiniCard("Missed", missed, Color(0xFFC62828))
            }

            Text(
                text = "Total answered reminders: $total",
                color = Color.DarkGray,
                fontSize = 13.sp
            )
        }
    }
}

@Composable
private fun StatMiniCard(
    label: String,
    value: Int,
    color: Color
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.12f)
        )
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value.toString(),
                color = color,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )

            Text(
                text = label,
                color = color,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold
            )
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