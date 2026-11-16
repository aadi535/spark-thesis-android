package com.spark.ui.screens.prolab

import android.widget.Toast
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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.BookmarkAdd
import androidx.compose.material.icons.filled.BookmarkRemove
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.spark.ui.theme.*

// Feature flag: Topic selection is managed via Dashboard Spark Guard Control.
// The entire Topic Preferences system (Classics, Mastery, Wonders) is preserved in code below.
private const val SHOW_TOPIC_SELECTION_IN_PROLAB = false

@Composable
fun ProLabScreen(
    onNavigateToSettings: () -> Unit = {},
    onGenerateSparkForTopic: (String) -> Unit = {}
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    // Load persisted Pro state
    var isProUser by remember { mutableStateOf(ProLabPreferences.isProUser(context)) }

    // Master Tier Tab: 0 = Free Tier, 1 = Spark Pro
    var selectedTierTab by remember { mutableIntStateOf(if (isProUser) 1 else 0) }

    // Preserved state for topic preferences (never deleted)
    var selectedCategoryTab by remember { mutableIntStateOf(0) }
    var selectedTopicPreference by remember { mutableStateOf(ProLabPreferences.getActiveTopicPreference(context)) }
    var selectedMasterySubject by remember { mutableStateOf("Physics") }
    var selectedMasteryLevel by remember { mutableStateOf("Class 11") }
    var savedTopics by remember { mutableStateOf(ProLabPreferences.getSavedTopics(context)) }

    // Coupon code state
    var couponInput by remember { mutableStateOf("") }
    var couponError by remember { mutableStateOf<String?>(null) }
    var showUpgradeModal by remember { mutableStateOf(false) }
    var selectedFeatureTitle by remember { mutableStateOf("") }

    val infiniteTransition = rememberInfiniteTransition(label = "prolab")
    val borderGlow by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "borderGlow"
    )

    fun handleActivateCoupon(codeToTry: String) {
        val code = codeToTry.trim()
        if (code.isEmpty()) {
            couponError = "Please enter a coupon code"
            return
        }
        if (ProLabPreferences.isValidCoupon(code)) {
            ProLabPreferences.setProUnlocked(context, code)
            isProUser = true
            couponError = null
            couponInput = ""
            selectedTierTab = 1
            Toast.makeText(context, "🎉 Spark Pro Unlocked Lifetime with $code!", Toast.LENGTH_LONG).show()
        } else {
            couponError = "❌ Invalid code. Try SPARKPRO, SPARK50, or ADITYA."
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .appBackdrop()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .verticalScroll(scrollState)
                .padding(horizontal = 24.dp)
                .padding(top = 16.dp, bottom = 100.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ScreenHeading("Pro Lab", "Go a little deeper.", "More possibility. On your terms.")
            Column(Modifier.fillMaxWidth().reveal(80).premiumSurface().padding(24.dp)) {
                Text("Your membership", color = TextSecondary, fontSize = 11.sp, letterSpacing = 1.5.sp)
                Spacer(Modifier.height(10.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(if (isProUser) "Spark Pro" else "Spark Essential", color = TextPrimary,
                        fontSize = 25.sp, fontWeight = FontWeight.Medium)
                    Text(if (isProUser) "Active" else "Free", color = TextSecondary, fontSize = 12.sp)
                }
                Spacer(Modifier.height(8.dp))
                Text(if (isProUser) "A little more freedom to explore." else "Three daily sparks. A world of possibilities.",
                    color = TextSecondary, fontSize = 13.sp, lineHeight = 20.sp)
                if (isProUser) {
                    val activeCode = ProLabPreferences.getActiveCouponCode(context)
                    if (!activeCode.isNullOrBlank()) {
                        Spacer(Modifier.height(10.dp))
                        Text("Unlocked with $activeCode", color = SparkCyan, fontSize = 12.sp)
                    }
                }
            }
            Spacer(Modifier.height(24.dp))
            Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(SurfaceVariant.copy(alpha = 0.5f)).padding(5.dp)) {
                listOf("Essential", "Spark Pro").forEachIndexed { index, label ->
                    val selected = selectedTierTab == index
                    Box(Modifier.weight(1f).clip(RoundedCornerShape(14.dp))
                        .background(if (selected) CardBackground else Color.Transparent)
                        .softPress { selectedTierTab = index }.padding(vertical = 13.dp), contentAlignment = Alignment.Center) {
                        Text(label, color = if (selected) TextPrimary else TextSecondary, fontSize = 14.sp,
                            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal)
                    }
                }
            }
            Spacer(Modifier.height(24.dp))
            // ─── 3. TAB CONTENT ─────────────────────────────────────────
            if (selectedTierTab == 0) {
                // ════════════════════════════════════════════════════════
                // TAB 0: FREE TIER FEATURES
                // ════════════════════════════════════════════════════════
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    FeatureDetailCard(
                        icon = Icons.Default.Security,
                        badge = "3 / DAY",
                        badgeColor = SparkCyan,
                        title = "Spark Guard Interceptions",
                        description = "Intercepts apps like YouTube, Instagram, and TikTok with up to 3 AI-curated micro-learning pauses daily to break mindless scrolling loops."
                    )

                    FeatureDetailCard(
                        icon = Icons.Default.AutoAwesome,
                        badge = "DAILY SET",
                        badgeColor = MasteryPurple,
                        title = "Spark AI Mystery Feed",
                        description = "6 daily multidisciplinary mystery questions across Science, Psychology, History, Biology, Math, and Tech with structured explanations."
                    )

                    FeatureDetailCard(
                        icon = Icons.Default.Psychology,
                        badge = "BASIC LOG",
                        badgeColor = WondersGreen,
                        title = "Mind Palace Journal",
                        description = "Saves completed learning quests into a local chronological memory palace log with quotes, key steps, and takeaways."
                    )

                    FeatureDetailCard(
                        icon = Icons.Default.Bolt,
                        badge = "5-MIN PASS",
                        badgeColor = SparkOrange,
                        title = "Standard Focus Pass",
                        description = "Allows a temporary 5-minute pass to access intercepted apps once a micro-learning lesson is completed."
                    )

                    FeatureDetailCard(
                        icon = Icons.Default.Tune,
                        badge = "SURPRISE MIX",
                        badgeColor = Color(0xFFEC4899),
                        title = "General Domain Learning",
                        description = "Explores a diverse multidisciplinary mix of topics selected automatically across all fields of curiosity."
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Promo Unlock Card inside Free Tab
                if (!isProUser) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = CardBackground),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, SparkCyan.copy(alpha = 0.18f), RoundedCornerShape(20.dp))
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                FeatureGlyph("🎁", size = 20.dp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "HAVE A PROMO CODE?",
                                    color = SparkCyan,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    letterSpacing = 0.5.sp
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Enter your coupon code (e.g. SPARKPRO, SPARK50, or ADITYA) to unlock full unlimited Pro access instantly!",
                                color = TextSecondary,
                                fontSize = 12.sp,
                                lineHeight = 21.sp
                            )
                            Spacer(modifier = Modifier.height(14.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedTextField(
                                    value = couponInput,
                                    onValueChange = { couponInput = it; couponError = null },
                                    placeholder = { Text("Code: SPARKPRO", color = TextSecondary, fontSize = 12.sp) },
                                    singleLine = true,
                                    modifier = Modifier.weight(1f),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = SparkCyan,
                                        unfocusedBorderColor = SurfaceVariant,
                                        focusedTextColor = TextPrimary,
                                        unfocusedTextColor = TextPrimary
                                    )
                                )

                                Button(
                                    onClick = { handleActivateCoupon(couponInput) },
                                    colors = ButtonDefaults.buttonColors(containerColor = SparkCyan),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text("Activate", color = OnAccent, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                                }
                            }

                            if (couponError != null) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(couponError!!, color = Color(0xFFFF5555), fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            } else {
                // ════════════════════════════════════════════════════════
                // TAB 1: PRO TIER FEATURES
                // ════════════════════════════════════════════════════════
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    FeatureDetailCard(
                        icon = Icons.Default.Security,
                        badge = "UNLIMITED",
                        badgeColor = WondersGreen,
                        title = "Unlimited Spark Guard Interceptions",
                        description = "Zero daily caps. Protect unlimited social apps 24/7 with instant AI micro-learning pauses. Never get locked out by limit counters."
                    )

                    FeatureDetailCard(
                        icon = Icons.Default.Hub,
                        badge = "3D INTERACTIVE",
                        badgeColor = SparkCyan,
                        title = "Mind Palace 3D Neural Network",
                        description = "Explore an interactive 3D physics-based particle constellation of all learned concepts, mapping synaptic connections across your brain."
                    )

                    FeatureDetailCard(
                        icon = Icons.Default.AutoAwesome,
                        badge = "AI MENTOR",
                        badgeColor = MasteryPurple,
                        title = "Interactive AI Mentor Chatbot",
                        description = "Chat directly with Spark AI on any lesson. Ask unlimited follow-ups, request mathematical proofs, challenge concepts, and go deeper."
                    )

                    FeatureDetailCard(
                        icon = Icons.Default.Bolt,
                        badge = "ON-DEMAND",
                        badgeColor = SparkOrange,
                        title = "Infinite AI Question Generation",
                        description = "Hit Refresh in Spark AI to generate 6 brand-new mystery questions on the fly with Groq AI, and unfold deep origin stories on tap."
                    )

                    FeatureDetailCard(
                        icon = Icons.Default.Tune,
                        badge = "HIGH & EXTREME",
                        badgeColor = Color(0xFFEC4899),
                        title = "Extreme Guard Friction Modes",
                        description = "Enable mindful breathing wave animations, lockout bypass passes, and strict friction rules that break dopamine loops for good."
                    )

                    FeatureDetailCard(
                        icon = Icons.Default.WorkspacePremium,
                        badge = "VIP STATUS",
                        badgeColor = SparkOrange,
                        title = "Lifetime VIP Status & Offline Vault",
                        description = "Full access to the complete verified offline library, priority AI processing, and permanent Pro badge identity across the app."
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Promo Code Activation / Status Card inside Pro Tab
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (isProUser) CardBackground else CardBackground
                    ),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            1.dp,
                            if (isProUser) WondersGreen.copy(alpha = 0.6f) else SparkCyan.copy(alpha = 0.18f),
                            RoundedCornerShape(20.dp)
                        )
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        if (!isProUser) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                FeatureGlyph("🔑", size = 20.dp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "ACTIVATE PRO WITH COUPON CODE",
                                    color = SparkCyan,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    letterSpacing = 0.5.sp
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Enter a valid promo code below to immediately unlock all Spark Pro features for free:",
                                color = TextSecondary,
                                fontSize = 12.sp,
                                lineHeight = 21.sp
                            )
                            Spacer(modifier = Modifier.height(14.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedTextField(
                                    value = couponInput,
                                    onValueChange = { couponInput = it; couponError = null },
                                    placeholder = { Text("Code: SPARKPRO", color = TextSecondary, fontSize = 12.sp) },
                                    singleLine = true,
                                    modifier = Modifier.weight(1f),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = SparkCyan,
                                        unfocusedBorderColor = SurfaceVariant,
                                        focusedTextColor = TextPrimary,
                                        unfocusedTextColor = TextPrimary
                                    )
                                )

                                Button(
                                    onClick = { handleActivateCoupon(couponInput) },
                                    colors = ButtonDefaults.buttonColors(containerColor = SparkCyan),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text("Activate Pro", color = OnAccent, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                                }
                            }

                            if (couponError != null) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(couponError!!, color = Color(0xFFFF5555), fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                            }
                        } else {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                FeatureGlyph("👑", size = 20.dp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "PRO MEMBERSHIP ACTIVE",
                                    color = WondersGreen,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    letterSpacing = 0.5.sp
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "You have unlocked the full Spark experience. Enjoy unlimited intercepts, 3D Neural Networks, and the AI Mentor.",
                                color = TextSecondary,
                                fontSize = 12.sp,
                                lineHeight = 21.sp
                            )
                            Spacer(modifier = Modifier.height(14.dp))

                            // Switch to Free button for debug / testing
                            OutlinedButton(
                                onClick = {
                                    ProLabPreferences.revokeProAccess(context)
                                    isProUser = false
                                    selectedTierTab = 0
                                    Toast.makeText(context, "Switched to Free Tier", Toast.LENGTH_SHORT).show()
                                },
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary),
                                border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceVariant),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Switch to Free Tier (For Testing)", fontSize = 12.sp)
                            }
                        }
                    }
                }
            }

            // ═══════════════════════════════════════════════════
            // TOPIC PREFERENCES FULL SYSTEM (Classics, Mastery, Wonders)
            // Code 100% preserved intact below, hidden via feature flag
            // ═══════════════════════════════════════════════════
            if (SHOW_TOPIC_SELECTION_IN_PROLAB) {
                Spacer(modifier = Modifier.height(20.dp))
                PreservedTopicPreferencesCard(
                    isProUser = isProUser,
                    borderGlow = borderGlow,
                    selectedCategoryTab = selectedCategoryTab,
                    onSelectTab = { selectedCategoryTab = it },
                    selectedTopicPreference = selectedTopicPreference,
                    onSelectTopic = { selectedTopicPreference = it },
                    selectedMasterySubject = selectedMasterySubject,
                    onSelectSubject = { selectedMasterySubject = it },
                    selectedMasteryLevel = selectedMasteryLevel,
                    onSelectLevel = { selectedMasteryLevel = it },
                    onGenerateSpark = onGenerateSparkForTopic,
                    onShowUpgrade = { showUpgradeModal = true }
                )
            }
        }
    }

    // ═══════════════════════════════════════════════════
    // PRO UPGRADE DIALOG MODAL (Coupon Code input)
    // ═══════════════════════════════════════════════════
    if (showUpgradeModal) {
        AlertDialog(
            onDismissRequest = { showUpgradeModal = false; couponError = null },
            containerColor = CardBackground,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    FeatureGlyph("⚡", size = 22.dp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Unlock Spark Pro",
                        color = TextPrimary,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = SparkFont
                    )
                }
            },
            text = {
                Column {
                    Text(
                        text = "Enter your coupon code to unlock all Pro features instantly!",
                        color = TextSecondary,
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = couponInput,
                        onValueChange = { couponInput = it; couponError = null },
                        placeholder = { Text("Enter Promo Code (e.g. SPARKPRO)", color = TextSecondary, fontSize = 12.sp) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = SparkCyan,
                            unfocusedBorderColor = SurfaceVariant,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )

                    if (couponError != null) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(couponError!!, color = Color(0xFFFF5555), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val code = couponInput.trim()
                        if (code.isEmpty()) {
                            couponError = "Please enter a promo code"
                        } else if (ProLabPreferences.isValidCoupon(code)) {
                            ProLabPreferences.setProUnlocked(context, code)
                            isProUser = true
                            couponError = null
                            couponInput = ""
                            selectedTierTab = 1
                            showUpgradeModal = false
                            Toast.makeText(context, "🎉 Pro Access Granted with $code!", Toast.LENGTH_LONG).show()
                        } else {
                            couponError = "❌ Invalid code. Try SPARKPRO or SPARK50."
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SparkCyan)
                ) {
                    Text("Activate Pro", color = OnAccent, fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showUpgradeModal = false; couponError = null }) {
                    Text("Cancel", color = TextSecondary)
                }
            }
        )
    }
}

