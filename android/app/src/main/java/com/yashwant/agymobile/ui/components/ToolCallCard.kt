package com.yashwant.agymobile.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.*
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.yashwant.agymobile.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import org.json.JSONObject

@Composable
fun ToolCallCard(
    toolName: String,
    argsJson: String,
    output: String? = null,
    error: String? = null,
    status: String? = null,
    durationMs: Long? = null,
    onCancelCommand: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val clipboardManager = LocalClipboardManager.current
    val haptic = LocalHapticFeedback.current
    var copiedTerminal by remember { mutableStateOf(false) }
    var showFullscreenModal by remember { mutableStateOf(false) }

    val effectiveStatus = when {
        status == "ERROR" || error != null -> "ERROR"
        status == "SUCCESS" || output != null -> "SUCCESS"
        status == "RUNNING" -> "RUNNING"
        else -> "RUNNING"
    }

    // Auto-expand when command is executing
    var isExpanded by remember(effectiveStatus) {
        mutableStateOf(effectiveStatus == "RUNNING" || status != null)
    }

    val parsedArgs = remember(argsJson) {
        try { JSONObject(argsJson) } catch (_: Exception) { null }
    }

    val commandLine = remember(parsedArgs) {
        parsedArgs?.optString("CommandLine", "")?.ifBlank { null }
            ?: parsedArgs?.optString("command", "")?.ifBlank { null }
            ?: parsedArgs?.optString("cmd", "")?.ifBlank { null }
    }

    val cwd = remember(parsedArgs) {
        parsedArgs?.optString("Cwd", "")?.ifBlank { null }
            ?: parsedArgs?.optString("cwd", "")?.ifBlank { null }
            ?: parsedArgs?.optString("directory", "")?.ifBlank { null }
    }

    val targetFile = remember(parsedArgs) {
        parsedArgs?.optString("TargetFile", "")?.ifBlank { null }
            ?: parsedArgs?.optString("AbsolutePath", "")?.ifBlank { null }
            ?: parsedArgs?.optString("path", "")?.ifBlank { null }
    }

    val toolAction = remember(parsedArgs) {
        parsedArgs?.optString("toolAction", "")?.ifBlank { null }
            ?: parsedArgs?.optString("toolSummary", "")?.ifBlank { null }
    }

    val statusColor = when (effectiveStatus) {
        "ERROR" -> CrimsonDanger
        "SUCCESS" -> EmeraldSuccess
        else -> AmberWarning
    }

    val isCommandTool = toolName.contains("command", ignoreCase = true) ||
            toolName.contains("bash", ignoreCase = true) ||
            commandLine != null

    val headerTitle = when {
        commandLine != null -> "$ $commandLine"
        targetFile != null -> "$toolName: ${java.io.File(targetFile).name}"
        toolAction != null -> "$toolAction ($toolName)"
        else -> "Tool: $toolName"
    }

    // Live execution duration ticker
    var liveElapsedMs by remember(effectiveStatus, durationMs) {
        mutableStateOf(durationMs ?: 0L)
    }
    LaunchedEffect(effectiveStatus) {
        if (effectiveStatus == "RUNNING") {
            val startTime = System.currentTimeMillis() - liveElapsedMs
            while (isActive) {
                liveElapsedMs = System.currentTimeMillis() - startTime
                delay(100L)
            }
        }
    }

    // Pulsing indicator for active execution
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(650, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    val combinedLogText = remember(commandLine, output, error) {
        buildString {
            if (commandLine != null) {
                append("$ ").append(commandLine).append("\n\n")
            }
            if (!output.isNullOrBlank()) {
                append(output.trimEnd())
            }
            if (!error.isNullOrBlank()) {
                if (isNotEmpty()) append("\n\n[STDERR]\n")
                append(error.trimEnd())
            }
        }
    }

    val lineCount = remember(combinedLogText) {
        if (combinedLogText.isBlank()) 0 else combinedLogText.lines().size
    }

    Column(
        modifier = modifier
            .animateContentSize()
            .fillMaxWidth()
            .padding(vertical = 3.dp)
    ) {
        // 1. Sleek Interactive Tool Pill / Card Header
        Surface(
            onClick = { isExpanded = !isExpanded },
            color = AntigravitySurface,
            shape = RoundedCornerShape(12.dp),
            border = androidx.compose.foundation.BorderStroke(
                0.5.dp,
                if (isExpanded) AntigravityBlue.copy(alpha = 0.5f) else AntigravityBorder
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(if (isCommandTool) AntigravityDarkBg else AntigravitySurfaceElevated),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isCommandTool) AgyIcons.TerminalSpark else Icons.Default.Code,
                            contentDescription = null,
                            tint = if (isCommandTool) AntigravityCyan else AntigravityBlue,
                            modifier = Modifier.size(13.dp)
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = headerTitle,
                            color = TextPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            fontFamily = if (commandLine != null) FontFamily.Monospace else FontFamily.Default,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (cwd != null || toolAction != null) {
                            Text(
                                text = cwd ?: toolAction ?: "",
                                color = TextMuted,
                                fontSize = 10.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Status Badge with Pulsing indicator if running
                    Surface(
                        color = statusColor.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            if (effectiveStatus == "RUNNING") {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(AmberWarning)
                                        .alpha(pulseAlpha)
                                )
                            }
                            Text(
                                text = effectiveStatus,
                                color = statusColor,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Live Ticking Duration Timer
                    val displayTime = if (effectiveStatus == "RUNNING") liveElapsedMs else (durationMs ?: liveElapsedMs)
                    if (displayTime > 0) {
                        Text(
                            text = if (effectiveStatus == "RUNNING") {
                                "Running ${String.format("%.1f", displayTime / 1000.0)}s..."
                            } else if (displayTime >= 1000) {
                                "${String.format("%.1f", displayTime / 1000.0)}s"
                            } else {
                                "${displayTime}ms"
                            },
                            color = if (effectiveStatus == "RUNNING") AmberWarning else TextMuted,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    // Stop / Cancel Button (Visible when command is running)
                    if (effectiveStatus == "RUNNING" && onCancelCommand != null) {
                        Surface(
                            onClick = onCancelCommand,
                            color = CrimsonDanger.copy(alpha = 0.18f),
                            shape = RoundedCornerShape(6.dp),
                            border = androidx.compose.foundation.BorderStroke(0.5.dp, CrimsonDanger.copy(alpha = 0.5f))
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(2.dp),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Stop,
                                    contentDescription = "Stop command",
                                    tint = CrimsonDanger,
                                    modifier = Modifier.size(10.dp)
                                )
                                Text(
                                    text = "Stop",
                                    color = CrimsonDanger,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Icon(
                        imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = if (isExpanded) "Collapse" else "Expand",
                        tint = TextSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        // 2. Expanded Terminal Body & Real-Time Streaming HUD
        AnimatedVisibility(
            visible = isExpanded,
            enter = expandVertically(),
            exit = shrinkVertically()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF0D1117))
                    .border(0.5.dp, AntigravityBorder, RoundedCornerShape(12.dp))
                    .padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Command Line Block (if present)
                if (commandLine != null) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            // Terminal window fake dots
                            Box(modifier = Modifier.size(7.dp).clip(CircleShape).background(Color(0xFFFF5F56)))
                            Box(modifier = Modifier.size(7.dp).clip(CircleShape).background(Color(0xFFFFBD2E)))
                            Box(modifier = Modifier.size(7.dp).clip(CircleShape).background(Color(0xFF27C93F)))
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = "BIONIC TERMINAL HUD",
                                color = TextMuted,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (lineCount > 0) {
                                Surface(
                                    color = AntigravitySurface,
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = "$lineCount lines",
                                        color = TextMuted,
                                        fontSize = 9.sp,
                                        fontFamily = FontFamily.Monospace,
                                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            // Fullscreen Expand Action
                            Icon(
                                imageVector = Icons.Default.Fullscreen,
                                contentDescription = "Expand Fullscreen",
                                tint = TextSecondary,
                                modifier = Modifier
                                    .size(15.dp)
                                    .clickable { showFullscreenModal = true }
                            )

                            // Copy Action
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(3.dp),
                                modifier = Modifier.clickable {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    clipboardManager.setText(AnnotatedString(combinedLogText))
                                    copiedTerminal = true
                                }
                            ) {
                                Icon(
                                    imageVector = if (copiedTerminal) Icons.Default.Check else Icons.Default.ContentCopy,
                                    contentDescription = "Copy log",
                                    tint = if (copiedTerminal) EmeraldSuccess else AntigravityBlue,
                                    modifier = Modifier.size(12.dp)
                                )
                                Text(
                                    text = if (copiedTerminal) "Copied" else "Copy",
                                    color = if (copiedTerminal) EmeraldSuccess else AntigravityBlue,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }

                // Working Directory (if present)
                if (cwd != null) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Folder,
                            contentDescription = null,
                            tint = TextMuted,
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = "cwd: $cwd",
                            color = TextSecondary,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // 3. Streaming Bounded Viewport (~220dp max-height with auto-scroll)
                val terminalScroll = rememberScrollState()
                val terminalHorizScroll = rememberScrollState()

                // Auto-scroll to bottom as output streams in
                LaunchedEffect(output?.length, error?.length) {
                    if (output != null || error != null) {
                        terminalScroll.animateScrollTo(terminalScroll.maxValue)
                    }
                }

                Surface(
                    color = Color(0xFF08090C),
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(0.5.dp, Color(0xFF1E2228)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 60.dp, max = 220.dp)
                            .verticalScroll(terminalScroll)
                            .horizontalScroll(terminalHorizScroll)
                            .padding(8.dp)
                    ) {
                        if (commandLine != null) {
                            Text(
                                text = "$ $commandLine",
                                color = AntigravityCyan,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                lineHeight = 16.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                        }

                        if (!output.isNullOrBlank()) {
                            Text(
                                text = output.trimEnd(),
                                color = Color(0xFFE6EDF3),
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp,
                                lineHeight = 15.sp
                            )
                        }

                        if (!error.isNullOrBlank()) {
                            if (!output.isNullOrBlank()) {
                                Spacer(modifier = Modifier.height(4.dp))
                            }
                            Text(
                                text = error.trimEnd(),
                                color = CrimsonDanger,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp,
                                lineHeight = 15.sp
                            )
                        }

                        // Running pulse indicator inside viewport
                        if (effectiveStatus == "RUNNING") {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.padding(top = 6.dp)
                            ) {
                                CircularProgressIndicator(
                                    color = AmberWarning,
                                    strokeWidth = 1.5.dp,
                                    modifier = Modifier.size(10.dp)
                                )
                                Text(
                                    text = if (output.isNullOrBlank() && error.isNullOrBlank()) {
                                        "Executing in Android Bionic environment..."
                                    } else {
                                        "Streaming process output..."
                                    },
                                    color = AmberWarning.copy(alpha = 0.85f),
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }

                        // If not command tool and empty output, show arguments JSON
                        if (commandLine == null && output.isNullOrBlank() && error.isNullOrBlank()) {
                            Text(
                                text = formatPrettyJson(argsJson),
                                color = TextSecondary,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp,
                                lineHeight = 15.sp
                            )
                        }
                    }
                }
            }
        }
    }

    // 4. Fullscreen Terminal Viewport Modal
    if (showFullscreenModal) {
        Dialog(
            onDismissRequest = { showFullscreenModal = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Surface(
                color = Color(0xFF090A0D),
                modifier = Modifier
                    .fillMaxSize()
                    .systemBarsPadding()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(14.dp)
                ) {
                    // Modal Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(AntigravitySurface),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = AgyIcons.TerminalSpark,
                                    contentDescription = null,
                                    tint = AntigravityCyan,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = headerTitle,
                                    color = TextPrimary,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "${lineCount} lines • ${effectiveStatus}",
                                    color = statusColor,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            IconButton(
                                onClick = {
                                    clipboardManager.setText(AnnotatedString(combinedLogText))
                                }
                            ) {
                                Icon(Icons.Default.ContentCopy, contentDescription = "Copy all", tint = AntigravityBlue, modifier = Modifier.size(18.dp))
                            }
                            IconButton(onClick = { showFullscreenModal = false }) {
                                Icon(Icons.Default.Close, contentDescription = "Close", tint = TextSecondary, modifier = Modifier.size(20.dp))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Fullscreen Scrollable Body
                    val modalVertScroll = rememberScrollState()
                    val modalHorizScroll = rememberScrollState()

                    Surface(
                        color = Color(0xFF040507),
                        shape = RoundedCornerShape(10.dp),
                        border = androidx.compose.foundation.BorderStroke(0.5.dp, Color(0xFF1E2228)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(modalVertScroll)
                                .horizontalScroll(modalHorizScroll)
                                .padding(12.dp)
                        ) {
                            Text(
                                text = combinedLogText.ifBlank { "No output captured for this command." },
                                color = Color(0xFFE6EDF3),
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun formatPrettyJson(raw: String): String {
    return try {
        val obj = JSONObject(raw)
        obj.toString(2)
    } catch (_: Exception) {
        raw
    }
}
