package com.spark.ui.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.spark.ui.theme.*

@Composable
fun LoginScreen(onLoginSuccess: () -> Unit) {
    val context = LocalContext.current
    val viewModel: AuthViewModel = org.koin.androidx.compose.koinViewModel()
    val state by viewModel.authState.collectAsState()
    LaunchedEffect(state) {
        if (state is AuthState.Success) onLoginSuccess()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .appBackdrop(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 28.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
                Icon(Icons.Outlined.AutoAwesome, null, tint = SparkCyan, modifier = Modifier.size(22.dp))
                Spacer(Modifier.width(8.dp))
                Text("spark", color = TextPrimary, fontSize = 23.sp, fontWeight = FontWeight.SemiBold,
                    letterSpacing = (-0.8).sp)
            }
            Spacer(Modifier.height(44.dp))
            SparkOrb(Modifier.fillMaxWidth().height(230.dp).reveal())
            Spacer(Modifier.height(32.dp))
            Column(Modifier.fillMaxWidth().reveal(100)) {
                Text("Your time.\nWell spent.", color = TextPrimary,
                    fontSize = 42.sp, lineHeight = 48.sp, fontWeight = FontWeight.Normal,
                    letterSpacing = (-1.5).sp)
                Spacer(Modifier.height(18.dp))
                Text("Less noise. More curiosity. Turn the moments between everything into something meaningful.",
                    color = TextSecondary, fontSize = 16.sp, lineHeight = 25.sp)
            }
            Spacer(Modifier.height(36.dp))
            // Google Sign In Button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .clip(RoundedCornerShape(28.dp))
                    .background(TextPrimary)
                    .clickable(enabled = state !is AuthState.Loading) {
                        viewModel.signInWithGoogle(context)
                    },
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        "G",
                        color = AppBackground,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        if (state is AuthState.Loading) "Please wait..." else "Continue with Google",
                        color = AppBackground,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (state is AuthState.Error) {
                Text((state as AuthState.Error).message, color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center, modifier = Modifier.padding(bottom = 12.dp))
            }

            // Guest Mode Button
            Text(
                text = "Explore as a guest",
                color = TextSecondary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier
                    .clickable(enabled = state !is AuthState.Loading) {
                        viewModel.enterGuestMode(context)
                    }
                    .padding(8.dp)
            )
        }
    }
}