// ─── STYLISH FEATURE DETAIL CARD ────────────────────────────────
@Composable
private fun FeatureDetailCard(
    icon: ImageVector,
    badge: String,
    badgeColor: Color,
    title: String,
    description: String
) {
    Column(Modifier.fillMaxWidth().premiumSurface(24).padding(22.dp)) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(42.dp).clip(RoundedCornerShape(13.dp)).background(SurfaceVariant.copy(alpha = 0.65f)),
                contentAlignment = Alignment.Center) { Icon(icon, null, tint = TextPrimary, modifier = Modifier.size(20.dp)) }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(title, color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Medium, lineHeight = 22.sp)
                Spacer(Modifier.height(4.dp))
                Text(badge, color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Medium)
            }
        }
        Spacer(Modifier.height(14.dp))
        Text(description, color = TextSecondary, fontSize = 13.sp, lineHeight = 21.sp)
    }
}

// ─── PRESERVED TOPIC PREFERENCES CARD (100% PRESERVED CODE) ────
@Composable
private fun PreservedTopicPreferencesCard(
    isProUser: Boolean,
    borderGlow: Float,
    selectedCategoryTab: Int,
    onSelectTab: (Int) -> Unit,
    selectedTopicPreference: String?,
    onSelectTopic: (String?) -> Unit,
    selectedMasterySubject: String,
    onSelectSubject: (String) -> Unit,
    selectedMasteryLevel: String,
    onSelectLevel: (String) -> Unit,
    onGenerateSpark: (String) -> Unit,
    onShowUpgrade: () -> Unit
) {
    val context = LocalContext.current
    Card(
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.5.dp,
                brush = Brush.linearGradient(listOf(SparkCyan.copy(alpha = borderGlow), GradientPurple.copy(alpha = borderGlow))),
                shape = RoundedCornerShape(24.dp)
            )
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text("⭐ TOPIC PREFERENCES", color = SparkCyan, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                TopicTabPill("🌟 Classics", selectedCategoryTab == 0, { onSelectTab(0) }, Modifier.weight(1f))
                TopicTabPill("⚛️ Mastery", selectedCategoryTab == 1, { onSelectTab(1) }, Modifier.weight(1f))
                TopicTabPill("✨ Wonders", selectedCategoryTab == 2, { onSelectTab(2) }, Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun CategorySubSection(
    title: String,
    items: List<String>,
    categoryPrefix: String,
    selectedTopic: String?,
    isProUser: Boolean,
    isSaved: (String) -> Boolean = { false },
    onSelect: (String) -> Unit,
    onSave: (String) -> Unit = {},
    onGenerate: (String) -> Unit = {}
) {
    Column {
        Text(title, color = SparkCyan, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(6.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items.forEach { item ->
                val fullTopic = categoryPrefix + item
                val isSelected = selectedTopic == fullTopic
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isSelected) SparkCyan.copy(alpha = 0.25f) else SurfaceVariant.copy(alpha = 0.4f))
                        .border(1.dp, if (isSelected) SparkCyan else SurfaceVariant, RoundedCornerShape(10.dp))
                        .softPress { onSelect(fullTopic) }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(item, color = if (isSelected) SparkCyan else TextPrimary, fontSize = 10.sp, fontWeight = FontWeight.Medium, textAlign = TextAlign.Center)
                }
            }
        }
    }
}

@Composable
private fun TopicTabPill(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) SparkCyan.copy(alpha = 0.25f) else SurfaceVariant.copy(alpha = 0.4f))
            .border(1.dp, if (isSelected) SparkCyan else Color.Transparent, RoundedCornerShape(12.dp))
            .softPress { onClick() }
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(title, color = if (isSelected) SparkCyan else TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun FlowTopicGroup(
    topics: List<String>,
    selectedTopic: String?,
    isProUser: Boolean,
    isSaved: (String) -> Boolean,
    onSelect: (String) -> Unit,
    onSave: (String) -> Unit,
    onGenerate: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        topics.chunked(2).forEach { rowTopics ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                rowTopics.forEach { topic ->
                    val isSelected = selectedTopic == topic
                    val saved = isSaved(topic)
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) SparkCyan.copy(alpha = 0.25f) else SurfaceVariant.copy(alpha = 0.5f))
                            .border(1.dp, if (isSelected) SparkCyan else Color.Transparent, RoundedCornerShape(12.dp))
                            .softPress { onSelect(topic) }
                            .padding(horizontal = 12.dp, vertical = 10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(topic, color = if (isSelected) SparkCyan else TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
                            if (saved) {
                                FeatureGlyph("📌", size = 11.dp)
                            }
                        }
                    }
                }
            }
        }
    }
}
