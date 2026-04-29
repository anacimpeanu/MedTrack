package com.medtrack.presentation

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onSplashFinished: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showContent by remember { mutableStateOf(false) }
    
    val alpha by animateFloatAsState(
        targetValue = if (showContent) 1f else 0f,
        animationSpec = tween(1000),
        label = "splash_alpha"
    )

    LaunchedEffect(Unit) {
        showContent = true
        delay(3000)
        onSplashFinished()
    }

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
            ),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .alpha(alpha)
                .clip(RoundedCornerShape(32.dp))
                .background(Color.White.copy(alpha = 0.15f))
                .clip(RoundedCornerShape(32.dp)),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(28.dp))
                    .background(Color.White.copy(alpha = 0.2f))
                    .clip(RoundedCornerShape(28.dp))
            ) {
                Text(
                    "🏥",
                    fontSize = 120.sp,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .clip(RoundedCornerShape(28.dp))
                )
            }
        }

        // App name below logo
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .alpha(alpha)
        ) {
            Text(
                "MedTrack",
                fontSize = 48.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier
                    .align(Alignment.Center)
            )
        }
    }
}

