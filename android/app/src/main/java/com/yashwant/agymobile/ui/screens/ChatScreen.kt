package com.yashwant.agymobile.ui.screens

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
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()

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
            // Header Bar
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(ObsidianSurface)
                    .statusBarsPadding()
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    IconButton(
                        onClick = onOpenMenu,
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(ObsidianElevated)
                    ) {
                        Icon(imageVector = Icons.Default.Menu, contentDescription = "Menu", tint = TextPrimary)
                    }

                    Column {
                        Text(
                            text = workspaceName.ifBlank { "Workspace" },
                            color = TextPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = gitBranch?.let { "git: $it" } ?: "no git",
                            color = ElectricCyan,
                            fontSize = 11.sp
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Auth Status Quick Chip
                    if (!authState.isAuthenticated) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(AmberGlow)
                                .border(1.dp, AmberWarning, RoundedCornerShape(16.dp))
                                .clickable { onOpenAuth() }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "Sign In",
                                color = AmberWarning,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Autonomous / Supervised Mode Pill
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (isAutonomous) Color(0x2610B981) else Color(0x26F59E0B))
                            .border(1.dp, if (isAutonomous) EmeraldSuccess else AmberWarning, RoundedCornerShape(16.dp))
                            .clickable { onToggleAutonomous() }
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(if (isAutonomous) EmeraldSuccess else AmberWarning)
                            )
                            Text(
                                text = if (isAutonomous) "Autonomous" else "Supervised",
                                color = if (isAutonomous) EmeraldSuccess else AmberWarning,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
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

            LazyColumn(
                state = listState,
                contentPadding = PaddingValues(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
            items(messages, key = { it.id }) { msg ->
                when (msg.role) {
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
                                        Brush.linearGradient(listOf(Color(0xFF2563EB), Color(0xFF1D4ED8)))
                                    )
                                    .padding(12.dp)
                            ) {
                                Text(text = msg.content, color = Color.White, fontSize = 14.sp)
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
                                    .fillMaxWidth(0.92f)
                                    .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomEnd = 16.dp, bottomStart = 4.dp))
                                    .background(ObsidianSurface)
                                    .border(1.dp, ObsidianBorder, RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomEnd = 16.dp, bottomStart = 4.dp))
                                    .padding(12.dp)
                            ) {
                                Text(text = msg.content, color = TextPrimary, fontSize = 14.sp, lineHeight = 20.sp)
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
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0x26EF4444))
                                .border(1.dp, CrimsonDanger, RoundedCornerShape(8.dp))
                                .padding(10.dp)
                        ) {
                            Text(text = "❌ ${msg.content}", color = CrimsonDanger, fontSize = 13.sp)
                        }
                    }
                }
            }
        }
    }
}
}
