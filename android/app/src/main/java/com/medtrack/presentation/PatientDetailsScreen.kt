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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

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
        if (patient == null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                HeaderBar(onBack = onBack)

                EmptyPatientCard(
                    title = "Patient not found",
                    message = "This patient could not be loaded."
                )
            }
            return@Box
        }

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
                        text = patient.fullName,
                        color = Color.White,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = "Patient profile, medical summary and activity overview.",
                        color = Color.White.copy(alpha = 0.85f),
                        fontSize = 14.sp
                    )
                }
            }

            PatientHeroCard(
                fullName = patient.fullName,
                birthDate = patient.birthDate ?: "-",
                gender = patient.gender ?: "-"
            )

            StatsCard(
                activeTreatments = state.activePlans.size,
                journalEntries = state.logs.size
            )

//            MedicalSummaryCard(
//                bloodType = patient.bloodType ?: "-",
//                allergies = patient.allergies ?: "-",
//                conditions = patient.chronicConditions ?: "-"
//            )
            PatientHealthStatsCard(
                logs = state.logs
            )

            QuickActionsCard(
                onOpenTreatments = {
                    viewModel.selectPatient(patient.patientId)
                    onOpenTreatments()
                },
                onOpenJournal = {
                    viewModel.selectPatient(patient.patientId)
                    onOpenJournal()
                }
            )

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
private fun PatientHeroCard(
    fullName: String,
    birthDate: String,
    gender: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.16f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = Color.White.copy(alpha = 0.25f)
            ) {
                Text(
                    text = "👤",
                    modifier = Modifier.padding(14.dp),
                    fontSize = 28.sp
                )
            }

            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = fullName,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )

                Text(
                    text = "Birth date: $birthDate",
                    color = Color.White.copy(alpha = 0.85f),
                    fontSize = 13.sp
                )

                Text(
                    text = "Gender: $gender",
                    color = Color.White.copy(alpha = 0.85f),
                    fontSize = 13.sp
                )
            }
        }
    }
}

@Composable
private fun StatsCard(
    activeTreatments: Int,
    journalEntries: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.96f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            StatItem(
                title = "Treatments",
                value = activeTreatments.toString()
            )

            StatItem(
                title = "Journal",
                value = journalEntries.toString()
            )
        }
    }
}

@Composable
private fun StatItem(
    title: String,
    value: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = value,
            color = Color(0xFF667EEA),
            fontWeight = FontWeight.Bold,
            fontSize = 24.sp
        )

        Text(
            text = title,
            color = Color.DarkGray,
            fontSize = 13.sp
        )
    }
}

//@Composable
//private fun MedicalSummaryCard(
//    bloodType: String,
//    allergies: String,
//    conditions: String
//) {
//    Card(
//        modifier = Modifier.fillMaxWidth(),
//        shape = RoundedCornerShape(28.dp),
//        colors = CardDefaults.cardColors(
//            containerColor = Color.White.copy(alpha = 0.96f)
//        )
//    ) {
//        Column(
//            modifier = Modifier.padding(18.dp),
//            verticalArrangement = Arrangement.spacedBy(12.dp)
//        ) {
//            PatientHealthStatsCard(
//                logs = state.logs
//            )
////            Text(
////                text = "Medical Summary",
////                fontWeight = FontWeight.Bold,
////                style = MaterialTheme.typography.titleMedium
////            )
////
////            SummaryRow(
////                icon = "🩸",
////                title = "Blood Type",
////                value = bloodType
////            )
////
////            SummaryRow(
////                icon = "⚠️",
////                title = "Allergies",
////                value = allergies
////            )
////
////            SummaryRow(
////                icon = "📋",
////                title = "Conditions",
////                value = conditions
////            )
//        }
//    }
//}

@Composable
private fun SummaryRow(
    icon: String,
    title: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Surface(
            shape = CircleShape,
            color = Color(0xFF667EEA).copy(alpha = 0.15f)
        ) {
            Text(
                text = icon,
                modifier = Modifier.padding(10.dp),
                fontSize = 18.sp
            )
        }

        Column(
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = title,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF667EEA)
            )

            Text(
                text = value,
                color = Color.DarkGray,
                fontSize = 13.sp
            )
        }
    }
}

@Composable
private fun QuickActionsCard(
    onOpenTreatments: () -> Unit,
    onOpenJournal: () -> Unit
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
                text = "Quick Actions",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium
            )

            Button(
                onClick = onOpenTreatments,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF667EEA)
                )
            ) {
                Text("Manage treatments")
            }

            Button(
                onClick = onOpenJournal,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF667EEA)
                )
            ) {
                Text("Open journal")
            }
        }
    }
}
@Composable
private fun PatientHealthStatsCard(
    logs: List<com.medtrack.data.local.entity.MedicationLogEntity>
) {
    val medicationLogs = logs.filter {
        it.status == "taken" || it.status == "skipped" || it.status == "missed"
    }

    val total = medicationLogs.size
    val taken = medicationLogs.count { it.status == "taken" }
    val skipped = medicationLogs.count { it.status == "skipped" }
    val missed = medicationLogs.count { it.status == "missed" }

    val adherence = if (total == 0) {
        0
    } else {
        ((taken.toDouble() / total) * 100).toInt()
    }

    val message = when {
        total == 0 -> "No medication activity yet."
        adherence >= 90 -> "Excellent adherence."
        adherence >= 70 -> "Good progress, but there is room to improve."
        else -> "Low adherence. Patient may need extra support."
    }

    val mainColor = when {
        adherence >= 90 -> Color(0xFF2E7D32)
        adherence >= 70 -> Color(0xFFF9A825)
        else -> Color(0xFFC62828)
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
                color = mainColor
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
                PatientStatMiniCard("Taken", taken, Color(0xFF2E7D32))
                PatientStatMiniCard("Skipped", skipped, Color(0xFFF9A825))
                PatientStatMiniCard("Missed", missed, Color(0xFFC62828))
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
private fun PatientStatMiniCard(
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

@Composable
private fun EmptyPatientCard(
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