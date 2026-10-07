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
import androidx.compose.material.icons.filled.VpnKey
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
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    IconButton(
                        onClick = onOpenMenu,
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(ObsidianElevated)
                    ) {
                        Icon(imageVector = Icons.Default.Menu, contentDescription = "Menu", tint = TextPrimary)
                    }

                    // Antigravity Brand Logo Mark
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(ObsidianBlack)
                            .border(1.dp, HairlineBorder, RoundedCornerShape(8.dp)),
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
                            text = workspaceName.ifBlank { "Workspace" },
                            color = TextPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
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
                                text = gitBranch?.let { "🌿 $it" } ?: "⚡ libagy • ready",
                                color = if (gitBranch != null) ElectricCyan else EmeraldSuccess,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
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
                                .padding(horizontal = 9.dp, vertical = 4.dp)
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
                                            Brush.linearGradient(
                                                listOf(Color(0xFF3B0764), Color(0xFF1E1B4B))
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
                                        color = Color(0xFFF1F5F9),
                                        fontSize = 14.sp,
                                        lineHeight = 20.sp
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
                                        .fillMaxWidth(0.92f)
                                        .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomEnd = 16.dp, bottomStart = 4.dp))
                                        .background(ObsidianSurface)
                                        .border(1.dp, ObsidianBorder, RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomEnd = 16.dp, bottomStart = 4.dp))
                                        .padding(14.dp)
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
                            val isAuthError = msg.content.contains("authentication required", ignoreCase = true) ||
                                    msg.content.contains("Run 'libagy.so' to log in", ignoreCase = true) ||
                                    msg.content.contains("authentication failed", ignoreCase = true)

                            if (isAuthError) {
                                // Interactive Auth Action Card
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
                                                imageVector = Icons.Default.VpnKey,
                                                contentDescription = "Auth Required",
                                                tint = AmberWarning,
                                                modifier = Modifier.size(20.dp)
                                            )
                                            Text(
                                                text = "Engine Authentication Required",
                                                color = TextPrimary,
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }

                                        Text(
                                            text = "libagy needs active credentials to run agent turns and tool calls.",
                                            color = TextSecondary,
                                            fontSize = 12.sp,
                                            lineHeight = 16.sp
                                        )

                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Button(
                                                onClick = onOpenAuth,
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = ElectricCyan,
                                                    contentColor = ObsidianBlack
                                                ),
                                                shape = RoundedCornerShape(8.dp),
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Text(text = "Sign In / Enter Key", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                            }
                                        }
                                    }
                                }
                            } else {
                                // Standard Error Card
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(Color(0x1CEF4444))
                                        .border(1.dp, CrimsonDanger.copy(alpha = 0.6f), RoundedCornerShape(10.dp))
                                        .padding(12.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.Top,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Warning,
                                            contentDescription = "Error",
                                            tint = CrimsonDanger,
                                            modifier = Modifier.size(16.dp).padding(top = 2.dp)
                                        )
                                        Text(
                                            text = msg.content,
                                            color = CrimsonDanger,
                                            fontSize = 12.sp,
                                            fontFamily = FontFamily.Monospace,
                                            lineHeight = 16.sp
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
