package com.spark.ui.theme

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.outlined.PhoneAndroid
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SparkSourceBadge(isAI: Boolean?) {
    if (isAI == null) return
    Surface(color = TextSecondary.copy(alpha = 0.08f), shape = RoundedCornerShape(8.dp)) {
        Row(Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            Icon(if (isAI) Icons.Default.AutoAwesome else Icons.Outlined.PhoneAndroid,
                contentDescription = null, tint = TextSecondary, modifier = Modifier.size(12.dp))
            Text(if (isAI) "AI Generated" else "Local Fallback", color = TextSecondary, fontSize = 10.sp)
        }
    }
}
