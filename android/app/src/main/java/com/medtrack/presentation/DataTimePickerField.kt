package com.medtrack.presentation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String = "Tap to select date"
) {
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { showDatePicker = true }
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = {},
            readOnly = true,
            enabled = false,
            label = { Text(label) },
            placeholder = { Text(placeholder) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = OutlinedTextFieldDefaults.colors(
                disabledTextColor = Color.DarkGray,
                disabledBorderColor = Color(0xFF667EEA),
                disabledLabelColor = Color(0xFF667EEA),
                disabledPlaceholderColor = Color.Gray
            )
        )
    }

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        val millis = datePickerState.selectedDateMillis
                        if (millis != null) {
                            val date = SimpleDateFormat(
                                "yyyy-MM-dd",
                                Locale.getDefault()
                            ).format(Date(millis))

                            onValueChange(date)
                        }

                        showDatePicker = false
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateTimePickerField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String = "Tap to select date and time"
) {
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    var selectedDate by remember { mutableStateOf("") }

    val datePickerState = rememberDatePickerState()
    val timePickerState = rememberTimePickerState(is24Hour = true)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { showDatePicker = true }
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = {},
            readOnly = true,
            enabled = false,
            label = { Text(label) },
            placeholder = { Text(placeholder) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = OutlinedTextFieldDefaults.colors(
                disabledTextColor = Color.DarkGray,
                disabledBorderColor = Color(0xFF667EEA),
                disabledLabelColor = Color(0xFF667EEA),
                disabledPlaceholderColor = Color.Gray
            )
        )
    }

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        val millis = datePickerState.selectedDateMillis

                        if (millis != null) {
                            selectedDate = SimpleDateFormat(
                                "yyyy-MM-dd",
                                Locale.getDefault()
                            ).format(Date(millis))

                            showDatePicker = false
                            showTimePicker = true
                        }
                    }
                ) {
                    Text("Next")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showTimePicker) {
        CustomTimePickerDialog(
            onDismissRequest = { showTimePicker = false },
            onConfirm = {
                val selectedTime = "%02d:%02d".format(
                    timePickerState.hour,
                    timePickerState.minute
                )

                onValueChange("$selectedDate $selectedTime")
                showTimePicker = false
            }
        ) {
            TimePicker(
                state = timePickerState,
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimePickerField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String = "Tap to select time"
) {
    var showTimePicker by remember { mutableStateOf(false) }
    val timePickerState = rememberTimePickerState(is24Hour = true)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { showTimePicker = true }
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = {},
            readOnly = true,
            enabled = false,
            label = { Text(label) },
            placeholder = { Text(placeholder) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = OutlinedTextFieldDefaults.colors(
                disabledTextColor = Color.DarkGray,
                disabledBorderColor = Color(0xFF667EEA),
                disabledLabelColor = Color(0xFF667EEA),
                disabledPlaceholderColor = Color.Gray
            )
        )
    }

    if (showTimePicker) {
        CustomTimePickerDialog(
            onDismissRequest = { showTimePicker = false },
            onConfirm = {
                val selectedTime = "%02d:%02d".format(
                    timePickerState.hour,
                    timePickerState.minute
                )

                val updatedValue = if (value.isBlank()) {
                    selectedTime
                } else {
                    "$value, $selectedTime"
                }

                onValueChange(updatedValue)
                showTimePicker = false
            }
        ) {
            TimePicker(
                state = timePickerState,
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}

@Composable
private fun CustomTimePickerDialog(
    onDismissRequest: () -> Unit,
    onConfirm: () -> Unit,
    content: @Composable () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Done")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text("Cancel")
            }
        },
        text = {
            content()
        }
    )
}