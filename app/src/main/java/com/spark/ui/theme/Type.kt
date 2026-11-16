package com.spark.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import com.spark.R
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val SparkFont = FontFamily(
    Font(R.font.manrope_light, FontWeight.Light),
    Font(R.font.manrope_regular, FontWeight.Normal),
    Font(R.font.manrope_medium, FontWeight.Medium),
    Font(R.font.manrope_semibold, FontWeight.SemiBold),
    Font(R.font.manrope_bold, FontWeight.Bold)
)

private fun type(size: Int, height: Int, weight: FontWeight = FontWeight.Normal) = TextStyle(
    fontFamily = SparkFont, fontWeight = weight,
    fontSize = size.sp, lineHeight = height.sp, letterSpacing = 0.sp
)
val Typography = Typography(
    displayLarge = type(48, 54, FontWeight.SemiBold),
    displayMedium = type(40, 46, FontWeight.SemiBold),
    displaySmall = type(34, 40, FontWeight.SemiBold),
    headlineLarge = type(32, 38, FontWeight.SemiBold),
    headlineMedium = type(28, 34, FontWeight.SemiBold),
    headlineSmall = type(24, 30, FontWeight.SemiBold),
    titleLarge = type(22, 28, FontWeight.SemiBold),
    titleMedium = type(16, 22, FontWeight.Medium),
    titleSmall = type(14, 20, FontWeight.Medium),
    bodyLarge = type(16, 25), bodyMedium = type(14, 22), bodySmall = type(12, 18),
    labelLarge = type(14, 20, FontWeight.Medium),
    labelMedium = type(12, 18, FontWeight.Medium),
    labelSmall = type(11, 16, FontWeight.Medium)
)
