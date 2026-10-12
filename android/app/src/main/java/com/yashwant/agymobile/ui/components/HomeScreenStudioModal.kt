package com.yashwant.agymobile.ui.components

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.yashwant.agymobile.core.HomeScreenShortcutManager
import com.yashwant.agymobile.core.ShortcutTarget
import com.yashwant.agymobile.ui.theme.*
import com.yashwant.agymobile.util.IconShape
import com.yashwant.agymobile.util.ShortcutDesign
import com.yashwant.agymobile.widget.HomeScreenWidgetManager
import com.yashwant.agymobile.widget.WidgetType

@Composable
fun HomeScreenStudioModal(
    activeWorkspacePath: String,
    activeWorkspaceName: String,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Shortcuts, 1: Widgets

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.90f)
                .clip(RoundedCornerShape(20.dp)),
            color = AntigravityDarkBg,
            border = androidx.compose.foundation.BorderStroke(1.dp, AntigravityBorder)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Home Screen Studio",
                            color = TextPrimary,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Customize icons, shapes & native widgets",
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = TextSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Tab Switcher
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(AntigravitySurface)
                        .padding(4.dp)
                ) {
                    TabPill(
                        label = "App Shortcuts",
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        modifier = Modifier.weight(1f)
                    )
                    TabPill(
                        label = "Native Widgets",
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Tab Content
                Box(modifier = Modifier.weight(1f)) {
                    if (selectedTab == 0) {
                        ShortcutStudioTab(
                            activeWorkspacePath = activeWorkspacePath,
                            activeWorkspaceName = activeWorkspaceName
                        )
                    } else {
                        WidgetStudioTab(
                            activeWorkspacePath = activeWorkspacePath,
                            activeWorkspaceName = activeWorkspaceName
                        )
                    }
                }
            }
        }
    }
}

data class ShortcutPreset(
    val name: String,
    val target: ShortcutTarget,
    val emoji: String,
    val defaultLabel: String
)

@Composable
private fun TabPill(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (selected) AntigravitySurfaceElevated else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = if (selected) TextPrimary else TextSecondary,
            fontSize = 13.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
        )
    }
}

