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
import com.yashwant.agymobile.core.WorkspaceInfo
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
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var showWorkspaceBrowser by remember { mutableStateOf(false) }
    var newProjectName by remember { mutableStateOf("") }
    var showNewProjectDialog by remember { mutableStateOf(false) }

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
            val currentDirName = currentDir.name.ifBlank { "Workspace" }
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
                            Text(
                                text = currentDirName,
                                color = TextPrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            workspaceInfo.gitBranch?.let { branch ->
                                Text(
                                    text = "branch: $branch",
                                    color = AntigravityCyan,
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }
                    Icon(
                        imageVector = if (showWorkspaceBrowser) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = "Expand Workspaces",
                        tint = TextSecondary,
                        modifier = Modifier.size(16.dp)
                    )
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
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
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
                    workspaceInfo.items.filter { it.isDirectory }.take(5).forEach { item ->
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
}

@Composable
private fun DrawerHistoryItem(
    item: ConversationItem,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        color = Color.Transparent,
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 7.dp)
        ) {
            Text(
                text = item.snippet.ifBlank { "Untitled conversation" },
                color = TextPrimary,
                fontSize = 13.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
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
