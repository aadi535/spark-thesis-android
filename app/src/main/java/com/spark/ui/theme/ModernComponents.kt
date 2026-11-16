package com.spark.ui.theme

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.ui.unit.Dp
import kotlin.math.sin
import kotlin.math.cos
import kotlin.math.PI
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

/** Presentation-only motion; no screen state or actions are changed. */
fun Modifier.reveal(delayMillis: Int = 0): Modifier = composed {
    var appeared by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { delay(delayMillis.toLong()); appeared = true }
    val alpha by animateFloatAsState(if (appeared) 1f else 0f, tween(480), label = "reveal")
    val offset by animateFloatAsState(if (appeared) 0f else 18f,
        spring(dampingRatio = 0.85f, stiffness = 180f), label = "rise")
    graphicsLayer { this.alpha = alpha; translationY = offset * density }
}

fun Modifier.softPress(onClick: () -> Unit): Modifier = composed {
    val interactions = remember { MutableInteractionSource() }
    val pressed by interactions.collectIsPressedAsState()
    val scale by animateFloatAsState(if (pressed) 0.97f else 1f,
        spring(dampingRatio = 0.65f, stiffness = 500f), label = "press")
    graphicsLayer { scaleX = scale; scaleY = scale }
        .clickable(interactionSource = interactions, indication = null, onClick = onClick)
}

fun Modifier.appBackdrop(): Modifier {
    val base = AppBackground
    val glow = if (ThemeManager.isLightMode) Color(0xFFC7AE92).copy(alpha = 0.14f) else Color(0xFFA9562D).copy(alpha = 0.045f)
    return drawBehind {
        drawRect(base)
        drawRect(Brush.radialGradient(listOf(glow, Color.Transparent), Offset(0f, 0f), size.width * 0.95f))
    }
}

fun Modifier.premiumSurface(radius: Int = 24): Modifier = this
    .clip(RoundedCornerShape(radius.dp))
    .background(CardBackground)
    .border(0.7.dp, GlassEdge, RoundedCornerShape(radius.dp))

@Composable
fun ScreenHeading(eyebrow: String, title: String, subtitle: String, modifier: Modifier = Modifier) {
    Column(modifier.fillMaxWidth().reveal().padding(top = 14.dp, bottom = 24.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text(eyebrow, color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Normal)
        }
        Spacer(Modifier.height(12.dp))
        Text(title, color = TextPrimary, fontFamily = SparkFont, fontSize = 29.sp, lineHeight = 36.sp,
            fontWeight = FontWeight.Medium, letterSpacing = (-1).sp)
        Spacer(Modifier.height(8.dp))
        Text(subtitle, color = TextSecondary, fontSize = 13.sp, lineHeight = 21.sp)
    }
}

@Composable
fun SectionHeading(title: String, subtitle: String? = null) {
    Column(Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 16.dp)) {
        Text(title, color = TextPrimary, fontSize = 22.sp, fontWeight = FontWeight.SemiBold,
            letterSpacing = (-0.5).sp)
        if (subtitle != null) {
            Spacer(Modifier.height(4.dp))
            Text(subtitle, color = TextSecondary, fontSize = 13.sp, lineHeight = 19.sp)
        }
    }
}

/** A native, softly rotating particle sculpture inspired by the supplied task-app reference. */
@Composable
fun SparkOrb(modifier: Modifier = Modifier, active: Boolean = true) {
    val transition = rememberInfiniteTransition(label = "particleSculpture")
    val phase by transition.animateFloat(0f, (2 * PI).toFloat(),
        infiniteRepeatable(tween(18000, easing = LinearEasing)), label = "orbit")
    val accent = if (active) AccentGlow else TextMuted
    Canvas(modifier) {
        val cx = size.width * 0.5f
        val cy = size.height * 0.51f
        val unit = size.minDimension * 0.34f
        drawCircle(Brush.radialGradient(listOf(accent.copy(alpha = 0.09f), Color.Transparent),
            Offset(cx, cy + unit * 0.45f), unit * 1.3f), unit * 1.3f, Offset(cx, cy + unit * 0.45f))
        for (ring in 0 until 18) {
            val v = ring * 2.0 * PI / 18
            for (point in 0 until 62) {
                val u = point * 2.0 * PI / 62 + phase * 0.12
                val major = 0.78 + 0.21 * cos(v)
                val x = major * cos(u)
                val y = 0.30 * sin(v)
                val z = major * sin(u)
                val tilt = 0.75 + 0.10 * sin(phase.toDouble())
                val py = y * cos(tilt) - z * sin(tilt)
                val depth = y * sin(tilt) + z * cos(tilt)
                val alpha = ((depth + 1.1) / 2.2).toFloat().coerceIn(0.1f, 1f)
                drawCircle(accent.copy(alpha = 0.12f + alpha * 0.74f),
                    (0.45f + alpha * 0.65f).dp.toPx(),
                    Offset(cx + x.toFloat() * unit * 1.12f, cy + py.toFloat() * unit * 1.12f))
            }
        }
    }
}

@Composable
fun FeatureGlyph(symbol: String, size: Dp = 20.dp, tint: Color = TextSecondary) {
    val domain = symbol.lowercase()
    val icon = when {
        "science" in domain || "cosmos" in domain -> Icons.Outlined.Science
        "psychology" in domain || "mind" in domain -> Icons.Outlined.Psychology
        "history" in domain -> Icons.Outlined.AccountBalance
        "biology" in domain || "nature" in domain -> Icons.Outlined.Spa
        "math" in domain -> Icons.Outlined.Functions
        "tech" in domain -> Icons.Outlined.Memory
        else -> when (symbol) {
        "🛡️", "🔒" -> Icons.Outlined.Shield
        "🧠", "💡" -> Icons.Outlined.Lightbulb
        "🔥", "⚡", "✨" -> Icons.Outlined.AutoAwesome
        "📈", "📊", "📡" -> Icons.Outlined.Insights
        "📱", "📁", "📚" -> Icons.Outlined.Layers
        "🔔" -> Icons.Outlined.NotificationsNone
        "✅", "✓" -> Icons.Outlined.Check
        "👑", "⭐", "🏆" -> Icons.Outlined.StarOutline
        "🌙" -> Icons.Outlined.DarkMode
        "☀️" -> Icons.Outlined.LightMode
        "⏳", "⏱️", "🕒" -> Icons.Outlined.Schedule
        "✏️" -> Icons.Outlined.Edit
        "🔍", "🌐" -> Icons.Outlined.Explore
        "→" -> Icons.Outlined.ArrowForward
        else -> Icons.Outlined.Interests
        }
    }
    Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(size))
}

@Composable
fun PrimaryAction(label: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(modifier.fillMaxWidth().height(54.dp).clip(RoundedCornerShape(18.dp))
        .background(SparkCyan).softPress(onClick), contentAlignment = Alignment.Center) {
        Text(label, color = OnAccent, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
    }
}