@Composable
private fun ShortcutStudioTab(
    activeWorkspacePath: String,
    activeWorkspaceName: String
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    // Structured Shortcut Presets
    val presets = remember(activeWorkspacePath, activeWorkspaceName) {
        listOf(
            ShortcutPreset(
                name = "New Chat",
                target = ShortcutTarget.NewChat,
                emoji = "💬",
                defaultLabel = "AGY Chat"
            ),
            ShortcutPreset(
                name = "Active Workspace",
                target = ShortcutTarget.OpenWorkspace(activeWorkspacePath, activeWorkspaceName),
                emoji = "📁",
                defaultLabel = activeWorkspaceName.ifBlank { "Workspace" }
            ),
            ShortcutPreset(
                name = "Voice Dictation",
                target = ShortcutTarget.VoiceDictation,
                emoji = "🎤",
                defaultLabel = "AGY Voice"
            ),
            ShortcutPreset(
                name = "Code Review",
                target = ShortcutTarget.QuickPrompt("Perform code review on recent git changes"),
                emoji = "⚡",
                defaultLabel = "AGY Review"
            ),
            ShortcutPreset(
                name = "Chrome Browser",
                target = ShortcutTarget.LaunchApp("com.android.chrome", "Chrome"),
                emoji = "🌐",
                defaultLabel = "Chrome"
            )
        )
    }

    var selectedTargetIndex by remember { mutableIntStateOf(0) }
    var selectedShape by remember { mutableStateOf(IconShape.SQUIRCLE) }
    var selectedColor by remember { mutableStateOf(AntigravityViolet) }
    var labelText by remember { mutableStateOf("AGY Chat") }
    var symbolText by remember { mutableStateOf("💬") }
    var badgeText by remember { mutableStateOf("AI") }

    // When preset changes, update defaults
    LaunchedEffect(selectedTargetIndex) {
        val preset = presets[selectedTargetIndex]
        symbolText = preset.emoji
        labelText = preset.defaultLabel
    }

    val palette = listOf(
        AntigravityViolet,
        AntigravityBlue,
        AntigravityCyan,
        EmeraldSuccess,
        AmberWarning,
        CrimsonDanger
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
    ) {
        // Live Preview Mockup
        Text(
            text = "LIVE PREVIEW",
            color = TextMuted,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(AntigravitySurface)
                .padding(vertical = 18.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // The icon shape
                val shapeModifier = when (selectedShape) {
                    IconShape.CIRCLE -> Modifier.clip(CircleShape)
                    IconShape.SQUIRCLE -> Modifier.clip(RoundedCornerShape(22.dp))
                    IconShape.ROUNDED_SQUARE -> Modifier.clip(RoundedCornerShape(12.dp))
                    IconShape.TEARDROP -> Modifier.clip(
                        RoundedCornerShape(
                            topStart = 24.dp,
                            topEnd = 24.dp,
                            bottomEnd = 24.dp,
                            bottomStart = 4.dp
                        )
                    )
                }

                Box(
                    modifier = Modifier
                        .size(62.dp)
                        .then(shapeModifier)
                        .background(selectedColor),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = symbolText,
                        fontSize = 26.sp
                    )
                    if (badgeText.isNotBlank()) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(4.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color(0xFF00E5FF))
                                .padding(horizontal = 4.dp, vertical = 1.dp)
                        ) {
                            Text(
                                text = badgeText.take(4),
                                color = Color(0xFF080C14),
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = labelText.ifBlank { "Shortcut" },
                    color = TextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 1. Choose Target Action
        Text(
            text = "1. CHOOSE ACTION / TARGET",
            color = TextMuted,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(6.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            presets.take(3).forEachIndexed { index, preset ->
                FilterChip(
                    selected = selectedTargetIndex == index,
                    onClick = { selectedTargetIndex = index },
                    label = { Text(preset.name, fontSize = 11.sp) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            presets.drop(3).forEachIndexed { idx, preset ->
                val actualIndex = idx + 3
                FilterChip(
                    selected = selectedTargetIndex == actualIndex,
                    onClick = { selectedTargetIndex = actualIndex },
                    label = { Text(preset.name, fontSize = 11.sp) },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 2. Choose Icon Shape
        Text(
            text = "2. ICON SHAPE",
            color = TextMuted,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(6.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            IconShape.values().forEach { shape ->
                val isSelected = selectedShape == shape
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) AntigravitySurfaceElevated else AntigravitySurface)
                        .border(
                            1.dp,
                            if (isSelected) AntigravityBlue else Color.Transparent,
                            RoundedCornerShape(8.dp)
                        )
                        .clickable { selectedShape = shape }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = shape.name.replace("_", " ").lowercase().replaceFirstChar { it.uppercase() },
                        color = if (isSelected) TextPrimary else TextSecondary,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 3. Accent Color
        Text(
            text = "3. ACCENT COLOR",
            color = TextMuted,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            palette.forEach { color ->
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(color)
                        .border(
                            2.dp,
                            if (selectedColor == color) TextPrimary else Color.Transparent,
                            CircleShape
                        )
                        .clickable { selectedColor = color }
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 4. Label & Symbol Customizer
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Column(modifier = Modifier.weight(2f)) {
                Text(
                    text = "LABEL",
                    color = TextMuted,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                BasicTextField(
                    value = labelText,
                    onValueChange = { labelText = it },
                    textStyle = TextStyle(color = TextPrimary, fontSize = 13.sp),
                    cursorBrush = SolidColor(AntigravityBlue),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(AntigravitySurface)
                        .padding(horizontal = 10.dp, vertical = 9.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "EMOJI",
                    color = TextMuted,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                BasicTextField(
                    value = symbolText,
                    onValueChange = { symbolText = it.take(2) },
                    textStyle = TextStyle(color = TextPrimary, fontSize = 13.sp, textAlign = TextAlign.Center),
                    cursorBrush = SolidColor(AntigravityBlue),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(AntigravitySurface)
                        .padding(horizontal = 10.dp, vertical = 9.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "BADGE",
                    color = TextMuted,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                BasicTextField(
                    value = badgeText,
                    onValueChange = { badgeText = it.take(4).uppercase() },
                    textStyle = TextStyle(color = TextPrimary, fontSize = 13.sp, textAlign = TextAlign.Center),
                    cursorBrush = SolidColor(AntigravityBlue),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(AntigravitySurface)
                        .padding(horizontal = 10.dp, vertical = 9.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // CTA: Pin to Home Screen
        Button(
            onClick = {
                val target = presets[selectedTargetIndex].target
                val design = ShortcutDesign(
                    label = labelText.ifBlank { "Antigravity" },
                    shape = selectedShape,
                    backgroundColor = selectedColor.toArgb(),
                    emojiOrText = symbolText.ifBlank { "⚡" },
                    badge = badgeText.ifBlank { null }
                )
                HomeScreenShortcutManager.pinShortcut(context, target, design)
            },
            colors = ButtonDefaults.buttonColors(containerColor = AntigravityViolet),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        ) {
            Icon(
                imageVector = Icons.Default.PushPin,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Pin Shortcut to Home Screen",
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun WidgetStudioTab(
    activeWorkspacePath: String,
    activeWorkspaceName: String
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    var selectedWidgetType by remember { mutableStateOf(WidgetType.PROMPT_PILL) }
    var selectedAccent by remember { mutableStateOf(AntigravityViolet) }
    var isExpandedShape by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
    ) {
        // 1. Widget Type Picker
        Text(
            text = "1. CHOOSE WIDGET",
            color = TextMuted,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(6.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            WidgetType.values().forEach { widget ->
                val isSelected = selectedWidgetType == widget
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isSelected) AntigravitySurfaceElevated else AntigravitySurface)
                        .border(
                            1.dp,
                            if (isSelected) AntigravityViolet else Color.Transparent,
                            RoundedCornerShape(10.dp)
                        )
                        .clickable { selectedWidgetType = widget }
                        .padding(horizontal = 6.dp, vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = widget.title,
                            color = if (isSelected) TextPrimary else TextSecondary,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = widget.standardDimensions,
                            color = TextMuted,
                            fontSize = 10.sp
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 2. Widget Reshape & Size Toggle
        Text(
            text = "2. RESHAPE & PROPORTIONS",
            color = TextMuted,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(6.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = !isExpandedShape,
                onClick = { isExpandedShape = false },
                label = {
                    Text(
                        selectedWidgetType.compactProportion,
                        fontSize = 12.sp
                    )
                },
                modifier = Modifier.weight(1f)
            )

            FilterChip(
                selected = isExpandedShape,
                onClick = { isExpandedShape = true },
                label = {
                    Text(
                        selectedWidgetType.expandedProportion,
                        fontSize = 12.sp
                    )
                },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 3. Live Interactive Widget Preview
        Text(
            text = "LIVE WIDGET PREVIEW",
            color = TextMuted,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(AntigravitySurface)
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            when (selectedWidgetType) {
                WidgetType.PROMPT_PILL -> {
                    // Prompt Pill Compose Preview
                    Surface(
                        color = Color(0xFF1E1F22),
                        shape = RoundedCornerShape(24.dp),
                        border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFF2B2D31)),
                        modifier = Modifier
                            .fillMaxWidth(if (isExpandedShape) 0.85f else 1f)
                            .height(48.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 14.dp)
                        ) {
                            Text("✨", fontSize = 16.sp)
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Ask Antigravity...",
                                color = Color(0xFFA0A0A5),
                                fontSize = 14.sp,
                                modifier = Modifier.weight(1f)
                            )
                            Text("🎤", fontSize = 16.sp)
                        }
                    }
                }
                WidgetType.ACTIONS_GRID -> {
                    // Actions Grid Compose Preview
                    Surface(
                        color = Color(0xFF1E1F22),
                        shape = RoundedCornerShape(16.dp),
                        border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFF2B2D31)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("✨", fontSize = 12.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "QUICK ACTIONS",
                                    color = Color(0xFF9E9E9E),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                listOf(
                                    "💬" to "New Chat",
                                    "📁" to "Folder",
                                    "🎤" to "Voice",
                                    "⚡" to "Prompt"
                                ).forEach { (icon, name) ->
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(Color(0xFF2B2D31))
                                            .padding(vertical = 8.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text(icon, fontSize = 15.sp)
                                            Spacer(modifier = Modifier.height(3.dp))
                                            Text(name, color = Color(0xFFE0E0E0), fontSize = 10.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                WidgetType.STATUS_CARD -> {
                    // Status Card Compose Preview
                    Surface(
                        color = Color(0xFF1E1F22),
                        shape = RoundedCornerShape(16.dp),
                        border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFF2B2D31)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("✨", fontSize = 12.sp)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "WORKSPACE & STATUS",
                                        color = Color(0xFF9E9E9E),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(7.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFF4CAF50))
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "READY",
                                        color = Color(0xFF4CAF50),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("📁", fontSize = 15.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = activeWorkspaceName.ifBlank { "Download" },
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Text(
                                text = activeWorkspacePath.ifBlank { "/storage/emulated/0/Download" },
                                color = Color(0xFF7E8087),
                                fontSize = 11.sp,
                                modifier = Modifier.padding(start = 22.dp)
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Daemon Ready • ARM64",
                                    color = Color(0xFF9E9E9E),
                                    fontSize = 11.sp
                                )
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0xFF2B2D31))
                                        .padding(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = "OPEN",
                                        color = Color(0xFF7C4DFF),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // CTA: Pin Widget to Home Screen
        Button(
            onClick = {
                HomeScreenWidgetManager.pinWidget(context, selectedWidgetType)
            },
            colors = ButtonDefaults.buttonColors(containerColor = AntigravityBlue),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Widgets,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Pin ${selectedWidgetType.title} to Home Screen",
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
