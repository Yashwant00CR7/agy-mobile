package com.yashwant.agymobile.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.core.*
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yashwant.agymobile.model.AuthState
import com.yashwant.agymobile.model.ChatMessage
import com.yashwant.agymobile.model.MessageRole
import com.yashwant.agymobile.ui.components.*
import com.yashwant.agymobile.ui.theme.*
import com.yashwant.agymobile.util.MarkdownBlock
import com.yashwant.agymobile.util.MarkdownLinkParser

@Composable
fun ChatScreen(
    messages: List<ChatMessage>,
    promptText: String,
    onPromptChange: (String) -> Unit,
    isRunning: Boolean,
    isAutonomous: Boolean,
    onToggleAutonomous: () -> Unit,
    workspaceName: String,
    gitBranch: String?,
    onOpenMenu: () -> Unit,
    onSend: () -> Unit,
    onCancel: () -> Unit,
    onApprove: (String) -> Unit,
    onDeny: (String) -> Unit,
    onVoiceClick: () -> Unit,
    isVoiceRecording: Boolean,
    authState: AuthState = AuthState(isAuthenticated = true),
    onOpenAuth: () -> Unit = {},
    selectedModel: String = "Gemini 2.5 Flash",
    onSelectModel: (String) -> Unit = {},
    selectedEffort: String = "low",
    onSelectEffort: (String) -> Unit = {},
    onClearChat: () -> Unit = {},
    hasStoragePermission: Boolean = true,
    onRequestStoragePermission: () -> Unit = {},
    onOpenFolderPicker: () -> Unit = {},
    onOpenLink: (String) -> Unit = {},
    onOpenHomeScreenStudio: () -> Unit = {},
    onFixWithAi: (String) -> Unit = {},
    compilationStatus: String? = null,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()
    var showModelPicker by remember { mutableStateOf(false) }
    var showStorageBanner by remember { mutableStateOf(!hasStoragePermission) }
    val clipboardManager = LocalClipboardManager.current
    val haptic = LocalHapticFeedback.current

    val density = LocalDensity.current
    val imeBottom = WindowInsets.ime.getBottom(density)

    // Auto-scroll on keyboard popup
    LaunchedEffect(imeBottom) {
        if (imeBottom > 0 && messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    // Auto-scroll on new messages
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    // Auto-scroll during active stream generation
    val lastContentLength = messages.lastOrNull()?.content?.length ?: 0
    LaunchedEffect(lastContentLength) {
        if (messages.isNotEmpty() && isRunning) {
            listState.scrollToItem(messages.size - 1)
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = AntigravityDarkBg,
        topBar = {
            ChatGptTopBar(
                selectedModel = selectedModel,
                selectedEffort = selectedEffort,
                isAutonomous = isAutonomous,
                onToggleAutonomous = onToggleAutonomous,
                onOpenDrawer = onOpenMenu,
                onOpenModelPicker = { showModelPicker = true },
                onNewChat = onClearChat,
                workspaceName = workspaceName,
                onOpenFolderPicker = onOpenFolderPicker
            )
        },
        bottomBar = {
            ChatGptComposer(
                promptText = promptText,
                onPromptChange = onPromptChange,
                isRunning = isRunning,
                onSend = onSend,
                onCancel = onCancel,
                onVoiceClick = onVoiceClick,
                isVoiceRecording = isVoiceRecording,
                onQuickAction = { action ->
                    onPromptChange(action)
                    onSend()
                },
                onOpenHomeScreenStudio = onOpenHomeScreenStudio,
                modifier = Modifier
                    .navigationBarsPadding()
                    .imePadding()
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Storage Access Banner (if needed)
            StorageBanner(
                visible = !hasStoragePermission && showStorageBanner,
                onGrantClick = onRequestStoragePermission,
                onDismiss = { showStorageBanner = false }
            )

            AnimatedVisibility(
                visible = !compilationStatus.isNullOrBlank(),
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Surface(
                    color = if (compilationStatus?.contains("failed", ignoreCase = true) == true) CrimsonGlow else AntigravitySurfaceElevated,
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(1.dp, if (compilationStatus?.contains("failed", ignoreCase = true) == true) CrimsonDanger.copy(alpha = 0.5f) else AntigravityBlue.copy(alpha = 0.5f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 4.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(14.dp),
                            strokeWidth = 2.dp,
                            color = if (compilationStatus?.contains("failed", ignoreCase = true) == true) CrimsonDanger else AntigravityCyan
                        )
                        Text(
                            text = compilationStatus ?: "",
                            color = if (compilationStatus?.contains("failed", ignoreCase = true) == true) CrimsonDanger else TextPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                        )
                    }
                }
            }

            if (messages.isEmpty()) {
                // Empty state: Minimal ChatGPT-style Welcome screen
                WelcomeHero(
                    authState = authState,
                    selectedModel = selectedModel,
                    workspacePath = workspaceName + (gitBranch?.let { " • 🌿 $it" } ?: ""),
                    onOpenModelPicker = { showModelPicker = true },
                    onOpenAuth = onOpenAuth,
                    onSelectPrompt = { prompt ->
                        onPromptChange(prompt)
                        onSend()
                    },
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                // Fluid Chat Canvas
                LazyColumn(
                    state = listState,
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    items(messages, key = { it.id }) { msg ->
                        when (msg.role) {
                            MessageRole.SYSTEM -> {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Surface(
                                        color = AntigravitySurface,
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Text(
                                            text = msg.content,
                                            color = TextSecondary,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Medium,
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                                        )
                                    }
                                }
                            }

                            MessageRole.USER -> {
                                // Right-aligned clean bubble
                                Box(
                                    modifier = Modifier.fillMaxWidth(),
                                    contentAlignment = Alignment.CenterEnd
                                ) {
                                    Surface(
                                        color = UserBubbleDark,
                                        shape = RoundedCornerShape(18.dp),
                                        modifier = Modifier.widthIn(max = 310.dp)
                                    ) {
                                        Text(
                                            text = msg.content,
                                            color = UserBubbleText,
                                            fontSize = 15.sp,
                                            lineHeight = 22.sp,
                                            fontWeight = FontWeight.Normal,
                                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                                        )
                                    }
                                }
                            }

                            MessageRole.ASSISTANT -> {
                                // Fluid full-width message directly on canvas
                                Box(
                                    modifier = Modifier.fillMaxWidth(),
                                    contentAlignment = Alignment.CenterStart
                                ) {
                                    Column(
                                        verticalArrangement = Arrangement.spacedBy(8.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        // Header Row: Small Logo + Antigravity Name
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(22.dp)
                                                    .clip(CircleShape)
                                                    .background(AntigravitySurfaceElevated),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = AgyIcons.Logo,
                                                    contentDescription = null,
                                                    tint = Color.Unspecified,
                                                    modifier = Modifier.size(13.dp)
                                                )
                                            }
                                            Text(
                                                text = "Antigravity",
                                                color = TextPrimary,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        }

                                        // Assistant Rich Markdown Body
                                        RichMarkdownMessage(
                                            content = msg.content,
                                            onOpenLink = onOpenLink
                                        )

                                        // Subtle Copy Action Icon
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            IconButton(
                                                onClick = {
                                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                                    clipboardManager.setText(AnnotatedString(msg.content))
                                                },
                                                modifier = Modifier.size(26.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.ContentCopy,
                                                    contentDescription = "Copy message",
                                                    tint = TextMuted,
                                                    modifier = Modifier.size(14.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            MessageRole.THOUGHT -> {
                                ThoughtCard(thoughtText = msg.content)
                            }

                            MessageRole.TOOL -> {
                                if (msg.apkResult != null) {
                                    val context = androidx.compose.ui.platform.LocalContext.current
                                    ApkInstallCard(
                                        result = msg.apkResult,
                                        onInstall = {
                                            msg.apkResult.apkFile?.let { apk ->
                                                com.yashwant.agymobile.util.ApkInstallerHelper.installApk(context, apk)
                                            }
                                        },
                                        onShare = {
                                            msg.apkResult.apkFile?.let { apk ->
                                                com.yashwant.agymobile.util.ApkInstallerHelper.shareApk(context, apk)
                                            }
                                        },
                                        onSaveDownloads = {
                                            msg.apkResult.apkFile?.let { apk ->
                                                com.yashwant.agymobile.util.ApkInstallerHelper.saveToDownloads(context, apk)
                                            }
                                        },
                                        onFixWithAi = { fixPrompt ->
                                            onFixWithAi(fixPrompt)
                                        }
                                    )
                                } else {
                                    ToolCallCard(
                                        toolName = msg.toolName ?: "action",
                                        argsJson = msg.toolArgs ?: msg.content,
                                        output = msg.toolOutput,
                                        error = msg.toolError,
                                        status = msg.toolStatus,
                                        durationMs = msg.durationMs,
                                        onCancelCommand = { onCancel() }
                                    )
                                }
                            }

                            MessageRole.APPROVAL -> {
                                ApprovalCard(
                                    toolName = msg.toolName ?: "Tool Execution",
                                    argsJson = msg.toolArgs ?: "{}",
                                    description = msg.content,
                                    onApprove = { onApprove(msg.toolCallId ?: "") },
                                    onDeny = { onDeny(msg.toolCallId ?: "") }
                                )
                            }

                            MessageRole.DIFF -> {
                                DiffCard(
                                    fileName = msg.diffFile ?: "File Diff",
                                    diffText = msg.content
                                )
                            }

                            MessageRole.ERROR -> {
                                val isAuthError = msg.content.contains("authentication required", ignoreCase = true) ||
                                        msg.content.contains("Run 'libagy.so' to log in", ignoreCase = true) ||
                                        msg.content.contains("authentication failed", ignoreCase = true) ||
                                        msg.content.contains("API key not valid", ignoreCase = true)

                                val isModelError = msg.content.contains("plan model not specified", ignoreCase = true) ||
                                        msg.content.contains("invalid model selection", ignoreCase = true) ||
                                        msg.content.contains("model not recognized", ignoreCase = true)

                                if (isAuthError) {
                                    Surface(
                                        color = AntigravitySurface,
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(14.dp),
                                            verticalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                Icon(
                                                    imageVector = AgyIcons.CyberKey,
                                                    contentDescription = null,
                                                    tint = AmberWarning,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Text(
                                                    text = "Authentication Required",
                                                    color = TextPrimary,
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.SemiBold
                                                )
                                            }
                                            Text(
                                                text = "Sign in or provide an API key to execute tools.",
                                                color = TextSecondary,
                                                fontSize = 12.sp
                                            )
                                            Button(
                                                onClick = onOpenAuth,
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = AntigravityBlue,
                                                    contentColor = Color.White
                                                ),
                                                shape = RoundedCornerShape(8.dp)
                                            ) {
                                                Text("Sign In / Key", fontSize = 12.sp)
                                            }
                                        }
                                    }
                                } else if (isModelError) {
                                    Surface(
                                        color = AntigravitySurface,
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(14.dp),
                                            verticalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                Icon(
                                                    imageVector = AgyIcons.ModelChip,
                                                    contentDescription = null,
                                                    tint = AntigravityCyan,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Text(
                                                    text = "Model Configuration",
                                                    color = TextPrimary,
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.SemiBold
                                                )
                                            }
                                            Text(
                                                text = "Active Model: $selectedModel. Select another model or provide a key.",
                                                color = TextSecondary,
                                                fontSize = 12.sp
                                            )
                                            Button(
                                                onClick = { showModelPicker = true },
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = AntigravityBlue,
                                                    contentColor = Color.White
                                                ),
                                                shape = RoundedCornerShape(8.dp)
                                            ) {
                                                Text("Choose Model", fontSize = 12.sp)
                                            }
                                        }
                                    }
                                } else {
                                    var showDiagnostics by remember { mutableStateOf(false) }
                                    Surface(
                                        color = Color(0x18EF4444),
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(12.dp),
                                            verticalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Warning,
                                                    contentDescription = "Error",
                                                    tint = CrimsonDanger,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Text(
                                                    text = msg.content.substringBefore("\nAGY_ERROR:"),
                                                    color = CrimsonDanger,
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.SemiBold
                                                )
                                            }

                                            if (msg.content.contains("AGY_ERROR:")) {
                                                Text(
                                                    text = if (showDiagnostics) "Hide Details" else "View Details",
                                                    color = TextMuted,
                                                    fontSize = 11.sp,
                                                    modifier = Modifier.clickable { showDiagnostics = !showDiagnostics }
                                                )

                                                AnimatedVisibility(
                                                    visible = showDiagnostics,
                                                    enter = expandVertically(),
                                                    exit = shrinkVertically()
                                                ) {
                                                    Text(
                                                        text = msg.content,
                                                        color = TextSecondary,
                                                        fontSize = 11.sp,
                                                        fontFamily = FontFamily.Monospace,
                                                        lineHeight = 15.sp,
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .clip(RoundedCornerShape(8.dp))
                                                            .background(AntigravityDarkBg)
                                                            .padding(8.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    if (isRunning && (messages.isEmpty() || messages.lastOrNull()?.role == MessageRole.USER)) {
                        item(key = "instant_thinking_indicator") {
                            ThinkingIndicator()
                        }
                    }
                }
            }
        }
    }

    if (showModelPicker) {
        ModelSelectorBottomSheet(
            selectedModel = selectedModel,
            onModelSelect = { model ->
                onSelectModel(model)
                showModelPicker = false
            },
            selectedEffort = selectedEffort,
            onEffortSelect = onSelectEffort,
            onDismiss = { showModelPicker = false }
        )
    }
}

@Composable
private fun RichMarkdownMessage(
    content: String,
    onOpenLink: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val clipboardManager = LocalClipboardManager.current
    val haptic = LocalHapticFeedback.current
    val blocks = remember(content) { MarkdownLinkParser.parseBlocks(content) }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        blocks.forEach { block ->
            when (block) {
                is MarkdownBlock.Heading -> {
                    val fontSize = when (block.level) {
                        1 -> 21.sp
                        2 -> 18.sp
                        else -> 15.sp
                    }
                    val fontWeight = if (block.level <= 2) FontWeight.Bold else FontWeight.SemiBold
                    val color = if (block.level <= 2) TextPrimary else AntigravityBlue
                    val topPadding = when (block.level) {
                        1 -> 10.dp
                        2 -> 8.dp
                        else -> 5.dp
                    }

                    Text(
                        text = MarkdownLinkParser.parseAnnotatedMarkdown(block.text, onLinkClick = onOpenLink),
                        color = color,
                        fontSize = fontSize,
                        fontWeight = fontWeight,
                        lineHeight = (fontSize.value + 6).sp,
                        modifier = Modifier.padding(top = topPadding, bottom = 2.dp)
                    )
                }

                is MarkdownBlock.BulletItem -> {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = (block.level * 12).dp, top = 2.dp, bottom = 2.dp)
                    ) {
                        Text(
                            text = "•",
                            color = AntigravityBlue,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = MarkdownLinkParser.parseAnnotatedMarkdown(block.text, onLinkClick = onOpenLink),
                            color = AssistantText,
                            fontSize = 15.sp,
                            lineHeight = 22.sp,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                    }
                }

                is MarkdownBlock.NumberedItem -> {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 2.dp, bottom = 2.dp)
                    ) {
                        Text(
                            text = block.number,
                            color = AntigravityCyan,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = MarkdownLinkParser.parseAnnotatedMarkdown(block.text, onLinkClick = onOpenLink),
                            color = AssistantText,
                            fontSize = 15.sp,
                            lineHeight = 22.sp,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                    }
                }

                is MarkdownBlock.Paragraph -> {
                    Text(
                        text = MarkdownLinkParser.parseAnnotatedMarkdown(block.text, onLinkClick = onOpenLink),
                        color = AssistantText,
                        fontSize = 15.sp,
                        lineHeight = 23.sp
                    )
                }

                is MarkdownBlock.Code -> {
                    Surface(
                        color = AntigravityDarkBg,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(AntigravitySurface)
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = block.language.ifBlank { "code" },
                                    color = TextSecondary,
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "Copy",
                                    color = AntigravityBlue,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.clickable {
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        clipboardManager.setText(AnnotatedString(block.code))
                                    }
                                )
                            }
                            val scrollState = rememberScrollState()
                            Text(
                                text = block.code,
                                color = AssistantText,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 12.sp,
                                lineHeight = 16.sp,
                                modifier = Modifier
                                    .horizontalScroll(scrollState)
                                    .padding(12.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ThinkingIndicator() {
    val infiniteTransition = rememberInfiniteTransition(label = "thinkingPulse")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.45f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(750, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    Surface(
        color = AntigravitySurface,
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(0.5.dp, AntigravityBorder),
        modifier = Modifier.padding(vertical = 4.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(AntigravityBlue.copy(alpha = alpha * 0.25f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = AgyIcons.Logo,
                    contentDescription = null,
                    tint = AntigravityBlue.copy(alpha = alpha),
                    modifier = Modifier.size(14.dp)
                )
            }

            Text(
                text = "Antigravity is thinking...",
                color = TextSecondary.copy(alpha = alpha),
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
