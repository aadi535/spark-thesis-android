package com.spark.interceptor

import org.koin.android.ext.android.inject
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.os.Bundle
import android.view.KeyEvent
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.spark.MainActivity
import com.spark.ui.screens.main.GlobalUserState
import com.spark.ui.screens.main.MindPalaceData
import com.spark.ui.theme.*
import kotlin.math.sin
import kotlinx.coroutines.*

data class SampleTopic(
    val emoji: String,
    val category: String,
    val title: String,
    val description: String,
    val steps: List<String>,
    val funFact: String,
    val isAI: Boolean = false
)

enum class BreathPhase {
    INHALE_RISING,
    EXHALE_FALLING,
    INTENTION_PROMPT
}

class SparkOverlayActivity : ComponentActivity() {

    private val progressSync: com.spark.data.network.ProgressSync by inject()

    private var audioManager: AudioManager? = null
    private var launchTimestamp: Long = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        audioManager = getSystemService(Context.AUDIO_SERVICE) as? AudioManager
        launchTimestamp = System.currentTimeMillis()
        hideSystemUI()
        pauseBackgroundMedia()
        renderUI()
    }

    override fun onResume() {
        super.onResume()
        hideSystemUI()
    }

    private fun hideSystemUI() {
        @Suppress("DEPRECATION")
        window.decorView.systemUiVisibility = (
            android.view.View.SYSTEM_UI_FLAG_FULLSCREEN
            or android.view.View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
            or android.view.View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
            or android.view.View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            or android.view.View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            or android.view.View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
        )
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        setIntent(intent)
        launchTimestamp = System.currentTimeMillis()
        pauseBackgroundMedia()
        renderUI()
    }

    override fun onDestroy() {
        releaseAudio()
        super.onDestroy()
    }

    private fun renderUI() {
        val ownerSession = com.spark.data.local.AccountSessionManager.generation
        val blockedPackage = intent.getStringExtra("blocked_package") ?: "com.instagram.android"
        val isLimitReached = intent.getBooleanExtra("is_limit_reached", false) || AppBlockerManager.hasReachedDailyLimit()
        val appName = AppBlockerManager.blockedApps
            .firstOrNull { it.packageName == blockedPackage }?.displayName ?: "Social Media"

        setContent {
            val owner = com.spark.data.local.AccountSessionManager.accountKey
            if (owner == null || ownerSession != com.spark.data.local.AccountSessionManager.generation) {
                LaunchedEffect(owner) { finish() }
                return@setContent
            }
            SparkTheme {
            SparkOverlayScreen(
                launchKey = launchTimestamp,
                appName = appName,
                blockedPackage = blockedPackage,
                isLimitReached = isLimitReached,
                blockingMode = AppBlockerManager.blockingMode,
                hasUsedNormalPass = AppBlockerManager.hasUsedNormalPassToday,
                dailyCount = GlobalUserState.completedTasks,
                dailyLimit = AppBlockerManager.dailyLimit,
                onLearn = learn@{ topic ->
                    if (ownerSession != com.spark.data.local.AccountSessionManager.generation ||
                        com.spark.data.local.AccountSessionManager.accountKey == null) {
                        finish()
                        return@learn
                    }
                    InterceptorTaskCache.prefetch(this@SparkOverlayActivity)
                    val sparkXp = 150
                    GlobalUserState.completeTask(sparkXp)
                    GlobalUserState.saveProgress(this@SparkOverlayActivity)
                    MindPalaceData.addSparkToMindPalace(
                        title = topic.title,
                        category = topic.category,
                        hook = topic.description.take(80),
                        xp = sparkXp,
                        fullContent = "${topic.description}\n\nSteps:\n${topic.steps.joinToString("\n- ", prefix = "- ")}\n\nFun Fact: ${topic.funFact}",
                        emoji = topic.emoji,
                        steps = topic.steps,
                        funFact = topic.funFact,
                        context = this@SparkOverlayActivity,
                        completionSource = "interception_explore"
                    )
                    MindPalaceData.saveToLocal(this@SparkOverlayActivity)
                    progressSync.saveCompletion(GlobalUserState.xp, MindPalaceData.completedHistory())

                    val intent = Intent(this, MainActivity::class.java).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                        putExtra("launch_spark_full", true)
                        putExtra("spark_title", topic.title)
                        putExtra("spark_category", topic.category)
                        putExtra("spark_emoji", topic.emoji)
                        putExtra("spark_description", topic.description)
                        putExtra("spark_steps", topic.steps.toTypedArray())
                        putExtra("spark_fun_fact", topic.funFact)
                        putExtra("spark_is_ai", topic.isAI)
                    }
                    startActivity(intent)
                    releaseAudio()
                    notifyServiceDismissed()
                    finish()
                },
                onUseNormalPass = { pkg ->
                    InterceptorTaskCache.prefetch(this@SparkOverlayActivity)
                    AppBlockerManager.grantNormalPass(pkg)
                    Toast.makeText(this, "5-Minute Focus Pass Granted for $appName!", Toast.LENGTH_SHORT).show()
                    releaseAudio()
                    notifyServiceDismissed()
                    finish()
                },
                onNavigateTab = { route ->
                    InterceptorTaskCache.prefetch(this@SparkOverlayActivity)
                    val intent = Intent(this, MainActivity::class.java).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                        putExtra("navigate_tab", route)
                    }
                    startActivity(intent)
                    releaseAudio()
                    notifyServiceDismissed()
                    finish()
                },
                onGoHome = {
                    InterceptorTaskCache.prefetch(this@SparkOverlayActivity)
                    val homeIntent = Intent(Intent.ACTION_MAIN).apply {
                        addCategory(Intent.CATEGORY_HOME)
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    startActivity(homeIntent)
                    releaseAudio()
                    notifyServiceDismissed()
                    finish()
                }
            )
            }
        }
    }

    private fun pauseBackgroundMedia() {
        try {
            audioManager?.requestAudioFocus(null, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_EXCLUSIVE)
            val pauseDown = KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_PAUSE)
            val pauseUp = KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_MEDIA_PAUSE)
            audioManager?.dispatchMediaKeyEvent(pauseDown)
            audioManager?.dispatchMediaKeyEvent(pauseUp)
        } catch (e: Exception) { e.printStackTrace() }
    }

    private fun releaseAudio() {
        try { audioManager?.abandonAudioFocus(null) } catch (e: Exception) { e.printStackTrace() }
    }

    private fun notifyServiceDismissed() {
        val intent = Intent(this, SparkInterceptorService::class.java).apply { action = "OVERLAY_DISMISSED" }
        startService(intent)
    }
}

