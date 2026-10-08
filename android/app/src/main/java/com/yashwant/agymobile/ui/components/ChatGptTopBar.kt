package com.yashwant.agymobile.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yashwant.agymobile.ui.theme.*

@Composable
fun ChatGptTopBar(
    selectedModel: String,
    onOpenDrawer: () -> Unit,
    onOpenModelPicker: () -> Unit,
    onNewChat: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = AntigravityDarkBg,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 6.dp)
            ) {
                // Left: Hamburger Menu Button
                IconButton(
                    onClick = onOpenDrawer,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Menu,
                        contentDescription = "Open Navigation Menu",
                        tint = TextPrimary,
                        modifier = Modifier.size(22.dp)
                    )
                }

                // Center: Centered Interactive Model Dropdown Pill
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(AntigravitySurface)
                        .clickable { onOpenModelPicker() }
                        .padding(horizontal = 14.dp, vertical = 7.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = formatModelDisplayName(selectedModel),
                            color = TextPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Icon(
                            imageVector = AgyIcons.ChevronDown,
                            contentDescription = "Select Model",
                            tint = TextSecondary,
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }

                // Right: New Chat Icon Button
                IconButton(
                    onClick = onNewChat,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = AgyIcons.NewChat,
                        contentDescription = "New Chat",
                        tint = TextPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Subtle bottom separator
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(0.5.dp)
                    .background(AntigravityHairline)
            )
        }
    }
}

private fun formatModelDisplayName(model: String): String {
    return when {
        model.contains("Flash", ignoreCase = true) -> "Gemini Flash"
        model.contains("Pro", ignoreCase = true) -> "Gemini Pro"
        model.contains("Claude", ignoreCase = true) -> "Claude 3.7 Sonnet"
        else -> model.substringBefore(" (").ifBlank { "Antigravity" }
    }
}
