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
import androidx.compose.foundation.text.KeyboardOptions
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

@Composable
fun ProfileSetupScreen(
    viewModel: ProfileSetupViewModel,
    onProfileCompleted: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    val photoPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
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
                Brush.verticalGradient(
                    listOf(
                        Color(0xFF667EEA),
                        Color(0xFF764BA2)
                    )
                )
            )
    ) {
        if (state.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
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
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Set up your role, medical details and profile photo.",
                color = Color.White.copy(alpha = 0.85f),
                fontSize = 14.sp
            )

            ProfileHeaderCard(
                fullName = state.fullName,
                email = state.email
            )

            ProfilePhotoCard(
                photoBitmap = photoBitmap,
                hasPhoto = state.profilePhotoUri.isNotBlank(),
                onPickPhoto = {
                    photoPicker.launch("image/*")
                }
            )

            RoleCard(
                selectedRole = state.profileRole,
                onRoleChange = viewModel::onProfileRoleChange
            )

            MedicalDetailsCard(
                age = state.age,
                onAgeChange = viewModel::onAgeChange,
                birthDate = state.birthDate,
                onBirthDateChange = viewModel::onBirthDateChange,
                cnp = state.cnp,
                onCnpChange = viewModel::onCnpChange,
                heightCm = state.heightCm,
                onHeightChange = viewModel::onHeightChange,
                weightKg = state.weightKg,
                onWeightChange = viewModel::onWeightChange,
                sex = state.sex,
                onSexChange = viewModel::onSexChange
            )

            state.errorMessage?.let { message ->
                Text(
                    text = message,
                    color = Color(0xFFFFD9D9),
                    fontWeight = FontWeight.SemiBold
                )
            }

            Button(
                onClick = {
                    val picked = state.profilePhotoUri.takeIf { it.isNotBlank() }

                    if (
                        picked != null &&
                        !picked.startsWith("file://") &&
                        !picked.startsWith(context.filesDir.absolutePath)
                    ) {
                        val saved = runCatching {
                            val photosDir = File(context.filesDir, "profile_photos")
                            if (!photosDir.exists()) {
                                photosDir.mkdirs()
                            }

                            val dest = File(
                                photosDir,
                                "profile_${System.currentTimeMillis()}.jpg"
                            )

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
                            viewModel.saveProfile()
                        }
                    } else {
                        viewModel.saveProfile()
                    }
                },
                enabled = !state.isSaving,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White
                )
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

@Composable
private fun ProfileHeaderCard(
    fullName: String,
    email: String
) {
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
                text = "Account details",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = fullName,
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp
            )

            Text(
                text = email,
                color = Color.Gray,
                fontSize = 13.sp
            )
        }
    }
}

@Composable
private fun ProfilePhotoCard(
    photoBitmap: android.graphics.Bitmap?,
    hasPhoto: Boolean,
    onPickPhoto: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.94f)
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Profile photo",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Surface(
                modifier = Modifier.size(136.dp),
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
                        Text("👤", fontSize = 52.sp)
                    }
                }
            }

            Button(
                onClick = onPickPhoto,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF667EEA)
                )
            ) {
                Text(
                    text = if (hasPhoto) "Change photo" else "Choose profile photo"
                )
            }
        }
    }
}

@Composable
private fun RoleCard(
    selectedRole: String,
    onRoleChange: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.94f)
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Profile type",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                RoleButton(
                    label = "Patient",
                    selected = selectedRole == "patient",
                    onClick = { onRoleChange("patient") },
                    modifier = Modifier.weight(1f)
                )

                RoleButton(
                    label = "Caretaker",
                    selected = selectedRole == "caretaker",
                    onClick = { onRoleChange("caretaker") },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun RoleButton(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = if (selected) Color(0xFF667EEA) else Color.Black
        )
    ) {
        Text(
            text = if (selected) "✓ $label" else label,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun MedicalDetailsCard(
    age: String,
    onAgeChange: (String) -> Unit,
    birthDate: String,
    onBirthDateChange: (String) -> Unit,
    cnp: String,
    onCnpChange: (String) -> Unit,
    heightCm: String,
    onHeightChange: (String) -> Unit,
    weightKg: String,
    onWeightChange: (String) -> Unit,
    sex: String,
    onSexChange: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.94f)
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Medical details",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            OutlinedTextField(
                value = age,
                onValueChange = onAgeChange,
                label = { Text("Age") },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number
                ),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp)
            )

            DatePickerField(
                value = birthDate,
                onValueChange = onBirthDateChange,
                label = "Birth date"
            )

            OutlinedTextField(
                value = cnp,
                onValueChange = onCnpChange,
                label = { Text("CNP") },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number
                ),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp)
            )

            OutlinedTextField(
                value = heightCm,
                onValueChange = onHeightChange,
                label = { Text("Height (cm)") },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number
                ),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp)
            )

            OutlinedTextField(
                value = weightKg,
                onValueChange = onWeightChange,
                label = { Text("Weight (kg)") },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Decimal
                ),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp)
            )

            Text(
                text = "Sex",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SexButton(
                    label = "Male",
                    selected = sex == "male",
                    onClick = { onSexChange("male") },
                    modifier = Modifier.weight(1f)
                )

                SexButton(
                    label = "Female",
                    selected = sex == "female",
                    onClick = { onSexChange("female") },
                    modifier = Modifier.weight(1f)
                )

                SexButton(
                    label = "Other",
                    selected = sex == "other",
                    onClick = { onSexChange("other") },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun SexButton(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = if (selected) Color(0xFF667EEA) else Color.Black
        )
    ) {
        Text(
            text = if (selected) "✓ $label" else label,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}