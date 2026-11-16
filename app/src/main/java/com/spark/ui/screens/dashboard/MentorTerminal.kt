package com.spark.ui.screens.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import com.spark.ui.theme.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

data class MentorMessage(val id: String, val isUser: Boolean, val text: String)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MentorTerminal(
    taskTitle: String,
    taskCategory: String,
    modifier: Modifier = Modifier
) {
    var query by remember { mutableStateOf("") }
    var messages by remember {
        mutableStateOf(listOf(
            MentorMessage("1", false, "Greetings! 🌌 I am your Adaptive AI Mentor for \"$taskTitle\". Ask me any follow-up questions to explore, synthesize, or clarify details about this concept!")
        ))
    }
    var isDiving by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    val primaryColor = MaterialTheme.colorScheme.primary

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.05f))
            .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f), RoundedCornerShape(24.dp))
            .padding(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.Lightbulb, contentDescription = "Mentor", tint = primaryColor, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    "QUANTUM AI MENTOR",
                    color = primaryColor,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.sp,
                    letterSpacing = 0.5.sp
                )
            }
            Text("TERMINAL DIALOGUE", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
        }

        // Chat Bubbles
        Box(modifier = Modifier.heightIn(max = 200.dp).padding(bottom = 12.dp)) {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(messages) { msg ->
                    ChatBubble(msg)
                }

                if (isDiving) {
                    item {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(start = 4.dp)) {
                            CircularProgressIndicator(modifier = Modifier.size(12.dp), color = primaryColor, strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Quantum Mentor synthesizing...", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp)
                        }
                    }
                }
            }
        }

        // Input Area
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            TextField(
                value = query,
                onValueChange = { query = it },
                placeholder = { Text("Ask AI to synthesize...", fontSize = 13.sp) },
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp)
                    .clip(RoundedCornerShape(12.dp)),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(onSend = {
                    if (query.isNotBlank() && !isDiving) {
                        val userQ = query
                        messages = messages + MentorMessage(System.currentTimeMillis().toString(), true, userQ)
                        query = ""
                        isDiving = true

                        // Fake synthesis response
                        scope.launch {
                            delay(1000)
                            messages = messages + MentorMessage(System.currentTimeMillis().toString(), false, "This is a great question about $taskTitle! The core of it lies in understanding how $taskCategory fundamentally shapes this pattern.")
                            isDiving = false
                        }
                    }
                })
            )

            Spacer(modifier = Modifier.width(8.dp))

            IconButton(
                onClick = {
                    if (query.isNotBlank() && !isDiving) {
                        val userQ = query
                        messages = messages + MentorMessage(System.currentTimeMillis().toString(), true, userQ)
                        query = ""
                        isDiving = true

                        // Fake synthesis response
                        scope.launch {
                            delay(1000)
                            messages = messages + MentorMessage(System.currentTimeMillis().toString(), false, "This is a great question about $taskTitle! The core of it lies in understanding how $taskCategory fundamentally shapes this pattern.")
                            isDiving = false
                        }
                    }
                },
                modifier = Modifier
                    .size(50.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(primaryColor)
            ) {
                Icon(Icons.Filled.Send, contentDescription = "Send", tint = Color.White, modifier = Modifier.size(20.dp))
            }
        }
    }
}

@Composable
fun ChatBubble(msg: MentorMessage) {
    val isUser = msg.isUser
    val primaryColor = MaterialTheme.colorScheme.primary

    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = if (isUser) Alignment.CenterEnd else Alignment.CenterStart) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .clip(RoundedCornerShape(
                    topStart = if (isUser) 14.dp else 2.dp,
                    topEnd = if (isUser) 2.dp else 14.dp,
                    bottomStart = 14.dp,
                    bottomEnd = 14.dp
                ))
                .background(if (isUser) primaryColor.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                .border(
                    1.dp,
                    if (isUser) primaryColor.copy(alpha = 0.3f) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f),
                    RoundedCornerShape(
                        topStart = if (isUser) 14.dp else 2.dp,
                        topEnd = if (isUser) 2.dp else 14.dp,
                        bottomStart = 14.dp,
                        bottomEnd = 14.dp
                    )
                )
                .padding(12.dp)
        ) {
            Text(
                text = msg.text,
                color = if (isUser) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface,
                fontSize = 13.sp,
                lineHeight = 18.sp
            )
        }
    }
}
