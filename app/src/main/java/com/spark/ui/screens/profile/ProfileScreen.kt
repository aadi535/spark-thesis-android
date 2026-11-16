package com.spark.ui.screens.profile

import android.widget.Toast
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.spark.ui.screens.main.GlobalUserState
import com.spark.ui.screens.main.MindPalaceData
import com.spark.ui.screens.prolab.ProLabPreferences
import com.spark.ui.theme.*
import com.spark.ui.theme.ThemeManager

data class RankData(
    val name: String,
    val emoji: String,
    val requiredXp: Int,
    val color: Color,
    val title: String
)

val RankTiers = listOf(
    RankData("EXPLORER", "🧭", 0, Color(0xFFFCD34D), "Novice Seeker"),
    RankData("THINKER", "🧠", 500, Color(0xFFF472B6), "Conceptualizer"),
    RankData("SCHOLAR", "🏆", 1500, Color(0xFF60A5FA), "Synthesizer"),
    RankData("LUMINARY", "🌟", 3000, SparkOrange, "Pattern Master"),
    RankData("MASTERMIND", "🚀", 5000, SparkCyan, "Cognitive Architect"),
    RankData("LEGEND", "👑", 10000, Color(0xFF34D399), "Domain Authority"),
    RankData("ORACLE", "🧿", 20000, Color(0xFF818CF8), "Intuitive Sage"),
    RankData("COSMOS", "🌌", 35000, MasteryPurple, "Universal Thinker"),
    RankData("OMEGA", "♾️", 50000, MasteryPurple, "Transcendent Mind"),
    RankData("SINGULARITY", "🕳️", 70000, TextSecondary, "Deep Synthesizer"),
    RankData("MULTIVERSE", "💠", 85000, SparkCyan, "Dimensional Nexus"),
    RankData("ENTITY", "👁️", 100000, MasteryPurple, "Cognitive Apex")
)

val AvatarChoices = listOf(
    "⚡", "🧠", "🚀", "🌟", "🧬", "🪐", "👑", "👁️", "💎", "🔥", "🦁", "🌌"
)

