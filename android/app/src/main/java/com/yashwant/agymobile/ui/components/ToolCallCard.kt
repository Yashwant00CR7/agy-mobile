package com.yashwant.agymobile.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import com.yashwant.agymobile.ui.theme.*
import org.json.JSONObject

@Composable
fun ToolCallCard(
    toolName: String,
    argsJson: String,
    output: String? = null,
    error: String? = null,
    status: String? = null,
    durationMs: Long? = null,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(false) }
    val clipboardManager = LocalClipboardManager.current
    val haptic = LocalHapticFeedback.current
    var copiedCommand by remember { mutableStateOf(false) }

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

    val effectiveStatus = when {
        status == "ERROR" || error != null -> "ERROR"
        status == "SUCCESS" || output != null -> "SUCCESS"
        status == "RUNNING" -> "RUNNING"
        else -> "RUNNING"
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
                    // Status Badge
                    Surface(
                        color = statusColor.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = effectiveStatus,
                            color = statusColor,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    // Duration (if available)
                    if (durationMs != null && durationMs > 0) {
                        Text(
                            text = if (durationMs >= 1000) "${String.format("%.1f", durationMs / 1000.0)}s" else "${durationMs}ms",
                            color = TextMuted,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace
                        )
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

        // 2. Expanded Terminal Body & Output Inspector
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
                    .background(AntigravityDarkBg)
                    .border(0.5.dp, AntigravityBorder, RoundedCornerShape(12.dp))
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Command Line Block (if present)
                if (commandLine != null) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "COMMAND",
                                color = TextMuted,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                modifier = Modifier.clickable {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    clipboardManager.setText(AnnotatedString(commandLine))
                                    copiedCommand = true
                                }
                            ) {
                                Icon(
                                    imageVector = if (copiedCommand) Icons.Default.Check else Icons.Default.ContentCopy,
                                    contentDescription = "Copy command",
                                    tint = if (copiedCommand) EmeraldSuccess else AntigravityBlue,
                                    modifier = Modifier.size(12.dp)
                                )
                                Text(
                                    text = if (copiedCommand) "Copied" else "Copy",
                                    color = if (copiedCommand) EmeraldSuccess else AntigravityBlue,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        Surface(
                            color = AntigravitySurface,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            val cmdScroll = rememberScrollState()
                            Text(
                                text = "$ $commandLine",
                                color = TextPrimary,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp,
                                lineHeight = 16.sp,
                                modifier = Modifier
                                    .horizontalScroll(cmdScroll)
                                    .padding(8.dp)
                            )
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
                            modifier = Modifier.size(13.dp)
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

                // STDOUT output section
                if (!output.isNullOrBlank()) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "STDOUT",
                                color = EmeraldSuccess,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            )
                            Text(
                                text = "Copy",
                                color = AntigravityBlue,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.clickable {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    clipboardManager.setText(AnnotatedString(output))
                                }
                            )
                        }

                        Surface(
                            color = AntigravitySurface,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            val outScroll = rememberScrollState()
                            Text(
                                text = output.trimEnd(),
                                color = AssistantText,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp,
                                lineHeight = 15.sp,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(max = 240.dp)
                                    .horizontalScroll(outScroll)
                                    .padding(8.dp)
                            )
                        }
                    }
                }

                // STDERR output section
                if (!error.isNullOrBlank()) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "STDERR",
                                color = CrimsonDanger,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            )
                            Text(
                                text = "Copy",
                                color = CrimsonDanger,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.clickable {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    clipboardManager.setText(AnnotatedString(error))
                                }
                            )
                        }

                        Surface(
                            color = CrimsonDanger.copy(alpha = 0.08f),
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(0.5.dp, CrimsonDanger.copy(alpha = 0.3f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            val errScroll = rememberScrollState()
                            Text(
                                text = error.trimEnd(),
                                color = CrimsonDanger,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp,
                                lineHeight = 15.sp,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(max = 200.dp)
                                    .horizontalScroll(errScroll)
                                    .padding(8.dp)
                            )
                        }
                    }
                }

                // Running state indicator
                if (output.isNullOrBlank() && error.isNullOrBlank() && effectiveStatus == "RUNNING") {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(vertical = 4.dp)
                    ) {
                        CircularProgressIndicator(
                            color = AmberWarning,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "Command executing on device...",
                            color = TextMuted,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                // If non-command tool without stdout/stderr, show formatted JSON arguments
                if (commandLine == null && output.isNullOrBlank() && error.isNullOrBlank()) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "ARGUMENTS",
                            color = TextMuted,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                        Surface(
                            color = AntigravitySurface,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            val argScroll = rememberScrollState()
                            Text(
                                text = formatPrettyJson(argsJson),
                                color = TextSecondary,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp,
                                lineHeight = 15.sp,
                                modifier = Modifier
                                    .horizontalScroll(argScroll)
                                    .padding(8.dp)
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
