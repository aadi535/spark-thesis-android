package com.spark.ui.theme

import android.app.Activity
import androidx.compose.material3.Shapes
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private fun DarkColorScheme() = darkColorScheme(
    primary = SparkCyan, secondary = MasteryPurple, tertiary = WondersGreen,
    background = AppBackground, surface = CardBackground, surfaceVariant = SurfaceVariant,
    onPrimary = OnAccent, onSecondary = AppBackground, onTertiary = AppBackground,
    onBackground = TextPrimary, onSurface = TextPrimary, onSurfaceVariant = TextSecondary,
    outline = GlassEdge, outlineVariant = GlassEdge
)

private fun LightColorScheme() = lightColorScheme(
    primary = SparkCyan, secondary = MasteryPurple, tertiary = WondersGreen,
    background = AppBackground, surface = CardBackground, surfaceVariant = SurfaceVariant,
    onPrimary = Color.White, onSecondary = Color.White, onTertiary = Color.White,
    onBackground = TextPrimary, onSurface = TextPrimary, onSurfaceVariant = TextSecondary,
    outline = GlassEdge, outlineVariant = GlassEdge
)

@Composable
fun SparkTheme(
    content: @Composable () -> Unit
) {
    val isLight = ThemeManager.isLightMode
    val colorScheme = if (isLight) LightColorScheme() else DarkColorScheme()

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                val insetsController = WindowCompat.getInsetsController(window, view)
                insetsController.isAppearanceLightStatusBars = isLight
                insetsController.isAppearanceLightNavigationBars = isLight
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = Shapes(small = RoundedCornerShape(12.dp), medium = RoundedCornerShape(20.dp), large = RoundedCornerShape(28.dp)),
        content = content
    )
}
