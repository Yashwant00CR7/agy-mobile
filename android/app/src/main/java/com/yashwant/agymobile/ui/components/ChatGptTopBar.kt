package com.yashwant.agymobile.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
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
    selectedEffort: String = "low",
    isAutonomous: Boolean,
    onToggleAutonomous: () -> Unit,
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
                    modifier = Modifier.size(38.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Menu,
                        contentDescription = "Open Navigation Menu",
                        tint = TextPrimary,
                        modifier = Modifier.size(22.dp)
                    )
                }

                // Center: Centered Interactive Model Dropdown Pill with Effort Badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(AntigravitySurface)
                        .clickable { onOpenModelPicker() }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = formatModelDisplayName(selectedModel),
                            color = TextPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        val (effortBadge, effortColor) = when (selectedEffort.lowercase()) {
                            "low" -> Pair("⚡ Low", AntigravityCyan)
                            "high" -> Pair("🧠 High", AntigravityViolet)
                            else -> Pair("⚖️ Med", AntigravityBlue)
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(effortColor.copy(alpha = 0.15f))
                                .padding(horizontal = 5.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = effortBadge,
                                color = effortColor,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Icon(
                            imageVector = AgyIcons.ChevronDown,
                            contentDescription = "Select Model & Effort",
                            tint = TextSecondary,
                            modifier = Modifier.size(11.dp)
                        )
                    }
                }

                // Right: Autonomous Toggle Pill + New Chat Icon
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Mode Pill
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .background(if (isAutonomous) EmeraldSuccess.copy(alpha = 0.15f) else AmberWarning.copy(alpha = 0.15f))
                            .clickable { onToggleAutonomous() }
                            .padding(horizontal = 8.dp, vertical = 5.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(if (isAutonomous) EmeraldSuccess else AmberWarning)
                            )
                            Text(
                                text = if (isAutonomous) "Auto" else "Plan",
                                color = if (isAutonomous) EmeraldSuccess else AmberWarning,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // New Chat Action
                    IconButton(
                        onClick = onNewChat,
                        modifier = Modifier.size(38.dp)
                    ) {
                        Icon(
                            imageVector = AgyIcons.NewChat,
                            contentDescription = "New Chat",
                            tint = TextPrimary,
                            modifier = Modifier.size(19.dp)
                        )
                    }
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
    val clean = model.substringBefore(" (").trim()
    return clean.ifBlank { "Gemini 2.5 Flash" }
}
