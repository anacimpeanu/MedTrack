package com.medtrack.presentation

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun WelcomeScreen(
    onLoginClick: () -> Unit,
    onRegisterClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF667EEA),  // Indigo
                        Color(0xFF764BA2)   // Purple
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.SpaceEvenly,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top section with logo and title
            AnimatedVisibility(
                visible = true,
                enter = fadeIn() + slideInVertically()
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(top = 60.dp)
                ) {
                    // Animated logo
                    Surface(
                        modifier = Modifier
                            .size(100.dp)
                            .clip(RoundedCornerShape(32.dp)),
                        color = Color.White.copy(alpha = 0.2f),
                        contentColor = Color.White
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "🏥",
                                fontSize = 64.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    Text(
                        "MedTrack",
                        fontSize = 40.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        "Healthcare Management",
                        fontSize = 16.sp,
                        color = Color.White.copy(alpha = 0.8f),
                        fontWeight = FontWeight.SemiBold
                    )

                    Text(
                        "Made Simple",
                        fontSize = 16.sp,
                        color = Color.White.copy(alpha = 0.8f),
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // Features section
            AnimatedVisibility(
                visible = true,
                enter = fadeIn() + slideInVertically(initialOffsetY = { 100 })
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(20.dp)
                ) {
                    FeatureItem("📋 Manage Patients", "Keep track of all patient information")
                    Spacer(modifier = Modifier.height(16.dp))
                    FeatureItem("💊 Treatment Plans", "Create and monitor treatment plans")
                    Spacer(modifier = Modifier.height(16.dp))
                    FeatureItem("📊 Medical Logs", "Record and review medical history")
                }
            }

            // Bottom section with buttons
            AnimatedVisibility(
                visible = true,
                enter = fadeIn() + slideInVertically(initialOffsetY = { 100 })
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 40.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Login Button
                    Button(
                        onClick = onLoginClick,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .clip(RoundedCornerShape(12.dp)),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White,
                            contentColor = Color(0xFF667EEA)
                        )
                    ) {
                        Text(
                            "Login",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    }

                    // Register Button
                    OutlinedButton(
                        onClick = onRegisterClick,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .border(2.dp, Color.White, RoundedCornerShape(12.dp))
                    ) {
                        Text(
                            "Create Account",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            color = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        "Secure • Fast • Reliable",
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.6f),
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                }
            }
        }
    }
}

// FeatureItem composable for displaying app features
@Composable
fun FeatureItem(
    title: String,
    description: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White.copy(alpha = 0.1f))
            .padding(16.dp)
    ) {
        Text(
            title,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            description,
            fontSize = 13.sp,
            color = Color.White.copy(alpha = 0.7f),
            fontWeight = FontWeight.Medium
        )
    }
}

