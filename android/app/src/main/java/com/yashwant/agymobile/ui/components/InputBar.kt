package com.yashwant.agymobile.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yashwant.agymobile.ui.theme.*

@Composable
fun InputBar(
    promptText: String,
    onPromptChange: (String) -> Unit,
    isRunning: Boolean,
    onSend: () -> Unit,
    onCancel: () -> Unit,
    onVoiceClick: () -> Unit,
    isVoiceRecording: Boolean,
    onChipClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val chipsScroll = rememberScrollState()

    val chips = listOf(
        "git status",
        "review current diffs",
        "run project tests",
        "Explain architecture",
        "Fix current errors"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(ObsidianSurface)
            .border(width = 0.5.dp, color = ObsidianBorder, shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Quick Chips Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(chipsScroll),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            chips.forEach { chipText ->
                SuggestionChip(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onChipClick(chipText)
                    },
                    label = { Text(chipText, fontSize = 11.sp, color = TextSecondary) },
                    colors = SuggestionChipDefaults.suggestionChipColors(containerColor = ObsidianElevated),
                    border = SuggestionChipDefaults.suggestionChipBorder(enabled = true, borderColor = HairlineBorder)
                )
            }
        }

        // Input & Actions Row
        Row(
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            // Mic Button
            IconButton(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onVoiceClick()
                },
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(if (isVoiceRecording) CrimsonDanger else ObsidianElevated)
            ) {
                Icon(
                    imageVector = Icons.Default.Mic,
                    contentDescription = "Voice Dictation",
                    tint = if (isVoiceRecording) Color.White else ElectricCyan,
                    modifier = Modifier.size(20.dp)
                )
            }

            // Text Input
            OutlinedTextField(
                value = promptText,
                onValueChange = onPromptChange,
                placeholder = { Text("Ask agy anything...", color = TextMuted, fontSize = 14.sp) },
                maxLines = 4,
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(14.dp)),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = ObsidianBlack,
                    unfocusedContainerColor = ObsidianBlack,
                    focusedBorderColor = ElectricCyan,
                    unfocusedBorderColor = ObsidianBorder,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                ),
                shape = RoundedCornerShape(14.dp)
            )

            // Send / Cancel Button
            IconButton(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    if (isRunning) onCancel() else onSend()
                },
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(if (isRunning) CrimsonDanger else ElectricCyan)
            ) {
                Icon(
                    imageVector = if (isRunning) Icons.Default.Stop else Icons.Default.Send,
                    contentDescription = if (isRunning) "Cancel" else "Send",
                    tint = ObsidianBlack,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
