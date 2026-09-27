package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Gamepad
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage

@Composable
fun AppIconBadge(
    iconPreset: String,
    iconUri: String? = null,
    size: Dp = 48.dp,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(size * 0.22f)

    Box(
        modifier = modifier
            .size(size)
            .clip(shape)
            .background(
                brush = Brush.linearGradient(
                    colors = when (iconPreset) {
                        "games" -> listOf(Color(0xFF8B5CF6), Color(0xFF6D28D9))
                        "browser" -> listOf(Color(0xFF38BDF8), Color(0xFF0284C7))
                        "rocket" -> listOf(Color(0xFFF97316), Color(0xFFEA580C))
                        "shopping" -> listOf(Color(0xFF10B981), Color(0xFF059669))
                        "code" -> listOf(Color(0xFF6366F1), Color(0xFF4338CA))
                        else -> listOf(Color(0xFF3B82F6), Color(0xFF1D4ED8))
                    }
                )
            )
            .border(1.dp, Color.White.copy(alpha = 0.2f), shape),
        contentAlignment = Alignment.Center
    ) {
        if (!iconUri.isNullOrEmpty()) {
            AsyncImage(
                model = iconUri,
                contentDescription = "App Icon",
                modifier = Modifier
                    .size(size)
                    .clip(shape)
            )
        } else {
            val iconVector = when (iconPreset) {
                "games" -> Icons.Default.Gamepad
                "browser" -> Icons.Default.Language
                "rocket" -> Icons.Default.RocketLaunch
                "shopping" -> Icons.Default.ShoppingBag
                "code" -> Icons.Default.Code
                else -> Icons.Default.Star
            }
            Icon(
                imageVector = iconVector,
                contentDescription = "Preset Icon",
                tint = Color.White,
                modifier = Modifier.size(size * 0.55f)
            )
        }
    }
}
