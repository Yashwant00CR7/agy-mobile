package com.yashwant.agymobile.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yashwant.agymobile.model.AuthState
import com.yashwant.agymobile.model.ChatMessage
import com.yashwant.agymobile.model.MessageRole
import com.yashwant.agymobile.ui.components.*
import com.yashwant.agymobile.ui.theme.*

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
    onClearChat: () -> Unit = {},
    hasStoragePermission: Boolean = true,
    onRequestStoragePermission: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()
    var showModelPicker by remember { mutableStateOf(false) }
    var showStorageBanner by remember { mutableStateOf(!hasStoragePermission) }
    val clipboardManager = LocalClipboardManager.current
    val haptic = LocalHapticFeedback.current

    // Auto-scroll on new messages
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = AntigravityDarkBg,
        topBar = {
            ChatGptTopBar(
                selectedModel = selectedModel,
                onOpenDrawer = onOpenMenu,
                onOpenModelPicker = { showModelPicker = true },
                onNewChat = onClearChat
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
                modifier = Modifier.navigationBarsPadding()
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

            if (messages.isEmpty()) {
                // Empty state: Minimal ChatGPT-style Welcome screen
                WelcomeHero(
                    authState = authState,
                    selectedModel = selectedModel,
                    workspacePath = workspaceName,
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

                                        // Assistant Text Body
                                        Text(
                                            text = msg.content,
                                            color = AssistantText,
                                            fontSize = 15.sp,
                                            lineHeight = 23.sp
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
                                ToolCallCard(
                                    toolName = msg.toolName ?: "action",
                                    argsJson = msg.toolArgs ?: msg.content
                                )
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
            onDismiss = { showModelPicker = false }
        )
    }
}
