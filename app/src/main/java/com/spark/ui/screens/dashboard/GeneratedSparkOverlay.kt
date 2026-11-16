package com.spark.ui.screens.dashboard

import android.content.Intent
import android.net.Uri

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.spark.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun GeneratedSparkInlineCard(
    spark: GeneratedSpark,
    onMaximize: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Brush.horizontalGradient(listOf(SparkCyan, GradientPurple)), RoundedCornerShape(24.dp))
            .softPress { onMaximize() }
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            FeatureGlyph(spark.emoji, size = 24.dp)
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                SparkSourceBadge(spark.isAI)
                Text(spark.type.uppercase(), color = SparkCyan, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                Text(spark.title, color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(SparkCyan)
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text("Expand", color = OnAccent, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
fun GeneratedSparkFullScreen(
    spark: GeneratedSpark,
    chatMessages: List<ChatMessage>,
    readingTimer: Int = 0,
    onSendMessage: (String) -> Unit,
    onMinimize: () -> Unit,
    onComplete: () -> Unit = {}
) {
    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val context = androidx.compose.ui.platform.LocalContext.current

    var showProChatModal by remember { mutableStateOf(false) }
    val isProUser = com.spark.ui.screens.prolab.ProLabPreferences.isProUser(context)

    // Pulsing glow animation
    val infiniteTransition = rememberInfiniteTransition(label = "fullscreenGlow")
    val heroGlow by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.85f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "heroGlow"
    )

    // Extract full narrative description (preserve all paragraphs, remove Steps & Fun Fact footers)
    val descriptionText = spark.content
        .substringBefore("\n\nSteps:")
        .substringBefore("\n\nFun Fact:")
        .trim()

    val funFactText = if (spark.content.contains("Fun Fact:", ignoreCase = true)) {
        spark.content.substringAfter("Fun Fact:", "").substringBefore("\n\n").trim()
    } else {
        "Learning new concepts creates physical neural connections in your brain!"
    }

    // Scroll to bottom when new chat message arrives
    LaunchedEffect(chatMessages.size) {
        if (chatMessages.isNotEmpty()) {
            listState.animateScrollToItem(listState.layoutInfo.totalItemsCount - 1)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .appBackdrop()
            .safeDrawingPadding()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // TOP BAR
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(modifier = Modifier.weight(1f).padding(end = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    listOf(GradientPurple.copy(alpha = 0.3f), GradientCyan.copy(alpha = 0.3f))
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        FeatureGlyph(spark.emoji, size = 20.dp)
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(Modifier.weight(1f)) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(SparkCyan.copy(alpha = 0.15f))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                "${spark.type} · Reading",
                                color = SparkCyan,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                letterSpacing = 0.5.sp
                            )
                        }
                        Text(
                            "Daily discovery",
                            color = TextSecondary,
                            fontSize = 13.sp,
                            fontFamily = SparkFont,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1
                        )
                    }
                }

                // Minimize Icon
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(SurfaceVariant.copy(alpha = 0.6f))
                        .softPress { onMinimize() }
                        .padding(8.dp)
                ) {
                    Icon(
                        Icons.Default.KeyboardArrowDown,
                        contentDescription = "Minimize",
                        tint = TextPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            // MAIN SCROLLABLE BODY (MATCHES WEB APP LAYOUT)
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                item {
                    Column(Modifier.fillMaxWidth().reveal().padding(top = 24.dp, bottom = 24.dp)) {
                        SparkSourceBadge(spark.isAI)
                        Spacer(Modifier.height(8.dp))
                        Text("${spark.type} · +150 XP", color = TextSecondary, fontSize = 11.sp,
                            fontWeight = FontWeight.Medium, letterSpacing = 1.sp)
                        Spacer(Modifier.height(16.dp))
                        Text(spark.title, color = TextPrimary, fontSize = 30.sp, lineHeight = 39.sp,
                            fontWeight = FontWeight.Medium, letterSpacing = (-0.8).sp)
                        Spacer(Modifier.height(12.dp))
                        Text("A short read. A new perspective.", color = TextSecondary, fontSize = 14.sp)
                    }
                }
                item {
                    Text(descriptionText, color = TextPrimary, fontSize = 17.sp, fontFamily = SparkFont, lineHeight = 29.sp)
                    Spacer(Modifier.height(28.dp))
                    HorizontalDivider(color = GlassEdge)
                    Spacer(Modifier.height(24.dp))
                }

                // LEARN DEEPER / WIKIPEDIA EXPLORATION
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = CardBackground),
                        shape = RoundedCornerShape(24.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, GlassEdge, RoundedCornerShape(24.dp))
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                FeatureGlyph("🌐", size = 18.dp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "LEARN DEEPER",
                                    color = SparkCyan,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    letterSpacing = 0.5.sp
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = "If you want to know or learn deeper about this topic, search '${spark.title}' on Wikipedia or Google.",
                                color = TextPrimary,
                                fontSize = 13.sp,
                                textAlign = TextAlign.Center,
                                lineHeight = 19.sp
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(SparkCyan.copy(alpha = 0.15f))
                                    .border(1.dp, SparkCyan.copy(alpha = 0.18f), RoundedCornerShape(12.dp))
                                    .clickable {
                                        try {
                                            val query = java.net.URLEncoder.encode(spark.title, "UTF-8")
                                            val wikiUrl = "https://en.wikipedia.org/wiki/Special:Search?search=$query"
                                            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(wikiUrl)).apply {
                                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                            }
                                            context.startActivity(browserIntent)
                                        } catch (e: Exception) {
                                            e.printStackTrace()
                                        }
                                    }
                                    .padding(horizontal = 16.dp, vertical = 9.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Search,
                                        contentDescription = "Search",
                                        tint = SparkCyan,
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Search on Wikipedia ↗",
                                        color = SparkCyan,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                }

                // COSMIC INSIGHT CARD (FUN FACT)
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = CardBackground),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, SparkOrange.copy(alpha = 0.35f), RoundedCornerShape(20.dp))
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(SparkOrange.copy(alpha = 0.15f))
                                    .padding(horizontal = 12.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    "COSMIC INSIGHT",
                                    color = SparkOrange,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    letterSpacing = 0.5.sp
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = funFactText,
                                color = TextPrimary,
                                fontSize = 14.sp,
                                textAlign = TextAlign.Center,
                                lineHeight = 20.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))
                }



                // ASK DEEPINSIDE QUESTIONS (AI MENTOR CHATBOT HEADER)
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(modifier = Modifier.weight(1f).height(1.dp).background(SurfaceVariant))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 12.dp)
                        ) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = "AI Mentor", tint = SparkCyan, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                "Explore with your AI mentor",
                                color = SparkCyan,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                letterSpacing = 0.5.sp
                            )
                        }
                        Box(modifier = Modifier.weight(1f).height(1.dp).background(SurfaceVariant))
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }

                // CHAT MESSAGES
                items(chatMessages) { message ->
                    ChatBubble(message)
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }

            // BOTTOM BAR WITH CHAT INPUT & CLAIM BUTTON
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .appBackdrop()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                // CHAT INPUT (LOCKED FOR FREE USERS)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(CardBackground)
                        .border(1.dp, GlassEdge, RoundedCornerShape(24.dp))
                        .softPress { if (!isProUser) showProChatModal = true }
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (!isProUser) {
                        Icon(Icons.Default.Lock, contentDescription = "Pro Lock", tint = SparkCyan, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Ask a follow-up",
                            color = TextSecondary,
                            fontSize = 13.sp,
                            modifier = Modifier.weight(1f)
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(SurfaceVariant)
                                .softPress { showProChatModal = true }
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text("Unlock AI", color = TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        }
                    } else {
                        BasicTextField(
                            value = inputText,
                            onValueChange = { inputText = it },
                            modifier = Modifier.weight(1f),
                            textStyle = TextStyle(color = TextPrimary, fontSize = 14.sp, fontFamily = SparkFont),
                            cursorBrush = SolidColor(SparkCyan),
                            decorationBox = { innerTextField ->
                                if (inputText.isEmpty()) {
                                    Text("What else do you want to know?", color = TextSecondary, fontSize = 13.sp)
                                }
                                innerTextField()
                            }
                        )

                        if (inputText.isNotBlank()) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(SparkCyan)
                                    .clickable {
                                        onSendMessage(inputText)
                                        inputText = ""
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Send, contentDescription = "Send", tint = Color.Black, modifier = Modifier.size(14.dp))
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // CLAIM XP BUTTON WITH 15-SEC READING TIMELOCK
                val isUnlocked = readingTimer == 0

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 52.dp)
                        .clip(RoundedCornerShape(26.dp))
                        .background(
                            if (isUnlocked)
                                SolidColor(TextPrimary)
                            else
                                SolidColor(SurfaceVariant.copy(alpha = 0.6f))
                        )
                        .clickable(enabled = isUnlocked) { onComplete() }
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        FeatureGlyph(if (isUnlocked) "✓" else "⏳", size = 18.dp, tint = if (isUnlocked) AppBackground else TextSecondary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isUnlocked)
                                "Complete · +150 XP"
                            else
                                "Keep reading · ${readingTimer}s",
                            color = if (isUnlocked) AppBackground else TextSecondary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }

        // PRO CHAT LOCK MODAL DIALOG
        if (showProChatModal) {
            AlertDialog(
                onDismissRequest = { showProChatModal = false },
                containerColor = CardBackground,
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        FeatureGlyph("🔒", size = 22.dp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Unlock Spark AI Mentor", color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.SemiBold, fontFamily = SparkFont)
                    }
                },
                text = {
                    Text(
                        "The interactive AI Mentor chatbot is an exclusive Pro feature. Upgrade to Pro Lab to ask follow-up questions and explore concepts deeply!",
                        color = TextSecondary,
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showProChatModal = false
                            onMinimize()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SparkCyan)
                    ) {
                        Text("⚡ Go to Pro Lab", color = OnAccent, fontWeight = FontWeight.SemiBold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showProChatModal = false }) {
                        Text("Close", color = TextSecondary)
                    }
                }
            )
        }
    }
}



@Composable
fun ChatBubble(message: ChatMessage) {
    val alignment = if (message.isUser) Alignment.CenterEnd else Alignment.CenterStart
    val horizontalAlignment = if (message.isUser) Alignment.End else Alignment.Start
    val bgColor = if (message.isUser) SparkCyan.copy(alpha = 0.2f) else CardBackground
    val textColor = if (message.isUser) SparkCyan else TextPrimary
    val shape = if (message.isUser) {
        RoundedCornerShape(18.dp, 18.dp, 4.dp, 18.dp)
    } else {
        RoundedCornerShape(18.dp, 18.dp, 18.dp, 4.dp)
    }

    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = alignment
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .wrapContentWidth(horizontalAlignment)
                .clip(shape)
                .background(bgColor)
                .border(1.dp, if (message.isUser) SparkCyan.copy(alpha = 0.18f) else SurfaceVariant, shape)
                .padding(14.dp)
        ) {
            Text(
                message.text,
                color = textColor,
                fontSize = 14.sp,
                fontFamily = SparkFont,
                lineHeight = 20.sp
            )
        }
    }
}