/**
 * In-memory pre-fetching cache for instant zero-latency AI-generated quests.
 * Pre-fetches in background so when social media is launched, a brand-new AI quest is already prepared!
 */
object InterceptorTaskCache {
    private val aiClient: com.spark.data.network.SparkAIClient by lazy {
        org.koin.core.context.GlobalContext.get().get<com.spark.data.network.SparkAIClient>()
    }
    private var cachedTopic: SampleTopic? = null
    private var isPrefetching = false

    fun clear() {
        cachedTopic = null
        isPrefetching = false
    }

    fun getAndClear(): SampleTopic? {
        val topic = cachedTopic
        cachedTopic = null
        return topic
    }

    fun hasCached(): Boolean = cachedTopic != null

    fun prefetch(context: Context) {
        if (isPrefetching || cachedTopic != null) return
        val session = com.spark.data.local.AccountSessionManager.generation
        if (com.spark.data.local.AccountSessionManager.accountKey == null) return
        isPrefetching = true
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val userTopicPref = com.spark.ui.screens.prolab.ProLabPreferences.getActiveTopicPreference(context)
                val seenInterceptions = InterceptorSeenStorage.getSeenTitles(context)
                val journalTitles = com.spark.ui.screens.main.MindPalaceData.journalHistory.map { it.title }
                val seenTitles = (journalTitles + seenInterceptions).distinct()

                val dto = aiClient.generateInterceptorSpark(userTopicPref, seenTitles)
                if (session != com.spark.data.local.AccountSessionManager.generation || !dto.isAI) return@launch
                val t = dto.title?.trim() ?: ""
                val d = dto.description?.trim() ?: dto.hook?.trim() ?: ""
                if (t.isNotBlank() && d.isNotBlank()) {
                    cachedTopic = SampleTopic(
                        isAI = dto.isAI,
                        emoji = dto.icon ?: "💡",
                        category = dto.category ?: "Deep Dive",
                        title = t,
                        description = d,
                        steps = if (!dto.steps.isNullOrEmpty()) dto.steps else listOf("Explore the core mechanism", "Reflect on how this applies today"),
                        funFact = dto.funFact ?: "Every new idea builds physical neural connections in your brain."
                    )
                }
            } catch (e: Exception) {
                // If offline, leave null and dynamic catalog will be used
            } finally {
                if (session == com.spark.data.local.AccountSessionManager.generation) isPrefetching = false
            }
        }
    }
}

