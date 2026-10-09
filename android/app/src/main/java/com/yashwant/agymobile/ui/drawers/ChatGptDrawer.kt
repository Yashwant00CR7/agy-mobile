package com.yashwant.agymobile.ui.drawers

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yashwant.agymobile.core.ConversationItem
import com.yashwant.agymobile.core.DeviceShortcut
import com.yashwant.agymobile.core.WorkspaceInfo
import com.yashwant.agymobile.core.WorkspaceManager
import com.yashwant.agymobile.model.AuthState
import com.yashwant.agymobile.ui.theme.*
import java.io.File
import java.util.concurrent.TimeUnit

@Composable
fun ChatGptDrawer(
    workspaceInfo: WorkspaceInfo,
    conversations: List<ConversationItem>,
    onSelectDirectory: (File) -> Unit,
    onCreateProject: (String) -> Unit,
    onResumeConversation: (String) -> Unit,
    onNewChat: () -> Unit,
    authState: AuthState,
    onOpenAuth: () -> Unit,
    onSignOut: () -> Unit,
    onCloseDrawer: () -> Unit,
    onOpenFolderPicker: () -> Unit = {},
    deviceShortcuts: List<com.yashwant.agymobile.core.DeviceShortcut> = emptyList(),
    onDeleteConversation: (String) -> Unit = {},
    engineLogs: List<com.yashwant.agymobile.core.EngineLogEntry> = emptyList(),
    onClearEngineLogs: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var showWorkspaceBrowser by remember { mutableStateOf(false) }
    var newProjectName by remember { mutableStateOf("") }
    var showNewProjectDialog by remember { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf(false) }
    var showEngineLogsDialog by remember { mutableStateOf(false) }
    var conversationPendingDelete by remember { mutableStateOf<ConversationItem?>(null) }

    ModalDrawerSheet(
        modifier = modifier
            .fillMaxHeight()
            .widthIn(max = 320.dp),
        drawerContainerColor = AntigravityDarkBg,
        drawerContentColor = TextPrimary
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            // 1. Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = {
                    Text("Search chats...", color = TextMuted, fontSize = 13.sp)
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = TextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }, modifier = Modifier.size(24.dp)) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear", tint = TextMuted, modifier = Modifier.size(14.dp))
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = AntigravitySurface,
                    unfocusedContainerColor = AntigravitySurface,
                    focusedBorderColor = AntigravityBorder,
                    unfocusedBorderColor = Color.Transparent,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(46.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            // 2. Primary "+ New Chat" Action Button
            Surface(
                onClick = {
                    onNewChat()
                    onCloseDrawer()
                },
                color = AntigravitySurface,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 11.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(26.dp)
                            .clip(CircleShape)
                            .background(AntigravitySurfaceElevated),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = AgyIcons.NewChat,
                            contentDescription = null,
                            tint = TextPrimary,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                    Text(
                        text = "New chat",
                        color = TextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 3. Workspace Status / Switcher Bar
            val currentDir = File(workspaceInfo.currentPath)
            val currentDirName = WorkspaceManager.getDisplayName(currentDir)
            Surface(
                onClick = { showWorkspaceBrowser = !showWorkspaceBrowser },
                color = AntigravitySurfaceElevated,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Folder,
                            contentDescription = null,
                            tint = AntigravityBlue,
                            modifier = Modifier.size(16.dp)
                        )
                        Column {
                            val isExternal = WorkspaceManager.isExternalStoragePath(currentDir.absolutePath)
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    text = currentDirName,
                                    color = TextPrimary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                if (isExternal) {
                                    Text(
                                        text = "DEVICE",
                                        color = AntigravityBlue,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier
                                            .background(AntigravityBlue.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                            .padding(horizontal = 4.dp, vertical = 1.dp)
                                    )
                                }
                            }
                            workspaceInfo.gitBranch?.let { branch ->
                                Text(
                                    text = "branch: $branch",
                                    color = AntigravityCyan,
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = onOpenFolderPicker,
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.FolderOpen,
                                contentDescription = "Choose Device Folder",
                                tint = AntigravityBlue,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Icon(
                            imageVector = if (showWorkspaceBrowser) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = "Expand Workspaces",
                            tint = TextSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            // Expanded Workspace Browser (if active)
            if (showWorkspaceBrowser) {
                Spacer(modifier = Modifier.height(8.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(AntigravitySurface)
                        .padding(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Prominent Device Folder Action Card
                    Surface(
                        onClick = onOpenFolderPicker,
                        color = AntigravityBlue.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, AntigravityBlue.copy(alpha = 0.35f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)
                        ) {
                            Icon(Icons.Default.FolderSpecial, contentDescription = null, tint = AntigravityBlue, modifier = Modifier.size(18.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Choose Device Folder", color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Text("Browse phone storage & SD card directly", color = TextMuted, fontSize = 10.sp)
                            }
                            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = AntigravityBlue, modifier = Modifier.size(16.dp))
                        }
                    }

                    // Device Shortcuts (Downloads, Documents, Storage Root)
                    if (deviceShortcuts.isNotEmpty()) {
                        Text(
                            text = "SHORTCUTS",
                            color = TextMuted,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                        deviceShortcuts.forEach { shortcut ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onSelectDirectory(shortcut.file) }
                                    .padding(horizontal = 8.dp, vertical = 5.dp)
                            ) {
                                val icon = when (shortcut.iconKey) {
                                    "download" -> Icons.Default.Download
                                    "document" -> Icons.Default.Description
                                    else -> Icons.Default.PhoneAndroid
                                }
                                Icon(icon, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(14.dp))
                                Text(shortcut.label, color = TextPrimary, fontSize = 12.sp)
                            }
                        }
                        HorizontalDivider(color = AntigravityBorder, modifier = Modifier.padding(vertical = 4.dp))
                    }

                    // Create Project Button
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showNewProjectDialog = true }
                            .padding(horizontal = 8.dp, vertical = 6.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, tint = EmeraldSuccess, modifier = Modifier.size(16.dp))
                            Text("New Project", color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                        }
                    }

                    // Parent folder link
                    val parent = currentDir.parentFile
                    if (parent != null && parent.exists()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSelectDirectory(parent) }
                                .padding(horizontal = 8.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(14.dp))
                            Text(".. (Parent Folder)", color = TextSecondary, fontSize = 12.sp)
                        }
                    }

                    // Directory items
                    workspaceInfo.items.filter { it.isDirectory }.take(8).forEach { item ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSelectDirectory(File(item.path)) }
                                .padding(horizontal = 8.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Default.Folder, contentDescription = null, tint = AntigravityBlue, modifier = Modifier.size(14.dp))
                            Text(item.name, color = TextPrimary, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 4. Conversation History List (Grouped chronologically)
            val filteredConversations = conversations.filter {
                searchQuery.isBlank() || it.snippet.contains(searchQuery, ignoreCase = true)
            }

            val grouped = groupConversations(filteredConversations)

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (grouped.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (searchQuery.isNotBlank()) "No matching chats" else "No chat history yet",
                                color = TextMuted,
                                fontSize = 12.sp
                            )
                        }
                    }
                } else {
                    grouped.forEach { (header, items) ->
                        item {
                            Text(
                                text = header,
                                color = TextMuted,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp)
                            )
                        }
                        items(items, key = { it.id }) { conv ->
                            DrawerHistoryItem(
                                item = conv,
                                onClick = {
                                    onResumeConversation(conv.id)
                                    onCloseDrawer()
                                },
                                onDelete = {
                                    conversationPendingDelete = conv
                                }
                            )
                        }
                    }
                }
            }

            // 5. Bottom Account & Auth Row
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(0.5.dp)
                    .background(AntigravityHairline)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Surface(
                color = Color.Transparent,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 6.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(AntigravitySurfaceElevated),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = AgyIcons.Logo,
                                contentDescription = null,
                                tint = Color.Unspecified,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Column {
                            Text(
                                text = if (authState.isAuthenticated) (authState.userEmail ?: "Antigravity User") else "Sign In",
                                color = TextPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = if (authState.isAuthenticated) "Personal Account" else "Tap to authenticate",
                                color = TextSecondary,
                                fontSize = 11.sp
                            )
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        IconButton(onClick = { showSettingsDialog = true }, modifier = Modifier.size(30.dp)) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Settings",
                                tint = TextSecondary,
                                modifier = Modifier.size(17.dp)
                            )
                        }

                        if (authState.isAuthenticated) {
                            IconButton(onClick = onSignOut, modifier = Modifier.size(30.dp)) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                                    contentDescription = "Sign Out",
                                    tint = TextSecondary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        } else {
                            TextButton(onClick = onOpenAuth) {
                                Text("Connect", color = AntigravityBlue, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }

    if (showNewProjectDialog) {
        AlertDialog(
            onDismissRequest = { showNewProjectDialog = false },
            title = { Text("New Project", color = TextPrimary) },
            text = {
                OutlinedTextField(
                    value = newProjectName,
                    onValueChange = { newProjectName = it },
                    placeholder = { Text("Project folder name") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (newProjectName.isNotBlank()) {
                            onCreateProject(newProjectName.trim())
                            newProjectName = ""
                            showNewProjectDialog = false
                        }
                    }
                ) {
                    Text("Create", color = AntigravityBlue)
                }
            },
            dismissButton = {
                TextButton(onClick = { showNewProjectDialog = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            },
            containerColor = AntigravitySurface,
            textContentColor = TextPrimary
        )
    }

    if (showSettingsDialog) {
        AlertDialog(
            onDismissRequest = { showSettingsDialog = false },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.Settings, contentDescription = null, tint = AntigravityBlue, modifier = Modifier.size(20.dp))
                    Text("Antigravity Settings", color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Surface(
                        color = AntigravityDarkBg,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("Active Engines:", color = TextMuted, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                            Text("• libagy.so (ARM64 Native Agent)", color = TextPrimary, fontSize = 11.sp, fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace)
                            Text("• libbash.so (ARM64 Native Shell)", color = TextPrimary, fontSize = 11.sp, fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace)
                        }
                    }
                    Text("Workspace: ${File(workspaceInfo.currentPath).name.ifBlank { "Default" }}", color = TextSecondary, fontSize = 12.sp)
                    Text("Version: agy-mobile v1.8.0 (Mobile AI)", color = TextMuted, fontSize = 11.sp)

                    OutlinedButton(
                        onClick = {
                            showSettingsDialog = false
                            showEngineLogsDialog = true
                        },
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = AntigravityBlue),
                        border = androidx.compose.foundation.BorderStroke(1.dp, AntigravityBlue.copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Code, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Engine & Shell Logs (${engineLogs.size})", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showSettingsDialog = false }) {
                    Text("Done", color = AntigravityBlue)
                }
            },
            containerColor = AntigravitySurface,
            textContentColor = TextPrimary
        )
    }

    if (showEngineLogsDialog) {
        val clipboardManager = androidx.compose.ui.platform.LocalClipboardManager.current
        val dateFormat = remember { java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault()) }

        AlertDialog(
            onDismissRequest = { showEngineLogsDialog = false },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.Code, contentDescription = null, tint = AntigravityCyan, modifier = Modifier.size(20.dp))
                        Text("Engine & Shell Logs", color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                    Text("${engineLogs.size} logs", color = TextMuted, fontSize = 11.sp)
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 400.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(
                            onClick = {
                                val text = engineLogs.joinToString("\n") {
                                    "[${dateFormat.format(java.util.Date(it.timestamp))}] [${it.tag}] ${it.level}: ${it.message}"
                                }
                                clipboardManager.setText(androidx.compose.ui.text.AnnotatedString(text))
                            }
                        ) {
                            Text("Copy All", color = AntigravityBlue, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                        TextButton(onClick = onClearEngineLogs) {
                            Text("Clear", color = RedError, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Surface(
                        color = AntigravityDarkBg,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f, fill = false)
                            .heightIn(min = 120.dp, max = 320.dp)
                    ) {
                        if (engineLogs.isEmpty()) {
                            Box(modifier = Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
                                Text("No engine logs recorded yet", color = TextMuted, fontSize = 12.sp)
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(8.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                items(engineLogs) { entry ->
                                    val timeStr = dateFormat.format(java.util.Date(entry.timestamp))
                                    val levelColor = when (entry.level) {
                                        com.yashwant.agymobile.core.LogLevel.ERROR -> RedError
                                        com.yashwant.agymobile.core.LogLevel.WARN -> AmberWarning
                                        com.yashwant.agymobile.core.LogLevel.DEBUG -> TextMuted
                                        else -> AntigravityCyan
                                    }
                                    Column(modifier = Modifier.fillMaxWidth()) {
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(timeStr, color = TextMuted, fontSize = 9.sp, fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace)
                                            Text("[${entry.tag}]", color = levelColor, fontSize = 9.sp, fontWeight = FontWeight.Bold, fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace)
                                        }
                                        Text(entry.message, color = TextPrimary, fontSize = 10.sp, fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace)
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showEngineLogsDialog = false }) {
                    Text("Close", color = AntigravityBlue)
                }
            },
            containerColor = AntigravitySurface,
            textContentColor = TextPrimary
        )
    }

    if (conversationPendingDelete != null) {
        val target = conversationPendingDelete!!
        AlertDialog(
            onDismissRequest = { conversationPendingDelete = null },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = null,
                        tint = RedError,
                        modifier = Modifier.size(20.dp)
                    )
                    Text("Delete Conversation?", color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Text(
                    text = "This will permanently delete \"${target.snippet.take(40)}\" and its session logs. This action cannot be undone.",
                    color = TextSecondary,
                    fontSize = 13.sp
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val id = target.id
                        conversationPendingDelete = null
                        onDeleteConversation(id)
                    }
                ) {
                    Text("Delete", color = RedError, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { conversationPendingDelete = null }) {
                    Text("Cancel", color = TextSecondary)
                }
            },
            containerColor = AntigravitySurface,
            textContentColor = TextPrimary
        )
    }
}

@Composable
private fun DrawerHistoryItem(
    item: ConversationItem,
    onClick: () -> Unit,
    onDelete: () -> Unit = {}
) {
    Surface(
        onClick = onClick,
        color = Color.Transparent,
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp)
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = item.snippet.ifBlank { "Untitled conversation" },
                    color = TextPrimary,
                    fontSize = 13.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                item.workspacePath?.let { path ->
                    if (path.isNotBlank()) {
                        val folderName = File(path).name.ifBlank { "Workspace" }
                        Spacer(modifier = Modifier.height(2.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Folder,
                                contentDescription = null,
                                tint = AntigravityBlue.copy(alpha = 0.8f),
                                modifier = Modifier.size(11.dp)
                            )
                            Text(
                                text = folderName,
                                color = TextMuted,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                            if (item.messageCount > 0) {
                                Text(
                                    text = "• ${item.messageCount} msgs",
                                    color = TextMuted.copy(alpha = 0.7f),
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }
                }
            }

            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(28.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete Conversation",
                    tint = TextMuted.copy(alpha = 0.5f),
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

private fun groupConversations(list: List<ConversationItem>): Map<String, List<ConversationItem>> {
    val now = System.currentTimeMillis()
    val oneDay = TimeUnit.DAYS.toMillis(1)
    val twoDays = TimeUnit.DAYS.toMillis(2)
    val sevenDays = TimeUnit.DAYS.toMillis(7)

    val result = linkedMapOf<String, MutableList<ConversationItem>>()

    for (item in list) {
        val age = now - item.lastModified
        val bucket = when {
            age < oneDay -> "Today"
            age < twoDays -> "Yesterday"
            age < sevenDays -> "Previous 7 Days"
            else -> "Older"
        }
        result.getOrPut(bucket) { mutableListOf() }.add(item)
    }
    return result
}
