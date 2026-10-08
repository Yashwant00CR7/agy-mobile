package com.yashwant.agymobile.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yashwant.agymobile.ui.theme.*

data class ModelOption(
    val id: String,
    val name: String,
    val tag: String,
    val description: String
)

val AvailableModelOptions = listOf(
    ModelOption(
        id = "Gemini 3.8 Flash (Medium)",
        name = "Gemini 3.8 Flash",
        tag = "⚡ Dynamic Effort (Default)",
        description = "Recommended. Sub-second streaming with customizable reasoning."
    ),
    ModelOption(
        id = "Gemini 3.7 Flash (High)",
        name = "Gemini 3.7 Flash",
        tag = "Thinking Hybrid",
        description = "Specialized for algorithmic coding with customizable reasoning."
    ),
    ModelOption(
        id = "Gemini 3.1 Pro (High)",
        name = "Gemini 3.1 Pro (High)",
        tag = "🧠 High Thought • Fixed",
        description = "Large context reasoning engine for complex architecture planning."
    ),
    ModelOption(
        id = "Gemini 3.1 Pro (Low)",
        name = "Gemini 3.1 Pro (Low)",
        tag = "⚡ Low Latency • Fixed",
        description = "Direct Pro reasoning engine without extended thinking delay."
    ),
    ModelOption(
        id = "Claude Sonnet 4.6 (Thinking)",
        name = "Claude Sonnet 4.6",
        tag = "🧠 Extended Thought • Fixed",
        description = "Extended reasoning chain with deep analytical verification."
    )
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModelSelectorBottomSheet(
    selectedModel: String,
    onModelSelect: (String) -> Unit,
    selectedEffort: String = "low",
    onEffortSelect: (String) -> Unit = {},
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = ObsidianSurface,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = 10.dp)
                    .width(36.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(TextMuted.copy(alpha = 0.4f))
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    imageVector = AgyIcons.ModelChip,
                    contentDescription = null,
                    tint = ElectricCyan,
                    modifier = Modifier.size(22.dp)
                )
                Column {
                    Text(
                        text = "SELECT AGENT MODEL & EFFORT",
                        color = TextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "Native Antigravity LLM engine powering libagy.so",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                }
            }

            val isFixedModel = com.yashwant.agymobile.core.BootstrapManager.isFixedTierModel(selectedModel)
            val fixedTier = com.yashwant.agymobile.core.BootstrapManager.fixedTierFor(selectedModel)
            val effectiveEffort = if (isFixedModel && fixedTier != null) fixedTier else selectedEffort

            // Interactive Reasoning Effort Control Bar
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(AntigravitySurface)
                    .border(0.5.dp, AntigravityBorder, RoundedCornerShape(14.dp))
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isFixedModel) "REASONING EFFORT (FIXED TIER)" else "REASONING EFFORT",
                        color = TextMuted,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.8.sp
                    )
                    Text(
                        text = if (isFixedModel) {
                            when (effectiveEffort.lowercase()) {
                                "low" -> "⚡ Low • Locked"
                                "medium" -> "⚖️ Medium • Locked"
                                "high" -> "🧠 High • Locked"
                                else -> "🔒 Locked"
                            }
                        } else {
                            when (effectiveEffort.lowercase()) {
                                "low" -> "⚡ Instant (<1s)"
                                "medium" -> "⚖️ Balanced (~3s)"
                                "high" -> "🧠 Deep Thought"
                                else -> "⚡ Instant"
                            }
                        },
                        color = when (effectiveEffort.lowercase()) {
                            "low" -> AntigravityCyan
                            "high" -> AntigravityViolet
                            else -> AntigravityBlue
                        },
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                // 3-Segment Effort Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(AntigravitySurfaceElevated)
                        .padding(3.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val effortOptions = listOf(
                        Triple("low", "⚡ Low", if (isFixedModel) (if (effectiveEffort == "low") "Active" else "Locked") else "Instant reply"),
                        Triple("medium", "⚖️ Med", if (isFixedModel) (if (effectiveEffort == "medium") "Active" else "Locked") else "Balanced CoT"),
                        Triple("high", "🧠 High", if (isFixedModel) (if (effectiveEffort == "high") "Active" else "Locked") else "Deep CoT")
                    )

                    effortOptions.forEach { (level, title, subtitle) ->
                        val isSelected = effectiveEffort.equals(level, ignoreCase = true)
                        val isClickable = !isFixedModel
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (isSelected) {
                                        if (isFixedModel) AntigravityBlue.copy(alpha = 0.7f) else AntigravityBlue
                                    } else Color.Transparent
                                )
                                .then(
                                    if (isClickable) {
                                        Modifier.clickable { onEffortSelect(level) }
                                    } else Modifier
                                )
                                .padding(vertical = 7.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = title,
                                    color = if (isSelected) Color.White else TextMuted,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                )
                                Text(
                                    text = subtitle,
                                    color = if (isSelected) Color.White.copy(alpha = 0.85f) else TextMuted.copy(alpha = 0.6f),
                                    fontSize = 9.sp
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(2.dp))

            AvailableModelOptions.forEach { option ->
                val isSelected = option.id == selectedModel

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isSelected) ObsidianElevated else Color(0x33080C14))
                        .border(
                            1.dp,
                            if (isSelected) ElectricCyan else HairlineBorder,
                            RoundedCornerShape(12.dp)
                        )
                        .clickable {
                            onModelSelect(option.id)
                            val tier = com.yashwant.agymobile.core.BootstrapManager.fixedTierFor(option.id)
                            if (tier != null) {
                                onEffortSelect(tier)
                            }
                            onDismiss()
                        }
                        .padding(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = option.name,
                                    color = if (isSelected) ElectricCyan else TextPrimary,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (isSelected) CyanGlow else Color(0x22FFFFFF))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = option.tag,
                                        color = if (isSelected) ElectricCyan else TextMuted,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = option.description,
                                color = TextSecondary,
                                fontSize = 11.sp,
                                lineHeight = 15.sp
                            )
                        }

                        if (isSelected) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(ElectricCyan),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Selected",
                                    tint = ObsidianBlack,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
        }
    }
}
