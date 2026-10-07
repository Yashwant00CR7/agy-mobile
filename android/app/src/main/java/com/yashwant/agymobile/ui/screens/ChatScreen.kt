package com.yashwant.agymobile.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
    authState: com.yashwant.agymobile.model.AuthState = com.yashwant.agymobile.model.AuthState(isAuthenticated = true),
    onOpenAuth: () -> Unit = {},
    selectedModel: String = "Gemini 3.8 Flash (High)",
    onSelectModel: (String) -> Unit = {},
    onClearChat: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()
    var showModelPicker by remember { mutableStateOf(false) }

    // Auto-scroll on new messages
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = ObsidianBlack,
        topBar = {
            // High-Craft Cyber Header
            Surface(
                color = ObsidianSurface,
                modifier = Modifier.fillMaxWidth()
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
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        // Left: Menu + Brand Logo + Title
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            IconButton(
                                onClick = onOpenMenu,
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(ObsidianElevated)
                                    .border(1.dp, HairlineBorder, RoundedCornerShape(10.dp))
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Menu,
                                    contentDescription = "Menu",
                                    tint = TextPrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            // Antigravity Brand Mark
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(ObsidianBlack)
                                    .border(1.dp, ElectricCyan.copy(alpha = 0.5f), RoundedCornerShape(8.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = AgyIcons.Logo,
                                    contentDescription = "Antigravity",
                                    tint = Color.Unspecified,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            Column {
                                Text(
                                    text = "ANTIGRAVITY",
                                    color = TextPrimary,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.2.sp
                                )
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(5.dp)
                                            .clip(CircleShape)
                                            .background(if (gitBranch != null) ElectricCyan else EmeraldSuccess)
                                    )
                                    Text(
                                        text = gitBranch?.let { "🌿 $it" } ?: workspaceName.ifBlank { "workspace" },
                                        color = TextSecondary,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }

                        // Right: Model Selector Pill + Mode Toggle + Auth
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            // Model Selector Pill (Interactive)
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(ObsidianElevated)
                                    .border(1.dp, ElectricCyan.copy(alpha = 0.6f), RoundedCornerShape(16.dp))
                                    .clickable { showModelPicker = true }
                                    .padding(horizontal = 9.dp, vertical = 5.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = AgyIcons.ModelChip,
                                        contentDescription = null,
                                        tint = ElectricCyan,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Text(
                                        text = selectedModel.substringBefore(" (").replace("Gemini ", ""),
                                        color = ElectricCyan,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Icon(
                                        imageVector = AgyIcons.ChevronDown,
                                        contentDescription = "Select",
                                        tint = ElectricCyan,
                                        modifier = Modifier.size(10.dp)
                                    )
                                }
                            }

                            // Autonomous Pill Toggle
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(if (isAutonomous) Color(0x2010B981) else Color(0x20F59E0B))
                                    .border(1.dp, if (isAutonomous) EmeraldSuccess else AmberWarning, RoundedCornerShape(16.dp))
                                    .clickable { onToggleAutonomous() }
                                    .padding(horizontal = 8.dp, vertical = 5.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(5.dp)
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

                            // Clear Chat Icon if conversation is active
                            if (messages.isNotEmpty()) {
                                IconButton(
                                    onClick = onClearChat,
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.DeleteOutline,
                                        contentDescription = "Clear Chat",
                                        tint = TextMuted,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Bottom Hairline Border
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(HairlineBorder)
                    )
                }
            }
        },
        bottomBar = {
            InputBar(
                promptText = promptText,
                onPromptChange = onPromptChange,
                isRunning = isRunning,
                onSend = onSend,
                onCancel = onCancel,
                onVoiceClick = onVoiceClick,
                isVoiceRecording = isVoiceRecording,
                onChipClick = { chip ->
                    onPromptChange(chip)
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
            AuthBanner(
                authState = authState,
                onSignInClick = onOpenAuth
            )

            if (messages.isEmpty()) {
                // Empty state: High-craft Welcome Hub
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
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 12.dp)
                )
            } else {
                LazyColumn(
                    state = listState,
                    contentPadding = PaddingValues(14.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    items(messages, key = { it.id }) { msg ->
                        when (msg.role) {
                            MessageRole.SYSTEM -> {
                                // Clean, unobtrusive timeline event badge
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(ObsidianElevated)
                                            .border(1.dp, HairlineBorder, RoundedCornerShape(12.dp))
                                            .padding(horizontal = 12.dp, vertical = 6.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(6.dp)
                                                .clip(CircleShape)
                                                .background(ElectricCyan)
                                        )
                                        Text(
                                            text = msg.content,
                                            color = TextSecondary,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }

                            MessageRole.USER -> {
                                Box(
                                    modifier = Modifier.fillMaxWidth(),
                                    contentAlignment = Alignment.CenterEnd
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth(0.85f)
                                            .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 16.dp, bottomEnd = 4.dp))
                                            .background(
                                                Brush.linearGradient(
                                                    listOf(Color(0xFF2E1065), Color(0xFF1E1B4B))
                                                )
                                            )
                                            .border(
                                                1.dp,
                                                AntigravityViolet.copy(alpha = 0.5f),
                                                RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 16.dp, bottomEnd = 4.dp)
                                            )
                                            .padding(14.dp)
                                    ) {
                                        Text(
                                            text = msg.content,
                                            color = Color(0xFFF8FAFC),
                                            fontSize = 14.sp,
                                            lineHeight = 21.sp,
                                            fontWeight = FontWeight.Normal
                                        )
                                    }
                                }
                            }

                            MessageRole.ASSISTANT -> {
                                Box(
                                    modifier = Modifier.fillMaxWidth(),
                                    contentAlignment = Alignment.CenterStart
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth(0.94f)
                                            .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomEnd = 16.dp, bottomStart = 4.dp))
                                            .background(ObsidianSurface)
                                            .border(1.dp, ObsidianBorder, RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomEnd = 16.dp, bottomStart = 4.dp))
                                            .padding(14.dp)
                                    ) {
                                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                Icon(
                                                    imageVector = AgyIcons.Logo,
                                                    contentDescription = null,
                                                    tint = Color.Unspecified,
                                                    modifier = Modifier.size(14.dp)
                                                )
                                                Text(
                                                    text = "ANTIGRAVITY",
                                                    color = ElectricCyan,
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    letterSpacing = 1.sp
                                                )
                                            }
                                            Text(
                                                text = msg.content,
                                                color = TextPrimary,
                                                fontSize = 14.sp,
                                                lineHeight = 21.sp
                                            )
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
                                        msg.content.contains("authentication failed", ignoreCase = true)

                                val isModelError = msg.content.contains("plan model not specified", ignoreCase = true) ||
                                        msg.content.contains("invalid model selection", ignoreCase = true)

                                if (isAuthError) {
                                    // Interactive Auth Recovery Card
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .border(1.dp, AmberWarning.copy(alpha = 0.5f), RoundedCornerShape(14.dp)),
                                        colors = CardDefaults.cardColors(containerColor = ObsidianSurface)
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(16.dp),
                                            verticalArrangement = Arrangement.spacedBy(10.dp)
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                Icon(
                                                    imageVector = AgyIcons.CyberKey,
                                                    contentDescription = null,
                                                    tint = AmberWarning,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                                Text(
                                                    text = "Authentication Required",
                                                    color = TextPrimary,
                                                    fontSize = 14.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }

                                            Text(
                                                text = "libagy needs active credentials to execute prompts and tools.",
                                                color = TextSecondary,
                                                fontSize = 12.sp,
                                                lineHeight = 16.sp
                                            )

                                            Button(
                                                onClick = onOpenAuth,
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = ElectricCyan,
                                                    contentColor = ObsidianBlack
                                                ),
                                                shape = RoundedCornerShape(8.dp),
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Text(text = "Sign In / Enter Key", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                            }
                                        }
                                    }
                                } else if (isModelError) {
                                    // Interactive Model Error Card
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .border(1.dp, ElectricCyan.copy(alpha = 0.6f), RoundedCornerShape(14.dp)),
                                        colors = CardDefaults.cardColors(containerColor = ObsidianSurface)
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(16.dp),
                                            verticalArrangement = Arrangement.spacedBy(10.dp)
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                Icon(
                                                    imageVector = AgyIcons.ModelChip,
                                                    contentDescription = null,
                                                    tint = ElectricCyan,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                                Text(
                                                    text = "Agent Model Selection Required",
                                                    color = TextPrimary,
                                                    fontSize = 14.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }

                                            Text(
                                                text = "Select an available LLM model to continue executing with libagy.",
                                                color = TextSecondary,
                                                fontSize = 12.sp,
                                                lineHeight = 16.sp
                                            )

                                            Button(
                                                onClick = { showModelPicker = true },
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = ElectricCyan,
                                                    contentColor = ObsidianBlack
                                                ),
                                                shape = RoundedCornerShape(8.dp),
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Text(text = "Choose Model (Default: Gemini 3.8 Flash)", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                            }
                                        }
                                    }
                                } else {
                                    // Cleaned Error Diagnostic Card (no raw JSON dump)
                                    var showDiagnostics by remember { mutableStateOf(false) }

                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(Color(0x18EF4444))
                                            .border(1.dp, CrimsonDanger.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                                            .padding(14.dp)
                                    ) {
                                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
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
                                                    text = if (showDiagnostics) "Hide Details" else "View Diagnostics",
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
                                                            .background(ObsidianBlack)
                                                            .padding(10.dp)
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
