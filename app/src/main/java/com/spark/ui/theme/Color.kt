package com.spark.ui.theme

import androidx.compose.ui.graphics.Color

// Reference palette: warm charcoal, ivory and a restrained ember accent.
val AppBackground: Color get() = if (ThemeManager.isLightMode) Color(0xFFF0EDE8) else Color(0xFF111213)
val CardBackground: Color get() = if (ThemeManager.isLightMode) Color(0xFFFAF8F5) else Color(0xFF1B1D1F)
val SurfaceVariant: Color get() = if (ThemeManager.isLightMode) Color(0xFFDDD9D2) else Color(0xFF2C2E30)
val GlassSurface: Color get() = if (ThemeManager.isLightMode) Color(0xFFF8F6F2) else Color(0xFF212325)
val GlassEdge: Color get() = if (ThemeManager.isLightMode) Color(0xFFFFFFFF).copy(alpha = 0.7f) else Color(0xFFB9B7AB).copy(alpha = 0.12f)
// Legacy names retained so existing callers share one accent without logic changes.
val SparkCyan: Color get() = if (ThemeManager.isLightMode) Color(0xFFAC4A22) else Color(0xFFEFB18B)
val OnAccent: Color get() = if (ThemeManager.isLightMode) Color.White else Color(0xFF17130F)
val SparkOrange = Color(0xFFE69058)
val MasteryPurple: Color get() = if (ThemeManager.isLightMode) Color(0xFF686259) else Color(0xFFD0C8B8)
val WondersGreen: Color get() = if (ThemeManager.isLightMode) Color(0xFF457158) else Color(0xFFA9C4AC)
val AccentGlow = Color(0xFFF48350)
val GradientPurple = Color(0xFF4C352A)
val GradientCyan = Color(0xFFA06642)
val GradientBlue = Color(0xFF655042)
val TextPrimary: Color get() = if (ThemeManager.isLightMode) Color(0xFF252520) else Color(0xFFF2F1EE)
val TextSecondary: Color get() = if (ThemeManager.isLightMode) Color(0xFF686961) else Color(0xFFACAEAF)
val TextMuted: Color get() = if (ThemeManager.isLightMode) Color(0xFF73736A) else Color(0xFF93958A)