object InterceptorSeenStorage {
    fun getSeenTitles(context: android.content.Context): List<String> {
        val prefs = com.spark.data.local.AccountSessionManager.preferences(context, "spark_interceptions")
        val raw = prefs.getString("seen_titles", "") ?: ""
        return if (raw.isBlank()) emptyList() else raw.split("|||")
    }

    fun markSeen(context: android.content.Context, title: String) {
        val current = getSeenTitles(context).toMutableList()
        if (!current.contains(title)) {
            current.add(title)
            val prefs = com.spark.data.local.AccountSessionManager.preferences(context, "spark_interceptions")
            prefs.edit().putString("seen_titles", current.takeLast(50).joinToString("|||")).apply()
        }
    }
}

@Composable
fun SparkOverlayScreen(
    launchKey: Long,
    appName: String,
    blockedPackage: String,
    isLimitReached: Boolean,
    blockingMode: BlockingMode,
    hasUsedNormalPass: Boolean,
    dailyCount: Int,
    dailyLimit: Int,
    onLearn: (SampleTopic) -> Unit,
    onUseNormalPass: (String) -> Unit,
    onNavigateTab: (String) -> Unit,
    onGoHome: () -> Unit
) {
    var phase by remember(launchKey) { mutableStateOf(BreathPhase.INHALE_RISING) }
    val waveProgress = remember(launchKey) { Animatable(0f) }

    val infiniteTransition = rememberInfiniteTransition(label = "glowPulse")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(tween(2200, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "glowAlpha"
    )

    LaunchedEffect(launchKey) {
        if (!AppBlockerManager.isBreathingEnabled) {
            phase = BreathPhase.INTENTION_PROMPT
        } else {
            phase = BreathPhase.INHALE_RISING
            waveProgress.snapTo(0f)
            waveProgress.animateTo(1f, animationSpec = tween(5000, easing = FastOutSlowInEasing))
            phase = BreathPhase.EXHALE_FALLING
            waveProgress.animateTo(0f, animationSpec = tween(5000, easing = FastOutSlowInEasing))
            phase = BreathPhase.INTENTION_PROMPT
        }
    }

    val context = androidx.compose.ui.platform.LocalContext.current
    val userTopicPref = remember(launchKey) { com.spark.ui.screens.prolab.ProLabPreferences.getActiveTopicPreference(context) }
    val seenInterceptions = remember(launchKey) { InterceptorSeenStorage.getSeenTitles(context) }
    val journalTitles = remember(launchKey) { com.spark.ui.screens.main.MindPalaceData.journalHistory.map { it.title } }
    val seenTitles = remember(launchKey) { (journalTitles + seenInterceptions).distinct() }

    // Show cached AI content or local content immediately.
    val preloadedSpark = remember(launchKey) { InterceptorTaskCache.getAndClear() }
    val currentTopic by remember(launchKey) {
        mutableStateOf(
            preloadedSpark ?: run {
                val spark = com.spark.ui.screens.dashboard.DynamicSparkCatalog.getUniqueSparkForTopic(userTopicPref, seenTitles)
                val rawSteps = spark.content.substringAfter("\n\nSteps:\n", "").substringBefore("\n\nFun Fact:")
                val parsedSteps = if (rawSteps.isNotBlank()) {
                    rawSteps.split("\n- ").map { it.removePrefix("- ") }
                } else {
                    listOf(
                        "Search '${spark.title}' to explore further.",
                        "Think about how this concept applies to daily life.",
                        "Discuss this idea with a friend — teaching reinforces learning."
                    )
                }
                SampleTopic(
                    emoji = spark.emoji,
                    category = spark.type,
                    title = spark.title,
                    description = spark.content.substringBefore("\n\nSteps:"),
                    steps = parsedSteps,
                    funFact = spark.content.substringAfter("\n\nFun Fact: ", "Every new idea you explore builds new neural connections in your brain.")
                )
            }
        )
    }

    // Always mark the displayed topic as seen so it never repeats across days
    LaunchedEffect(currentTopic.title) {
        InterceptorSeenStorage.markSeen(context, currentTopic.title)
    }

    // Prepare one AI Spark for the next interception.
    LaunchedEffect(launchKey) {
        InterceptorTaskCache.prefetch(context.applicationContext)
    }

    // A single presentation accent keeps reading and breathing views visually consistent.
    val accentColor = SparkCyan

    Box(
        modifier = Modifier
            .fillMaxSize()
            .appBackdrop()
    ) {
        // Subtle ambient glow in background
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(accentColor.copy(alpha = glowAlpha * 0.12f), Color.Transparent),
                        center = Offset.Unspecified,
                        radius = 600f
                    )
                )
                .align(Alignment.TopCenter)
        )

        // Breath wave canvas only during breathing phase
        if (phase != BreathPhase.INTENTION_PROMPT) {
            AirflowWaveCanvas(phase = phase, progress = waveProgress.value)
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // ─── TOP PILL ───────────────────────────────────────────────
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .clip(RoundedCornerShape(50.dp))
                    .background(CardBackground)
                    .border(1.dp, GlassEdge, RoundedCornerShape(50.dp))
                    .padding(horizontal = 14.dp, vertical = 7.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(WondersGreen)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = when (phase) {
                        BreathPhase.INHALE_RISING -> "BREATHE IN"
                        BreathPhase.EXHALE_FALLING -> "BREATHE OUT"
                        BreathPhase.INTENTION_PROMPT -> "SPARK GUARD"
                    },
                    color = TextSecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 0.5.sp
                )
            }

            // ─── CENTER CONTENT ─────────────────────────────────────────
            if (phase != BreathPhase.INTENTION_PROMPT) {
                // BREATHING PHASE UI
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.weight(1f)
                ) {
                    val orbScale = if (phase == BreathPhase.INHALE_RISING) {
                        0.7f + (waveProgress.value * 0.55f)
                    } else {
                        1.25f - ((1f - waveProgress.value) * 0.55f)
                    }

                    Box(
                        modifier = Modifier
                            .size(140.dp)
                            .scale(orbScale)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    listOf(
                                        accentColor.copy(alpha = 0.25f),
                                        CardBackground
                                    )
                                )
                            )
                            .border(1.dp, accentColor.copy(alpha = glowAlpha), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        SparkOrb(Modifier.size(120.dp))
                    }

                    Spacer(modifier = Modifier.height(36.dp))

                    Text(
                        text = if (phase == BreathPhase.INHALE_RISING) "Breathe In" else "Breathe Out",
                        color = TextPrimary,
                        fontSize = 34.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = (-0.5).sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = if (phase == BreathPhase.INHALE_RISING)
                            "Slow down. You were about to scroll $appName."
                        else
                            "Release the urge. Your focus is worth protecting.",
                        color = TextSecondary,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center,
                        lineHeight = 21.sp,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            } else {
                // Full-height reading preview; long generated content scrolls independently of actions.
                Column(
                    Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState()).padding(top = 32.dp, bottom = 24.dp),
                    horizontalAlignment = Alignment.Start
                ) {
                    SparkSourceBadge(currentTopic.isAI)
                    Spacer(Modifier.height(8.dp))
                    Text("A moment before $appName", color = TextSecondary, fontSize = 12.sp)
                    Spacer(Modifier.height(12.dp))
                    Text(currentTopic.title, color = TextPrimary, fontSize = 30.sp, lineHeight = 39.sp,
                        fontWeight = FontWeight.Medium, letterSpacing = (-0.7).sp)
                    Spacer(Modifier.height(18.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        FeatureGlyph(currentTopic.category, size = 16.dp, tint = SparkCyan)
                        Spacer(Modifier.width(8.dp))
                        Text("${currentTopic.category} · 30 sec read", color = TextSecondary, fontSize = 12.sp)
                    }
                    Spacer(Modifier.height(28.dp))
                    HorizontalDivider(color = GlassEdge)
                    Spacer(Modifier.height(24.dp))
                    Text(currentTopic.description, color = TextPrimary, fontSize = 16.sp, lineHeight = 28.sp)
                    Spacer(Modifier.height(28.dp))
                    Text("Take a closer look", color = TextSecondary, fontSize = 12.sp)
                    Spacer(Modifier.height(12.dp))
                    currentTopic.steps.take(2).forEachIndexed { index, step ->
                        Row(Modifier.padding(bottom = 14.dp), verticalAlignment = Alignment.Top) {
                            Text("0${index + 1}", color = SparkCyan, fontSize = 12.sp, modifier = Modifier.padding(end = 16.dp, top = 3.dp))
                            Text(step.removePrefix("Step ${index + 1}: "), color = TextSecondary, fontSize = 14.sp, lineHeight = 23.sp)
                        }
                    }
                }
            }

            // ─── BOTTOM ACTIONS ─────────────────────────────────────────
            if (phase == BreathPhase.INTENTION_PROMPT) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // PRIMARY CTA
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .clip(RoundedCornerShape(24.dp))
                            .background(
                                Brush.horizontalGradient(
                                    listOf(accentColor, accentColor.copy(alpha = 0.75f))
                                )
                            )
                            .softPress { onLearn(currentTopic) },
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            FeatureGlyph("✨", size = 16.dp, tint = OnAccent)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Explore this idea",
                                color = OnAccent,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                letterSpacing = (-0.2).sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // SECONDARY — go home
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .clip(RoundedCornerShape(24.dp))
                            .appBackdrop()
                            .border(1.dp, CardBackground, RoundedCornerShape(24.dp))
                            .softPress { onGoHome() },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Exit & Stay Focused",
                            color = TextSecondary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // 5-min pass link
                    if (blockingMode == BlockingMode.NORMAL) {
                        if (!hasUsedNormalPass) {
                            Text(
                                text = "Continue to $appName  (5-min pass)",
                                color = TextPrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.softPress { onUseNormalPass(blockedPackage) }
                            )
                        } else {
                            Text(
                                text = "Pass already used today",
                                color = TextPrimary,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AirflowWaveCanvas(phase: BreathPhase, progress: Float) {
    val transition = rememberInfiniteTransition(label = "waveOffset")
    val waveOffset by transition.animateFloat(
        initialValue = 0f,
        targetValue = 2 * Math.PI.toFloat(),
        animationSpec = infiniteRepeatable(tween(4000, easing = LinearEasing), RepeatMode.Restart),
        label = "waveOffset"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height
        val waveY = height * (1f - progress)
        val accentC = SparkCyan
        val purpleC = MasteryPurple

        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(accentC.copy(alpha = 0.3f), purpleC.copy(alpha = 0.15f), Color.Transparent),
                startY = (waveY - 400f).coerceAtLeast(0f),
                endY = (waveY + 400f).coerceAtMost(height)
            )
        )

        val path1 = Path().apply {
            moveTo(0f, waveY)
            var x = 0f
            while (x <= width) {
                val y = waveY + sin((x / width * 3 * Math.PI) + waveOffset).toFloat() * 30f
                lineTo(x, y); x += 8f
            }
            lineTo(width, height); lineTo(0f, height); close()
        }
        drawPath(path1, brush = Brush.verticalGradient(listOf(accentC.copy(alpha = 0.28f), Color.Transparent)))

        val path2 = Path().apply {
            moveTo(0f, waveY + 20f)
            var x = 0f
            while (x <= width) {
                val y = waveY + 20f + sin((x / width * 4 * Math.PI) - waveOffset).toFloat() * 22f
                lineTo(x, y); x += 8f
            }
            lineTo(width, height); lineTo(0f, height); close()
        }
        drawPath(path2, brush = Brush.verticalGradient(listOf(purpleC.copy(alpha = 0.2f), Color.Transparent)))
    }
}
