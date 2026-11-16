package com.spark.ui.screens.dashboard

import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.spark.interceptor.AppBlockerManager
import com.spark.interceptor.SparkInterceptorService
import com.spark.interceptor.SparkOverlayActivity
import com.spark.ui.screens.main.GlobalUserState
import com.spark.ui.screens.profile.EditProfileModal
import com.spark.ui.screens.profile.RankTiers
import com.spark.ui.screens.main.MindPalaceData
import com.spark.ui.screens.prolab.ProLabPreferences
import com.spark.ui.theme.*
import com.spark.ui.theme.ThemeManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewDashboardScreen(
    onNavigateToProLab: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {},
    launchSparkFull: Boolean = false,
    sparkTitle: String = "",
    sparkCategory: String = "",
    sparkEmoji: String = "🧠",
    sparkDescription: String = "",
    sparkSteps: Array<String> = emptyArray(),
    sparkFunFact: String = "",
    sparkIsAI: Boolean? = null,
    onSparkConsumed: () -> Unit = {},
    generateTopicFromProLab: String? = null,
    onGenerateTopicConsumed: () -> Unit = {},
    viewModel: DashboardViewModel = koinViewModel(),
    modifier: Modifier = Modifier
) {
    val progressSync: com.spark.data.network.ProgressSync = org.koin.compose.koinInject()
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()

    val dashboardState by viewModel.state.collectAsState()
    var showGuardConfigSheet by remember { mutableStateOf(false) }
    var showEditProfileModal by remember { mutableStateOf(false) }

    // Load saved user state and mind palace data on init
    LaunchedEffect(Unit) {
        val serviceIntent = Intent(context, SparkInterceptorService::class.java)
        context.startForegroundService(serviceIntent)
    }

    // Run timer countdown in GlobalSparkState scope so it persists across tab switches & minimizes
    LaunchedEffect(GlobalSparkState.activeFullSpark) {
        if (GlobalSparkState.activeFullSpark != null && GlobalSparkState.readingTimer > 0) {
            while (GlobalSparkState.readingTimer > 0) {
                delay(1000L)
                GlobalSparkState.readingTimer--
            }
        }
    }

    // Handle Pro Lab topic generation trigger
    LaunchedEffect(generateTopicFromProLab) {
        if (!generateTopicFromProLab.isNullOrBlank()) {
            val topic = generateTopicFromProLab
            onGenerateTopicConsumed()
            ProLabPreferences.setActiveTopicPreference(context, topic)
            viewModel.generateSpark("curious", "30 minutes", topic)
        }
    }

    // Listen to ViewModel AI generation state
    LaunchedEffect(dashboardState) {
        when (val state = dashboardState) {
            is DashboardState.Generated -> {
                val task = state.spark
                val formattedSteps = task.steps?.joinToString("\n- ", prefix = "- ")
                    ?: "- Explore the core concept\n- Reflect on practical applications\n- Share what you learned"

                val newSpark = GeneratedSpark(
                    title = task.title ?: "AI Generated Spark",
                    content = "${task.description ?: ""}\n\nSteps:\n$formattedSteps\n\nFun Fact: ${task.funFact ?: "Learning creates physical neural connections!"}",
                    type = task.category ?: "Deep Dive",
                    isAI = task.isAI,
                    emoji = task.icon ?: "🧠"
                )
                GlobalSparkState.openSpark(newSpark)
                viewModel.resetState()
            }
            is DashboardState.Error -> {
                val activeTopic = ProLabPreferences.getActiveTopicPreference(context) ?: generateTopicFromProLab ?: "Deep Curiosity"
                val seenTitles = MindPalaceData.journalHistory.map { it.title }
                val fallbackSpark = DynamicSparkCatalog.getUniqueSparkForTopic(activeTopic, seenTitles)
                GlobalSparkState.openSpark(fallbackSpark)
                viewModel.resetState()
            }
            else -> {}
        }
    }

    // Handle intent payload from SparkOverlayActivity when "Learn This" is clicked
    LaunchedEffect(launchSparkFull, sparkTitle) {
        if (launchSparkFull && sparkTitle.isNotBlank()) {
            val formattedSteps = if (sparkSteps.isNotEmpty()) {
                sparkSteps.joinToString("\n- ", prefix = "- ")
            } else {
                "- Explore the core concept\n- Reflect on practical applications\n- Share what you learned"
            }

            val newSpark = GeneratedSpark(
                title = sparkTitle,
                content = "${sparkDescription}\n\nSteps:\n${formattedSteps}\n\nFun Fact: ${if (sparkFunFact.isNotBlank()) sparkFunFact else "Learning new concepts creates physical neural pathways in your brain!"}",
                type = sparkCategory.ifBlank { "Deep Dive" },
                isAI = sparkIsAI,
                emoji = sparkEmoji.ifBlank { "🧠" }
            )
            GlobalSparkState.openSpark(newSpark)
            onSparkConsumed()
        }
    }

    val spark = GlobalSparkState.activeFullSpark
    if (GlobalSparkState.isFullSparkExpanded && spark != null) {
        BackHandler { GlobalSparkState.minimize() }
        GeneratedSparkFullScreen(
            spark = spark,
            chatMessages = GlobalSparkState.chatMessages,
            readingTimer = GlobalSparkState.readingTimer,
            onSendMessage = { text ->
                GlobalSparkState.chatMessages.add(
                    ChatMessage(
                        id = java.util.UUID.randomUUID().toString(),
                        text = text,
                        isUser = true
                    )
                )
                coroutineScope.launch {
                    val answer = viewModel.sendWormholeMessage(
                        question = text,
                        sparkTitle = spark.title,
                        sparkDescription = spark.content,
                        history = GlobalSparkState.chatMessages.toList()
                    )
                    GlobalSparkState.chatMessages.add(
                        ChatMessage(
                            id = java.util.UUID.randomUUID().toString(),
                            text = answer,
                            isUser = false
                        )
                    )
                }
            },
            onMinimize = {
                GlobalSparkState.minimize()
            },
            onComplete = {
                val sparkXp = 150
                if (!MindPalaceData.journalHistory.any { it.title.equals(spark.title, ignoreCase = true) }) {
                    GlobalUserState.completeTask(sparkXp)
                    GlobalUserState.saveProgress(context)

                    MindPalaceData.addSparkToMindPalace(
                        title = spark.title,
                        category = spark.type,
                        hook = spark.content.split("\n").firstOrNull() ?: "",
                        xp = sparkXp,
                        fullContent = spark.content,
                        emoji = spark.emoji,
                        context = context
                    )
                    MindPalaceData.saveToLocal(context)
                    progressSync.saveCompletion(GlobalUserState.xp, MindPalaceData.completedHistory())
                }

                GlobalSparkState.completeAndClear()
            }
        )
        return
    }

    Box(modifier = modifier.fillMaxSize().appBackdrop()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .verticalScroll(scrollState)
                .padding(horizontal = 24.dp)
                .padding(top = 10.dp, bottom = 100.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // ─── 1. Cyber-Minimalist Profile & Level Hero ───
            CyberDashboardProfileHero(
                userName = GlobalUserState.userName,
                userAvatar = GlobalUserState.userAvatar,
                streak = GlobalUserState.streak,
                xp = GlobalUserState.xp,
                activeNeurons = maxOf(GlobalUserState.activeNeurons, MindPalaceData.neuralNodes.size),
                onEditProfileClick = { showEditProfileModal = true },
                onViewProfileClick = onNavigateToProfile
            )

            Spacer(modifier = Modifier.height(20.dp))

            // ─── 3. Majestic Diamond / Illuminati Spark Guard Centerpiece ───
            DiamondSparkGuardHero(
                isGuardActive = AppBlockerManager.isGuardEnabled,
                monitoredAppsCount = AppBlockerManager.blockedApps.count { it.isEnabled },
                onDiamondClick = {
                    showGuardConfigSheet = true
                }
            )

            Spacer(modifier = Modifier.height(20.dp))

            // ─── 2. Cyber Focus Objective HUD ───
            CyberFocusObjectiveHUD(
                completedSparks = GlobalUserState.completedTasks,
                targetSparks = 3
            )
            Spacer(modifier = Modifier.height(20.dp))
            KnowledgeCheckCard()

            // AI Generating Status Banner
            if (dashboardState is DashboardState.Generating) {
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    colors = CardDefaults.cardColors(containerColor = CardBackground),
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, SparkCyan.copy(alpha = 0.18f), RoundedCornerShape(24.dp))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = SparkCyan,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(14.dp))
                        Column {
                            Text(
                                "Creating your spark…",
                                color = SparkCyan,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                letterSpacing = 0.5.sp
                            )
                            Text(
                                "A new idea is on its way.",
                                color = TextSecondary,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }

            // Minimized Active Spark Banner (if any)
            AnimatedVisibility(
                visible = !GlobalSparkState.isFullSparkExpanded && GlobalSparkState.activeFullSpark != null,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                GlobalSparkState.activeFullSpark?.let { spark ->
                    Column {
                        Spacer(modifier = Modifier.height(16.dp))
                        Card(
                            colors = CardDefaults.cardColors(containerColor = CardBackground),
                            shape = RoundedCornerShape(24.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(
                                    1.dp,
                                    Brush.horizontalGradient(listOf(SparkCyan, GradientPurple)),
                                    RoundedCornerShape(24.dp)
                                )
                                .softPress { GlobalSparkState.resume() }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                FeatureGlyph(spark.emoji, size = 22.dp)
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        "IN-PROGRESS SPARK",
                                        color = SparkCyan,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        letterSpacing = 1.sp
                                    )
                                    Text(
                                        spark.title,
                                        color = TextPrimary,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(SparkCyan)
                                        .padding(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Text("Resume", color = OnAccent, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            DashboardProgressCard(
                xp = GlobalUserState.xp, streak = GlobalUserState.streak,
                activeNeurons = maxOf(GlobalUserState.activeNeurons, MindPalaceData.neuralNodes.size),
                onViewProfileClick = onNavigateToProfile)
            Spacer(Modifier.height(20.dp))
            // ─── 4. Cyber Trigger: Live Interception Simulation ───
            CyberSimulationTrigger(
                onTriggerSimulation = {
                    val testIntent = Intent(context, SparkOverlayActivity::class.java).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        putExtra("blocked_package", "com.instagram.android")
                        putExtra("is_limit_reached", false)
                    }
                    context.startActivity(testIntent)
                }
            )

            Spacer(modifier = Modifier.height(20.dp))

            // ─── 5. Cyber-Minimalist Philosophical Whisper ───
            Text(
                text = "TURN PASSIVE SCROLLING INTO INTENTIONAL CURIOSITY",
                color = TextSecondary.copy(alpha = 0.5f),
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 0.5.sp,
                textAlign = TextAlign.Center
            )
        }

                // ─── Edit Profile & Avatar Modal ───
        if (showEditProfileModal) {
            EditProfileModal(
                currentName = GlobalUserState.userName,
                currentAvatar = GlobalUserState.userAvatar,
                onDismiss = { showEditProfileModal = false },
                onSave = { newName, newAvatar ->
                    GlobalUserState.updateProfile(context, newName, newAvatar)
                    showEditProfileModal = false
                }
            )
        }

        // ─── Guard & Apps Configuration Bottom Sheet Modal (Clean Redesigned) ───
        if (showGuardConfigSheet) {
            androidx.compose.ui.window.Dialog(
                onDismissRequest = { showGuardConfigSheet = false },
                properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)
            ) {
                GuardConfigurationSheetContent(
                    onClose = { showGuardConfigSheet = false }
                )
            }
        }

    }
}

// ═══════════════════════════════════════════════════════════
// 1. CYBER-MINIMALIST PROFILE & LEVEL HERO
// ═══════════════════════════════════════════════════════════
@Composable
private fun CyberDashboardProfileHero(
    userName: String,
    userAvatar: String,
    streak: Int,
    xp: Int,
    activeNeurons: Int,
    onEditProfileClick: () -> Unit,
    onViewProfileClick: () -> Unit
) {
    Row(Modifier.fillMaxWidth().reveal().padding(top = 14.dp, bottom = 12.dp),
        verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f).softPress(onViewProfileClick)) {
            Text("A little better, every day", color = TextSecondary, fontSize = 11.sp)
            Spacer(Modifier.height(6.dp))
            Text("Hi, ${userName.ifBlank { "Explorer" }.substringBefore(" ")}", color = TextPrimary,
                fontFamily = SparkFont, fontSize = 27.sp, fontWeight = FontWeight.Medium, letterSpacing = (-0.9).sp)
        }
        Box(Modifier.size(44.dp).premiumSurface(22).softPress(onEditProfileClick), contentAlignment = Alignment.Center) {
            Text(userAvatar, fontSize = 22.sp)
        }
    }
}

@Composable
private fun DashboardProgressCard(xp: Int, streak: Int, activeNeurons: Int, onViewProfileClick: () -> Unit) {
    Column(Modifier.fillMaxWidth().reveal(150)) {
        Row(Modifier.fillMaxWidth().padding(bottom = 14.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Your momentum", color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Medium)
            Text("View profile ↗", color = TextSecondary, fontSize = 11.sp, modifier = Modifier.softPress(onViewProfileClick))
        }
        Row(Modifier.fillMaxWidth().softPress(onViewProfileClick), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            MomentumTile("$streak", "Day streak", Modifier.weight(1f))
            MomentumTile("$xp", "Total XP", Modifier.weight(1f))
            MomentumTile("$activeNeurons", "Connections", Modifier.weight(1f))
        }
    }
}

@Composable
private fun MomentumTile(value: String, label: String, modifier: Modifier) {
    Column(modifier.padding(horizontal = 12.dp, vertical = 14.dp)) {
        Text(value, color = TextPrimary, fontSize = 26.sp, fontFamily = SparkFont, fontWeight = FontWeight.Medium)
        Spacer(Modifier.height(8.dp))
        Text(label, color = TextSecondary, fontSize = 10.sp)
    }
}

// ═══════════════════════════════════════════════════════════
// 2. CYBER FOCUS OBJECTIVE HUD
// ═══════════════════════════════════════════════════════════
@Composable
private fun CyberFocusObjectiveHUD(
    completedSparks: Int,
    targetSparks: Int = 3
) {
    val progress by animateFloatAsState((completedSparks.toFloat() / targetSparks).coerceIn(0f, 1f),
        tween(850, easing = FastOutSlowInEasing), label = "dailyProgress")
    Row(Modifier.fillMaxWidth().reveal(80).padding(horizontal = 8.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(48.dp), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxSize(),
                color = SparkCyan, trackColor = SurfaceVariant, strokeWidth = 4.dp)
            Icon(Icons.Outlined.Check, null, tint = SparkCyan, modifier = Modifier.size(20.dp))
        }
        Spacer(Modifier.width(16.dp))
        Column(Modifier.weight(1f)) {
            Text("Today’s intention", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(5.dp))
            Text("$completedSparks of $targetSparks sparks completed", color = TextSecondary, fontSize = 12.sp)
        }
    }
}

// ═══════════════════════════════════════════════════════════
// 3. MAJESTIC DIAMOND / ILLUMINATI SPARK GUARD HERO
// ═══════════════════════════════════════════════════════════
@Composable
private fun DiamondSparkGuardHero(
    isGuardActive: Boolean,
    monitoredAppsCount: Int,
    onDiamondClick: () -> Unit
) {
    Column(Modifier.fillMaxWidth().reveal(50).premiumSurface(28).softPress(onDiamondClick).padding(22.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("Focus guard", color = TextSecondary, fontSize = 12.sp)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(5.dp).clip(CircleShape).background(if (isGuardActive) SparkCyan else TextMuted))
                Spacer(Modifier.width(6.dp))
                Text(if (isGuardActive) "On" else "Paused", color = TextSecondary, fontSize = 11.sp)
            }
        }
        Row(Modifier.fillMaxWidth().padding(vertical = 16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("Make space\nfor curiosity.", color = TextPrimary, fontFamily = SparkFont,
                    fontSize = 26.sp, lineHeight = 34.sp, fontWeight = FontWeight.Normal, letterSpacing = (-0.9).sp)
                Spacer(Modifier.height(12.dp))
                Text(if (isGuardActive) "$monitoredAppsCount apps. Fewer distractions." else "A quieter moment starts here.",
                    color = TextSecondary, fontSize = 11.sp, lineHeight = 18.sp)
            }
            SparkOrb(Modifier.size(112.dp), active = isGuardActive)
        }
        Row(Modifier.fillMaxWidth().height(46.dp).clip(CircleShape)
            .background(Brush.verticalGradient(listOf(Color(0xFFF5F2ED), Color(0xFFBEBEB8))))
            .padding(horizontal = 18.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("Manage focus", color = Color(0xFF20211E), fontSize = 12.sp, fontWeight = FontWeight.Medium)
            Icon(Icons.Outlined.ArrowForward, null, tint = Color(0xFF20211E), modifier = Modifier.size(18.dp))
        }
    }
}

// ═══════════════════════════════════════════════════════════
// 4. CYBER TRIGGER: LIVE INTERCEPTION SIMULATION
// ═══════════════════════════════════════════════════════════
@Composable
private fun CyberSimulationTrigger(
    onTriggerSimulation: () -> Unit
) {
    Row(Modifier.fillMaxWidth().premiumSurface(28).softPress(onTriggerSimulation).padding(16.dp),
        verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(34.dp).clip(CircleShape).background(SurfaceVariant), contentAlignment = Alignment.Center) {
            Icon(Icons.Outlined.PlayArrow, null, tint = TextPrimary, modifier = Modifier.size(19.dp))
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text("A moment of discovery", color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Medium)
            Spacer(Modifier.height(3.dp))
            Text("Preview your next mindful pause", color = TextSecondary, fontSize = 10.sp)
        }
        Icon(Icons.Outlined.ChevronRight, null, tint = TextSecondary, modifier = Modifier.size(18.dp))
    }
}

// ═══════════════════════════════════════════════════════════
// GUARD CONFIGURATION BOTTOM SHEET (ULTRA-MODERN GLASS)
// ═══════════════════════════════════════════════════════════
@Composable
private fun GuardConfigurationSheetContent(onClose: () -> Unit) {
    val context = LocalContext.current
    var selectedTopicId by remember {
        mutableStateOf(ProLabPreferences.getActiveTopicPreference(context) ?: "surprise")
    }
    val topics = remember { listOf(
        Triple("surprise", "Surprise Mix 🎲", "A little of everything"),
        Triple("science", "Science & Cosmos 🔬", "Physics and the universe"),
        Triple("psychology", "Psychology & Mind 🧠", "Understand your mind"),
        Triple("tech", "Tech & AI 🚀", "Ideas shaping tomorrow"),
        Triple("biology", "Biology & Genetics 🧬", "The science of life"),
        Triple("history", "History & Philosophy 🏛️", "Lessons across time")
    ) }
    Box(
        Modifier.fillMaxSize().safeDrawingPadding().padding(horizontal = 16.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier.widthIn(max = 560.dp).fillMaxWidth().fillMaxHeight(0.94f),
            shape = RoundedCornerShape(28.dp),
            color = CardBackground,
            border = androidx.compose.foundation.BorderStroke(1.dp, GlassEdge),
            shadowElevation = 6.dp
        ) {
            Column(Modifier.padding(horizontal = 20.dp)) {
                Row(Modifier.fillMaxWidth().padding(top = 16.dp, bottom = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("Manage Focus", color = TextPrimary, fontFamily = SparkFont, fontSize = 24.sp, fontWeight = FontWeight.Medium)
                        Spacer(Modifier.height(4.dp))
                        Text("Choose your apps and focus preferences.", color = TextSecondary, fontSize = 12.sp, lineHeight = 18.sp)
                    }
                    IconButton(onClick = onClose) { Icon(Icons.Default.Close, "Close focus", tint = TextPrimary) }
                }
                Column(Modifier.weight(1f).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(20.dp)) {
                    Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(AppBackground).padding(horizontal = 14.dp)) {
                        FocusToggleRow("Protect my attention", "Turn distractions into moments of discovery", AppBlockerManager.isGuardEnabled) {
                            AppBlockerManager.isGuardEnabled = it
                        }
                    }
                    Column {
                        Text("YOUR APPS", color = TextSecondary, fontSize = 10.sp, letterSpacing = 1.sp)
                        Spacer(Modifier.height(10.dp))
                        Column(Modifier.clip(RoundedCornerShape(18.dp)).background(AppBackground).padding(horizontal = 14.dp)) {
                            AppBlockerManager.blockedApps.forEachIndexed { index, app ->
                                if (index > 0) HorizontalDivider(color = GlassEdge)
                                FocusToggleRow(app.displayName, if (app.isEnabled && AppBlockerManager.isGuardEnabled) "Protected" else "Paused",
                                    app.isEnabled && AppBlockerManager.isGuardEnabled, enabled = AppBlockerManager.isGuardEnabled) {
                                    AppBlockerManager.setAppEnabled(index, it)
                                }
                            }
                        }
                    }
                    Column {
                        Text("MAKE ROOM FOR", color = TextSecondary, fontSize = 10.sp, letterSpacing = 1.sp)
                        Spacer(Modifier.height(10.dp))
                        Column(Modifier.clip(RoundedCornerShape(18.dp)).background(AppBackground).padding(horizontal = 14.dp)) {
                            topics.forEachIndexed { index, (id, savedTitle, description) ->
                                val selected = selectedTopicId.equals(id, true) || selectedTopicId.contains(id, true)
                                if (index > 0) HorizontalDivider(color = GlassEdge)
                                Row(Modifier.fillMaxWidth().clickable {
                                    selectedTopicId = id
                                    ProLabPreferences.setActiveTopicPreference(context, savedTitle)
                                }.heightIn(min = 64.dp).padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Column(Modifier.weight(1f).padding(end = 8.dp)) {
                                        Text(savedTitle.substringBeforeLast(" "), color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                                        Text(description, color = TextSecondary, fontSize = 11.sp)
                                    }
                                    RadioButton(selected = selected, onClick = {
                                        selectedTopicId = id
                                        ProLabPreferences.setActiveTopicPreference(context, savedTitle)
                                    }, colors = RadioButtonDefaults.colors(selectedColor = SparkCyan, unselectedColor = TextMuted))
                                }
                            }
                        }
                    }
                    Column {
                        Text("YOUR RITUAL", color = TextSecondary, fontSize = 10.sp, letterSpacing = 1.sp)
                        Spacer(Modifier.height(10.dp))
                        Column(Modifier.clip(RoundedCornerShape(18.dp)).background(AppBackground).padding(horizontal = 14.dp)) {
                            FocusToggleRow("Take a breath", "A five-second pause before you learn", AppBlockerManager.isBreathingEnabled) { AppBlockerManager.isBreathingEnabled = it }
                            HorizontalDivider(color = GlassEdge)
                            FocusToggleRow("Revisit ideas", "Bring past discoveries back into view", AppBlockerManager.isSpacedRevisionEnabled) { AppBlockerManager.isSpacedRevisionEnabled = it }
                        }
                    }
                }
                Button(onClick = onClose, modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp).height(50.dp),
                    shape = RoundedCornerShape(16.dp), colors = ButtonDefaults.buttonColors(containerColor = TextPrimary, contentColor = AppBackground)) {
                    Text("Done", fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
private fun FocusToggleRow(title: String, subtitle: String, checked: Boolean, enabled: Boolean = true, onChange: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth().clickable(enabled = enabled) { onChange(!checked) }.heightIn(min = 72.dp).padding(vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f).padding(end = 12.dp)) {
            Text(title, color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Medium)
            Spacer(Modifier.height(4.dp))
            Text(subtitle, color = TextSecondary, fontSize = 11.sp, lineHeight = 17.sp)
        }
        Switch(checked = checked, onCheckedChange = onChange, enabled = enabled,
            colors = SwitchDefaults.colors(checkedThumbColor = AppBackground, checkedTrackColor = TextPrimary,
                uncheckedThumbColor = TextSecondary, uncheckedTrackColor = SurfaceVariant, uncheckedBorderColor = Color.Transparent))
    }
}