@Composable
fun ProfileScreen(
    onLogout: () -> Unit = {},
    onBack: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    val currentXp = GlobalUserState.xp
    val currentStreak = GlobalUserState.streak
    val userName = GlobalUserState.userName
    val userAvatar = GlobalUserState.userAvatar
    val activeNeurons = maxOf(GlobalUserState.activeNeurons, MindPalaceData.neuralNodes.size)
    val syncRatio = MindPalaceData.stats.syncRatio
    val isPro = GlobalUserState.isPro || ProLabPreferences.isProUser(context)

    // Dynamic Level (Every 500 XP = 1 Level, starting at 1)
    val currentLevel = (currentXp / 500) + 1
    val xpInLevel = currentXp % 500
    val levelProgress = (xpInLevel.toFloat() / 500f).coerceIn(0.04f, 1f)

    // Current Rank & Next Rank
    val currentRankIndex = RankTiers.indexOfLast { currentXp >= it.requiredXp }.coerceAtLeast(0)
    val currentRank = RankTiers[currentRankIndex]
    val nextRank = RankTiers.getOrNull(currentRankIndex + 1)
    val xpToNextRank = if (nextRank != null) (nextRank.requiredXp - currentXp).coerceAtLeast(0) else 0

    val rankProgress = if (nextRank != null) {
        val tierDelta = nextRank.requiredXp - currentRank.requiredXp
        val earnedInTier = currentXp - currentRank.requiredXp
        (earnedInTier.toFloat() / tierDelta.toFloat()).coerceIn(0.04f, 1f)
    } else 1f

    // Dialog state
    var showEditProfileDialog by remember { mutableStateOf(false) }

    // Tier filter state
    var selectedTierFilter by remember { mutableStateOf("ALL") }

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
                .padding(top = 12.dp, bottom = 100.dp)
        ) {
            // ─── TOP BAR ───────────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (onBack != null) {
                        IconButton(
                            onClick = onBack,
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(if (ThemeManager.isLightMode) SurfaceVariant else CardBackground)
                                .border(1.dp, SurfaceVariant, CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = SparkCyan,
                                modifier = Modifier.size(17.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                    }
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(SparkCyan)
                    )
                    Spacer(modifier = Modifier.width(7.dp))
                    Text(
                        text = "Your profile",
                        color = SparkCyan,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 0.5.sp
                    )
                }

                // Edit Profile Quick Button
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (ThemeManager.isLightMode) SurfaceVariant else CardBackground)
                        .border(1.dp, GlassEdge, RoundedCornerShape(10.dp))
                        .softPress { showEditProfileDialog = true }
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit",
                            tint = SparkCyan,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Edit",
                            color = TextPrimary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Column(Modifier.fillMaxWidth().reveal().premiumSurface(20).padding(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(44.dp).clip(CircleShape).background(SurfaceVariant)
                        .softPress { showEditProfileDialog = true }, contentAlignment = Alignment.Center) {
                        Text(userAvatar, fontSize = 23.sp)
                    }
                    Spacer(Modifier.width(16.dp))
                    Column(Modifier.weight(1f)) {
                        Text(userName.ifBlank { "Explorer" }, color = TextPrimary, fontSize = 19.sp,
                            fontWeight = FontWeight.Medium, letterSpacing = (-0.6).sp)
                        Spacer(Modifier.height(6.dp))
                        Text("Level $currentLevel · ${currentRank.name.lowercase().replaceFirstChar { it.uppercase() }}${if (isPro) " · Pro" else ""}",
                            color = TextSecondary, fontSize = 11.sp)
                    }
                }
                Spacer(Modifier.height(12.dp))
                LinearProgressIndicator(progress = { levelProgress }, color = TextPrimary, trackColor = SurfaceVariant,
                    modifier = Modifier.fillMaxWidth().height(5.dp).clip(CircleShape))
                Spacer(Modifier.height(10.dp))
                Text("$xpInLevel / 500 XP to Level ${currentLevel + 1}", color = TextSecondary, fontSize = 11.sp)
            }
            Spacer(Modifier.height(12.dp))
            ProfileLeaderboard()
            Spacer(Modifier.height(16.dp))
            // ─── 2. 4-METRIC SYNCHRONIZED HUD ─────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ProfileMetricCard(
                    title = "TOTAL XP",
                    value = "%,d".format(currentXp),
                    subtext = "Level $currentLevel",
                    emoji = "⚡",
                    accentColor = SparkCyan,
                    modifier = Modifier.weight(1f)
                )
                ProfileMetricCard(
                    title = "STREAK",
                    value = "$currentStreak Days",
                    subtext = if (currentStreak > 0) "Focus active" else "Ignite today",
                    emoji = "🔥",
                    accentColor = SparkOrange,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ProfileMetricCard(
                    title = "ACTIVE NEURONS",
                    value = "$activeNeurons",
                    subtext = "Mind Palace",
                    emoji = "🧠",
                    accentColor = WondersGreen,
                    modifier = Modifier.weight(1f)
                )
                ProfileMetricCard(
                    title = "SYNC RATIO",
                    value = "$syncRatio%",
                    subtext = MindPalaceData.stats.intellectDnaRank,
                    emoji = "📡",
                    accentColor = MasteryPurple,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // ─── 3. TIER PROGRESSION HERO ─────────────────────────────
            Text(
                text = "Your learning journey",
                color = TextSecondary,
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 0.5.sp,
                modifier = Modifier.padding(start = 2.dp, bottom = 8.dp)
            )

            Card(
                colors = CardDefaults.cardColors(containerColor = CardBackground),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, SurfaceVariant.copy(alpha = 0.8f), RoundedCornerShape(14.dp))
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(modifier = Modifier.weight(1f).padding(end = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text(currentRank.emoji, fontSize = 20.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = currentRank.name,
                                    color = TextPrimary,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = currentRank.title,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    color = SparkCyan,
                                    fontSize = 10.sp
                                )
                            }
                        }

                        if (nextRank != null) {
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "NEXT: ${nextRank.name}",
                                    color = TextSecondary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "%,d XP needed".format(xpToNextRank),
                                    color = SparkCyan,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        } else {
                            Text(
                                text = "APEX ACHIEVED",
                                color = MasteryPurple,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(5.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(if (ThemeManager.isLightMode) SurfaceVariant else CardBackground)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(rankProgress)
                                .fillMaxHeight()
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(SparkCyan, SparkCyan)
                                    )
                                )
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "${(rankProgress * 100).toInt()}% towards ${nextRank?.name ?: "Apex"}",
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                        Text(
                            text = "%,d / %,d XP".format(currentXp, nextRank?.requiredXp ?: currentRank.requiredXp),
                            color = TextSecondary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // ─── 4. ALL COGNITIVE RANKS GRID ──────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "ALL COGNITIVE RANKS",
                    color = TextSecondary,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 0.5.sp,
                    modifier = Modifier.padding(start = 2.dp)
                )

                // Filter tabs
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    listOf("ALL" to "All", "UNLOCKED" to "Unlocked", "LOCKED" to "Locked").forEach { (key, label) ->
                        val isSelected = selectedTierFilter == key
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isSelected) SparkCyan.copy(alpha = 0.2f) else Color.Transparent)
                                .border(0.8.dp, if (isSelected) SparkCyan else Color.Transparent, RoundedCornerShape(6.dp))
                                .softPress { selectedTierFilter = key }
                                .padding(horizontal = 7.dp, vertical = 2.5.dp)
                        ) {
                            Text(
                                text = label,
                                color = if (isSelected) SparkCyan else TextSecondary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Filtered Ranks
            val displayedRanks = remember(selectedTierFilter, currentXp) {
                when (selectedTierFilter) {
                    "UNLOCKED" -> RankTiers.filter { currentXp >= it.requiredXp }
                    "LOCKED" -> RankTiers.filter { currentXp < it.requiredXp }
                    else -> RankTiers
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                displayedRanks.chunked(3).forEach { rowItems ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        rowItems.forEach { rank ->
                            Box(modifier = Modifier.weight(1f)) {
                                CompactRankCard(
                                    rank = rank,
                                    currentXp = currentXp,
                                    isCurrent = rank == currentRank
                                )
                            }
                        }
                        repeat(3 - rowItems.size) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // ─── 5. JOURNEY TO ENTITY HORIZON CARD ────────────────────
            JourneyToEntityCard()

        }
    }

    // ─── EDIT PROFILE MODAL ──────────────────────────────────────────
    if (showEditProfileDialog) {
        EditProfileModal(
            currentName = userName,
            currentAvatar = userAvatar,
            onDismiss = { showEditProfileDialog = false },
            onSave = { newName, newAvatar ->
                GlobalUserState.updateProfile(context, newName, newAvatar)
                Toast.makeText(context, "Profile updated successfully!", Toast.LENGTH_SHORT).show()
                showEditProfileDialog = false
            }
        )
    }
}

@Composable
private fun ProfileLeaderboard() {
    val api: com.spark.data.network.SparkApiService = org.koin.compose.koinInject()
    val owner = com.spark.data.local.AccountSessionManager.accountKey
    val session = com.spark.data.local.AccountSessionManager.generation
    var refresh by remember(owner, session) { mutableIntStateOf(0) }
    var loading by remember(owner, session) { mutableStateOf(false) }
    var failed by remember(owner, session) {
        mutableStateOf(GlobalUserState.leaderboardRequested && GlobalUserState.leaderboard == null)
    }
    val leaderboard = GlobalUserState.leaderboard
    val guest = owner == null || owner == com.spark.data.local.AccountSessionManager.GUEST

    LaunchedEffect(owner, session, refresh) {
        if (guest) return@LaunchedEffect
        if (refresh == 0 && GlobalUserState.leaderboardRequested) return@LaunchedEffect
        GlobalUserState.leaderboardRequested = true
        loading = true
        failed = false
        try {
            val response = api.getLeaderboard()
            if (session == com.spark.data.local.AccountSessionManager.generation) GlobalUserState.leaderboard = response
        } catch (e: kotlinx.coroutines.CancellationException) {
            throw e
        } catch (e: Exception) {
            if (session == com.spark.data.local.AccountSessionManager.generation) failed = true
        } finally {
            loading = false
        }
    }

    Column(Modifier.fillMaxWidth().premiumSurface(14).padding(14.dp)) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween) {
            Text("XP Leaderboard", color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            TextButton(onClick = { refresh++ }, enabled = !guest && !loading) {
                Text("Refresh", color = if (guest || loading) TextSecondary else SparkCyan)
            }
        }
        Spacer(Modifier.height(8.dp))
        when {
            guest -> Text("Sign in to see the global leaderboard.", color = TextSecondary, fontSize = 12.sp)
            loading -> CircularProgressIndicator(Modifier.size(24.dp), color = SparkCyan)
            failed -> {
                Text("Could not load the leaderboard.", color = TextSecondary, fontSize = 12.sp)
                TextButton(onClick = { refresh++ }) { Text("Try again", color = SparkCyan) }
            }
            else -> leaderboard?.let { response ->
                if (response.entries.isEmpty()) {
                    Text("No ranked players yet.", color = TextSecondary, fontSize = 12.sp)
                }
                response.entries.forEach { LeaderboardRow(it) }
                if (response.entries.none { it.currentUser }) {
                    response.currentUser?.let {
                        HorizontalDivider(Modifier.padding(vertical = 10.dp), color = SurfaceVariant)
                        Text("Your global position", color = TextSecondary, fontSize = 11.sp)
                        LeaderboardRow(it)
                    }
                }
            }
        }
    }
}

@Composable
private fun LeaderboardRow(entry: com.spark.data.network.LeaderboardEntry) {
    Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp))
        .background(if (entry.currentUser) SparkCyan.copy(alpha = 0.12f) else Color.Transparent)
        .padding(horizontal = 8.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(entry.position?.let { "#$it" } ?: "—", color = SparkCyan, fontSize = 12.sp,
            modifier = Modifier.widthIn(min = 42.dp))
        val displayName = if (entry.currentUser) GlobalUserState.userName else entry.name
        Text(displayName + if (entry.currentUser) " (You)" else "", color = TextPrimary,
            fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
        Spacer(Modifier.width(8.dp))
        Text("%,d XP".format(entry.xp), color = TextSecondary, fontSize = 12.sp)
    }
    if (entry.currentUser && entry.position == null) {
        Text("Not ranked yet", color = TextSecondary, fontSize = 11.sp)
    }
}

@Composable
private fun ProfileMetricCard(
    title: String,
    value: String,
    subtext: String,
    emoji: String,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Column(modifier.premiumSurface(14).padding(horizontal = 12.dp, vertical = 8.dp)) {
        Text(title.lowercase().replaceFirstChar { it.uppercase() }, color = TextSecondary, fontSize = 10.sp, maxLines = 1)
        Text(value, color = TextPrimary, fontSize = 20.sp, fontWeight = FontWeight.SemiBold,
            letterSpacing = (-0.4).sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
        Text(subtext, color = TextSecondary, fontSize = 10.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
private fun CompactRankCard(
    rank: RankData,
    currentXp: Int,
    isCurrent: Boolean
) {
    val isUnlocked = currentXp >= rank.requiredXp
    val borderCol = if (isCurrent) SparkCyan else if (isUnlocked) SparkCyan.copy(alpha = 0.35f) else SurfaceVariant.copy(alpha = 0.3f)
    val bgCol = if (isCurrent) SparkCyan.copy(alpha = 0.12f) else CardBackground

    Card(
        colors = CardDefaults.cardColors(containerColor = bgCol),
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(if (isCurrent) 1.2.dp else 0.8.dp, borderCol, RoundedCornerShape(10.dp))
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 6.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Text(rank.emoji, fontSize = 18.sp, modifier = Modifier.alpha(if (isUnlocked) 1f else 0.35f))
            Column(Modifier.weight(1f)) {
                Text(
                    text = rank.name,
                    color = if (isCurrent) SparkCyan else if (isUnlocked) TextPrimary else TextSecondary,
                    fontSize = 8.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = when {
                        isCurrent -> "ACTIVE"
                        isUnlocked -> "UNLOCKED"
                        else -> "%,d XP".format(rank.requiredXp)
                    },
                    color = if (isCurrent) SparkCyan else if (isUnlocked) WondersGreen else TextSecondary,
                    fontSize = 7.5.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun EditProfileModal(
    currentName: String,
    currentAvatar: String,
    onDismiss: () -> Unit,
    onSave: (newName: String, newAvatar: String) -> Unit
) {
    var nameInput by remember { mutableStateOf(currentName) }
    var selectedAvatar by remember { mutableStateOf(currentAvatar) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CardBackground,
        shape = RoundedCornerShape(20.dp),
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    FeatureGlyph("✏️", size = 16.dp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Edit Profile & Identity",
                        color = TextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                IconButton(onClick = onDismiss, modifier = Modifier.size(26.dp)) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = TextSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                // Avatar Preview
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(68.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    listOf(SparkCyan.copy(alpha = 0.35f), if (ThemeManager.isLightMode) SurfaceVariant else CardBackground)
                                )
                            )
                            .border(2.dp, SparkCyan, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(selectedAvatar, fontSize = 34.sp)
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Avatar Selector
                Text(
                    text = "CHOOSE AVATAR GLYPH",
                    color = TextSecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 1.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AvatarChoices.forEach { glyph ->
                        val isSelected = selectedAvatar == glyph
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(if (isSelected) SparkCyan.copy(alpha = 0.2f) else if (ThemeManager.isLightMode) SurfaceVariant else CardBackground)
                                .border(
                                    1.2.dp,
                                    if (isSelected) SparkCyan else SurfaceVariant,
                                    CircleShape
                                )
                                .softPress { selectedAvatar = glyph },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(glyph, fontSize = 20.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Name Input
                Text(
                    text = "DISPLAY NAME",
                    color = TextSecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 1.sp
                )

                Spacer(modifier = Modifier.height(6.dp))

                OutlinedTextField(
                    value = nameInput,
                    onValueChange = { nameInput = it },
                    singleLine = true,
                    placeholder = { Text("Enter your name", color = TextSecondary) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SparkCyan,
                        unfocusedBorderColor = SurfaceVariant,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        cursorColor = SparkCyan
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Synchronizes across Dashboard, Mind Palace, and Settings.",
                    color = TextSecondary.copy(alpha = 0.7f),
                    fontSize = 10.5.sp
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (nameInput.trim().isNotBlank()) {
                        onSave(nameInput.trim(), selectedAvatar)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = SparkCyan),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(
                    text = "Save Changes",
                    color = AppBackground,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.sp
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextSecondary, fontSize = 12.sp)
            }
        }
    )
}

@Composable
fun JourneyToEntityCard() {
    Card(
        colors = CardDefaults.cardColors(containerColor = if (ThemeManager.isLightMode) CardBackground else CardBackground),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, SurfaceVariant.copy(alpha = 0.7f), RoundedCornerShape(14.dp))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                FeatureGlyph("👁️", size = 18.dp)
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "Journey to Entity",
                        color = TextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "The ultimate tier of human cognition (100,000 XP)",
                        color = MasteryPurple,
                        fontSize = 10.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Reach ENTITY rank in 365 days by completing ~2 AI Sparks daily.",
                color = TextSecondary,
                fontSize = 11.sp,
                lineHeight = 15.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (ThemeManager.isLightMode) SurfaceVariant else CardBackground)
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "DAILY TARGET",
                        color = SparkCyan,
                        fontSize = 8.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 0.8.sp
                    )
                    Spacer(modifier = Modifier.height(1.dp))
                    Text(
                        text = "~2 AI Sparks / Day",
                        color = TextPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Text(
                    text = "100K XP Horizon",
                    color = TextSecondary,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
