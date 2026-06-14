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
fun JournalScreen(
    viewModel: DashboardViewModel,
    onBack: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val selectedPatient = state.selectedPatientId?.let { pid -> state.patients.firstOrNull { it.patientId == pid } }

    var note by remember { mutableStateOf("") }
    var editingLogId by remember { mutableStateOf<Long?>(null) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        TextButton(onClick = onBack) {
            Text("Back")
        }

        Text(text = "Journal for: ${selectedPatient?.fullName ?: "-"}")

        if (selectedPatient == null) {
            Text(text = "No patient selected.", color = androidx.compose.ui.graphics.Color.Red)
            return@Column
        }

        LazyColumn(modifier = Modifier.fillMaxWidth()) {
            items(state.logs) { log ->
                Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors()) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Status: ${log.status}")
                        Text("Taken at: ${log.takenAt}")
                        Text("Notes: ${log.notes ?: ""}")
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(onClick = {
                                // populate edit
                                note = log.notes ?: log.status
                                editingLogId = log.logId
                            }) {
                                Text("Edit")
                            }
                            Button(onClick = { viewModel.deleteJournalLog(log.logId) }) {
                                Text("Delete")
                            }
                        }
                    }
                }
            }
        }

        // state moved to top

        OutlinedTextField(value = note, onValueChange = { note = it }, label = { Text("Log note / status") }, modifier = Modifier.fillMaxWidth())

        if (editingLogId == null) {
            Button(onClick = {
                viewModel.addJournalLog(note)
                note = ""
            }, enabled = true, colors = ButtonDefaults.buttonColors()) {
                Text(text = "Add log")
            }
        } else {
            Button(onClick = {
                val item = com.medtrack.data.local.entity.MedicationLogEntity(
                    logId = editingLogId!!,
                    scheduleId = 0,
                    patientId = selectedPatient.patientId,
                    takenAt = java.time.Instant.now().toString(),
                    status = note.ifBlank { "note" },
                    notes = note.ifBlank { null }
                )
                viewModel.updateJournalLog(item)
                editingLogId = null
                note = ""
            }, enabled = editingLogId != null, colors = ButtonDefaults.buttonColors()) {
                Text(text = "Update log")
            }
        }
    }
}





