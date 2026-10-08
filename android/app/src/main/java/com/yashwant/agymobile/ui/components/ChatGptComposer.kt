package com.yashwant.agymobile.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yashwant.agymobile.ui.theme.*

@Composable
fun ChatGptComposer(
    promptText: String,
    onPromptChange: (String) -> Unit,
    isRunning: Boolean,
    onSend: () -> Unit,
    onCancel: () -> Unit,
    onVoiceClick: () -> Unit,
    isVoiceRecording: Boolean,
    onQuickAction: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    var showQuickMenu by remember { mutableStateOf(false) }

    val quickActions = listOf(
        QuickActionItem("git status", "Check git working tree & status"),
        QuickActionItem("review current diffs", "Run Standards and Spec code review"),
        QuickActionItem("run project tests", "Execute automated test suite"),
        QuickActionItem("explain architecture", "Overview of codebase architecture")
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 6.dp)
    ) {
        // Floating Capsule Composer
        Surface(
            color = AntigravitySurface,
            shape = RoundedCornerShape(26.dp),
            border = androidx.compose.foundation.BorderStroke(0.5.dp, AntigravityBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                verticalAlignment = Alignment.Bottom,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 6.dp, vertical = 5.dp)
            ) {
                // Left: '+' Options Button
                Box {
                    IconButton(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            showQuickMenu = !showQuickMenu
                        },
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(AntigravitySurfaceElevated)
                    ) {
                        Icon(
                            imageVector = AgyIcons.Plus,
                            contentDescription = "Options & Tools",
                            tint = TextPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // Dropdown Menu for Quick Commands
                    DropdownMenu(
                        expanded = showQuickMenu,
                        onDismissRequest = { showQuickMenu = false },
                        modifier = Modifier
                            .background(AntigravitySurfaceElevated)
                            .widthIn(min = 220.dp)
                    ) {
                        Text(
                            text = "QUICK ACTIONS",
                            color = TextMuted,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                        )
                        quickActions.forEach { action ->
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text(action.label, color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                        Text(action.subtitle, color = TextSecondary, fontSize = 11.sp)
                                    }
                                },
                                onClick = {
                                    showQuickMenu = false
                                    onQuickAction(action.label)
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Center: Clean Auto-expanding Input Field
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    if (promptText.isEmpty()) {
                        Text(
                            text = if (isVoiceRecording) "Listening..." else "Message Antigravity...",
                            color = if (isVoiceRecording) CrimsonDanger else TextMuted,
                            fontSize = 15.sp
                        )
                    }

                    BasicTextField(
                        value = promptText,
                        onValueChange = onPromptChange,
                        textStyle = TextStyle(
                            color = TextPrimary,
                            fontSize = 15.sp,
                            lineHeight = 21.sp
                        ),
                        cursorBrush = SolidColor(AntigravityBlue),
                        maxLines = 5,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                // Right: Dynamic Circular Action Button (Mic / Send / Stop)
                when {
                    isRunning -> {
                        // Stop button (Square stop)
                        IconButton(
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                onCancel()
                            },
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(CrimsonDanger)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Stop,
                                contentDescription = "Stop",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    promptText.isNotBlank() -> {
                        // Signature Send button: Solid circle with Arrow Up
                        IconButton(
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                onSend()
                            },
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(SendButtonActive)
                        ) {
                            Icon(
                                imageVector = AgyIcons.ArrowUp,
                                contentDescription = "Send",
                                tint = SendButtonText,
                                modifier = Modifier.size(19.dp)
                            )
                        }
                    }

                    else -> {
                        // Voice / Dictation Mic button
                        IconButton(
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                onVoiceClick()
                            },
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(if (isVoiceRecording) CrimsonDanger else AntigravitySurfaceElevated)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Mic,
                                contentDescription = "Voice Dictation",
                                tint = if (isVoiceRecording) Color.White else TextPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

private data class QuickActionItem(
    val label: String,
    val subtitle: String
)
