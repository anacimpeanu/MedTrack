package com.medtrack.presentation

import android.graphics.BitmapFactory
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

@Composable
fun ProfileSetupScreen(
    viewModel: ProfileSetupViewModel,
    onProfileCompleted: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val photoPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        viewModel.onProfilePhotoUriChange(uri?.toString().orEmpty())
    }

    val photoBitmap = remember(state.profilePhotoUri) {
        state.profilePhotoUri.takeIf { it.isNotBlank() }?.let { uriString ->
            runCatching {
                context.contentResolver.openInputStream(uriString.toUri())?.use { inputStream ->
                    BitmapFactory.decodeStream(inputStream)
                }
            }.getOrNull()
        }
    }

    LaunchedEffect(state.successUserId) {
        state.successUserId?.let { userId ->
            onProfileCompleted(userId)
            viewModel.clearSaveResult()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF667EEA),
                        Color(0xFF764BA2)
                    )
                )
            )
    ) {
        if (state.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = "Loading profile...",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
            return@Box
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Create your medical profile",
                color = Color.White,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Choose patient or caretaker and add your medical details.",
                color = Color.White.copy(alpha = 0.85f),
                fontSize = 14.sp
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.94f))
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Account details",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(text = state.fullName)
                    Text(text = state.email, color = Color.Gray)
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.94f))
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Profile photo",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )

                    Surface(
                        modifier = Modifier.size(120.dp),
                        shape = CircleShape,
                        color = Color(0xFFF0F4FF)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            if (photoBitmap != null) {
                                Image(
                                    bitmap = photoBitmap.asImageBitmap(),
                                    contentDescription = "Profile photo",
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                Text("👤", fontSize = 48.sp)
                            }
                        }
                    }

                    Button(
                        onClick = { photoPicker.launch("image/*") },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF667EEA))
                    ) {
                        Text(if (state.profilePhotoUri.isBlank()) "Choose profile photo" else "Change photo")
                    }
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.94f))
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Profile type",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedButton(
                            onClick = { viewModel.onProfileRoleChange("patient") },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = if (state.profileRole == "patient") Color(0xFF667EEA) else Color.Black
                            )
                        ) {
                            Text("Patient")
                        }
                        OutlinedButton(
                            onClick = { viewModel.onProfileRoleChange("caretaker") },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = if (state.profileRole == "caretaker") Color(0xFF667EEA) else Color.Black
                            )
                        ) {
                            Text("Caretaker")
                        }
                    }
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.94f))
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Medical details",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )

                    OutlinedTextField(
                        value = state.age,
                        onValueChange = viewModel::onAgeChange,
                        label = { Text("Age") },
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = state.birthDate,
                        onValueChange = viewModel::onBirthDateChange,
                        label = { Text("Birth date (YYYY-MM-DD)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = state.cnp,
                        onValueChange = viewModel::onCnpChange,
                        label = { Text("CNP") },
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = state.heightCm,
                        onValueChange = viewModel::onHeightChange,
                        label = { Text("Height (cm)") },
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = state.weightKg,
                        onValueChange = viewModel::onWeightChange,
                        label = { Text("Weight (kg)") },
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text(
                        text = "Sex",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        listOf("male" to "Male", "female" to "Female", "other" to "Other").forEach { (value, label) ->
                            OutlinedButton(
                                onClick = { viewModel.onSexChange(value) },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = if (state.sex == value) Color(0xFF667EEA) else Color.Black
                                )
                            ) {
                                Text(label)
                            }
                        }
                    }
                }
            }

            state.errorMessage?.let { message ->
                Text(text = message, color = Color(0xFFFFD9D9), fontWeight = FontWeight.SemiBold)
            }

            Button(
                onClick = {
                    // If a photo was picked from external provider, copy it into internal storage first.
                    val picked = state.profilePhotoUri.takeIf { it.isNotBlank() }
                    if (picked != null && !picked.startsWith("file://") && !picked.startsWith(context.filesDir.absolutePath)) {
                        val saved = runCatching {
                            // ensure directory
                            val photosDir = File(context.filesDir, "profile_photos")
                            if (!photosDir.exists()) photosDir.mkdirs()
                            val dest = File(photosDir, "profile_${System.currentTimeMillis()}.jpg")
                            context.contentResolver.openInputStream(picked.toUri())?.use { input ->
                                FileOutputStream(dest).use { output ->
                                    input.copyTo(output)
                                }
                            }
                            "file://${dest.absolutePath}"
                        }.getOrNull()

                        if (saved != null) {
                            viewModel.saveProfileWithPhotoPath(saved)
                        } else {
                            // fallback to saving without copying
                            viewModel.saveProfile()
                        }
                    } else {
                        viewModel.saveProfile()
                    }
                },
                enabled = !state.isSaving,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.White)
            ) {
                Text(
                    text = if (state.isSaving) "Saving..." else "Save profile",
                    color = Color(0xFF667EEA),
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}




