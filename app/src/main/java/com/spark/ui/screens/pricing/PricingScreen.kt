package com.spark.ui.screens.pricing

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.spark.ui.theme.*

@Composable
fun PricingScreen() {
    val scrollState = rememberScrollState()

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
            ScreenHeading("Spark Pro", "Good things.\nMore of them.", "For the ideas you want to take further.")
            SparkOrb(Modifier.size(140.dp).reveal(80))
            Spacer(Modifier.height(24.dp))
            // Promo Code Box
            PromoCodeSection()

            Spacer(modifier = Modifier.height(16.dp))

            // Free Plan Card
            FreePlanCard()

            Spacer(modifier = Modifier.height(16.dp))

            // Spark Pro Card
            SparkProCard()

            Spacer(modifier = Modifier.height(16.dp))

            // Restore Purchase
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Check, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(12.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Already Spark Pro? Restore purchase", color = TextSecondary, fontSize = 12.sp)
            }
        }
    }
}

@Composable
fun PromoCodeSection() {
    var promoCode by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.LocalOffer, contentDescription = null, tint = SparkCyan, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Have a Coupon or Promo Code?", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .border(1.dp, GlassEdge, RoundedCornerShape(12.dp))
                .padding(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextField(
                value = promoCode,
                onValueChange = { promoCode = it },
                placeholder = { Text("e.g. SPARKPRO100", color = TextSecondary.copy(alpha = 0.5f), fontSize = 14.sp) },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                ),
                modifier = Modifier.weight(1f)
            )

            Button(
                onClick = { /* Apply promo */ },
                colors = ButtonDefaults.buttonColors(containerColor = MasteryPurple), // Purple button
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.padding(4.dp)
            ) {
                Text("Apply", color = TextPrimary, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
fun FreePlanCard() {
    Card(
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier.fillMaxWidth().border(1.dp, GlassEdge, RoundedCornerShape(24.dp))
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                Column {
                    Text("Free Plan", color = TextPrimary, fontSize = 24.sp, fontWeight = FontWeight.SemiBold)
                    Text("Forever free", color = MasteryPurple, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("$0", color = TextPrimary, fontSize = 32.sp, fontWeight = FontWeight.SemiBold)
                    Text("forever", color = TextSecondary, fontSize = 12.sp)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                "Kickstart your micro-learning adventure. Perfect for casual daily learning.",
                color = TextSecondary,
                fontSize = 14.sp,
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(24.dp))

            FreeFeatureItem("3 Sparks/day for registered users")
            FreeFeatureItem("8 topic categories")
            FreeFeatureItem("Interactive Knowledge Map")
            FreeFeatureItem("XP & streak tracking")
            FreeFeatureItem("Deep dive reading links")

            Spacer(modifier = Modifier.height(24.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .border(1.dp, GlassEdge, RoundedCornerShape(12.dp))
                    .softPress { /* Continue free */ }
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("Continue Free", color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
            }
        }
    }
}

@Composable
fun FreeFeatureItem(text: String) {
    Row(modifier = Modifier.padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier.size(20.dp).clip(CircleShape).border(1.dp, SurfaceVariant, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Check, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(12.dp))
        }
        Spacer(modifier = Modifier.width(12.dp))
        Text(text, color = TextSecondary, fontSize = 14.sp)
    }
}

@Composable
fun SparkProCard() {
    Box(modifier = Modifier.fillMaxWidth()) {
        // Gradient background for card
        Card(
            colors = CardDefaults.cardColors(containerColor = CardBackground), // Deep purple background
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier.fillMaxWidth().padding(top = 12.dp) // Offset for ribbon
        ) {
            Column(modifier = Modifier.padding(top = 24.dp, start = 20.dp, end = 20.dp, bottom = 20.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                    Column {
                        Text("Spark Pro", color = SparkCyan, fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
                        Text("per month • cancel anytime", color = MasteryPurple, fontSize = 10.sp, fontWeight = FontWeight.SemiBold) // Light purple
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                "$11.99",
                                color = TextSecondary,
                                fontSize = 12.sp,
                                textDecoration = TextDecoration.LineThrough,
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("$3.99", color = SparkCyan, fontSize = 28.sp, fontWeight = FontWeight.SemiBold)
                        }
                        Text("67% lifetime discount", color = MasteryPurple, fontSize = 11.sp)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    "Commit to unlimited memory mastery. Access state-of-the-art AI guides and personal journals.",
                    color = TextPrimary.copy(alpha = 0.8f),
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                val proFeatures = listOf(
                    "Unlimited Daily Sparks",
                    "3D Neural Knowledge Web",
                    "Adaptive AI Mentor (Q&A & Synthesis)",
                    "Mastery Syllabus (Physics, Mathematics, Chemistry, Biology, History)",
                    "Wonder Universe (Philosophy, Ancient Civilizations, Cosmos Mysteries, Discoveries)",
                    "Intellectual DNA Analytics",
                    "Instant AI Flashcards",
                    "\"Mind Palace\" Gallery",
                    "AI-Driven Topic Exploration",
                    "Exclusive Premium Themes",
                    "Full History & Analytics",
                    "Knowledge Journal Export (PDF)"
                )

                proFeatures.forEach { feature ->
                    ProFeatureItem(feature)
                }

                Spacer(modifier = Modifier.height(16.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Brush.horizontalGradient(listOf(MasteryPurple, Color(0xFFFF5A36))))
                        .softPress { /* Upgrade */ }
                        .padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        FeatureGlyph("⚡", size = 14.dp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Upgrade to Spark Pro", color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    }
                }
            }
        }

        // "MOST POPULAR" Ribbon
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth(0.8f)
                .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
                .background(Brush.horizontalGradient(listOf(Color(0xFFF97316), MasteryPurple)))
                .padding(vertical = 4.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("★", color = TextPrimary, fontSize = 8.sp)
                Spacer(modifier = Modifier.width(4.dp))
                Text("MOST POPULAR • 67% OFF", color = TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 1.sp)
            }
        }
    }
}

@Composable
fun ProFeatureItem(text: String) {
    Row(modifier = Modifier.padding(vertical = 4.dp), verticalAlignment = Alignment.Top) {
        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = SparkCyan, modifier = Modifier.size(16.dp).padding(top = 2.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(text, color = TextPrimary, fontSize = 12.sp)
    }
}
