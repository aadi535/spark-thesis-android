package com.spark.ui.screens.main

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.spark.ui.theme.AppBackground
import com.spark.ui.theme.TextPrimary

@Composable
fun PlaceholderScreen(title: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackground),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = title,
            color = TextPrimary,
            fontSize = 24.sp,
            fontFamily = com.spark.ui.theme.SparkFont,
            fontWeight = FontWeight.SemiBold
        )
    }
}
