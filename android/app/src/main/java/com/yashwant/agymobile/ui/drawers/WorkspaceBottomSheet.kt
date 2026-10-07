package com.yashwant.agymobile.ui.drawers

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.InsertDriveFile
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yashwant.agymobile.core.ConversationItem
import com.yashwant.agymobile.core.WorkspaceInfo
import com.yashwant.agymobile.ui.theme.*
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkspaceBottomSheet(
    workspaceInfo: WorkspaceInfo,
    conversations: List<ConversationItem>,
    onSelectDirectory: (File) -> Unit,
    onCreateProject: (String) -> Unit,
    onResumeConversation: (String) -> Unit,
    onDismiss: () -> Unit,
    authState: com.yashwant.agymobile.model.AuthState = com.yashwant.agymobile.model.AuthState(),
    onOpenAuth: () -> Unit = {},
    onSignOut: () -> Unit = {}
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var newProjectName by remember { mutableStateOf("") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = ObsidianSurface,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Google Account Bar
            Card(
                colors = CardDefaults.cardColors(containerColor = ObsidianElevated),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(androidx.compose.foundation.shape.CircleShape)
                                .background(if (authState.isAuthenticated) EmeraldSuccess else AmberWarning)
                        )
                        Column {
                            Text(
                                text = if (authState.isAuthenticated) (authState.userEmail ?: "Active Account") else "Authentication Required",
                                color = TextPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = if (authState.isAuthenticated) "Ready for native agent turns" else "Tap to authenticate agent",
                                color = TextMuted,
                                fontSize = 11.sp
                            )
                        }
                    }

                    if (authState.isAuthenticated) {
                        TextButton(onClick = { onSignOut() }) {
                            Text("Sign Out", color = CrimsonDanger, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    } else {
                        Button(
                            onClick = {
                                onDismiss()
                                onOpenAuth()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = AmberWarning, contentColor = ObsidianBlack),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier.height(30.dp)
                        ) {
                            Text("Sign In", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
            // Tab Selector
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = ObsidianElevated,
                contentColor = ElectricCyan,
                indicator = {},
                divider = {}
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(imageVector = Icons.Default.Folder, contentDescription = null, modifier = Modifier.size(16.dp))
                            Text("Workspaces", fontWeight = FontWeight.Bold)
                        }
                    }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(imageVector = Icons.Default.History, contentDescription = null, modifier = Modifier.size(16.dp))
                            Text("History", fontWeight = FontWeight.Bold)
                        }
                    }
                )
            }

            if (selectedTab == 0) {
                // Workspaces View
                Text(
                    text = workspaceInfo.currentPath,
                    color = ElectricCyan,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(ObsidianBlack, RoundedCornerShape(8.dp))
                        .padding(8.dp)
                )

                // New Project Input
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = newProjectName,
                        onValueChange = { newProjectName = it },
                        placeholder = { Text("New project name", color = TextMuted, fontSize = 12.sp) },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = ObsidianBlack,
                            unfocusedContainerColor = ObsidianBlack,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        shape = RoundedCornerShape(10.dp)
                    )
                    IconButton(
                        onClick = {
                            if (newProjectName.isNotBlank()) {
                                onCreateProject(newProjectName)
                                newProjectName = ""
                            }
                        },
                        modifier = Modifier
                            .size(46.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(ElectricCyan)
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = "Create", tint = ObsidianBlack)
                    }
                }

                // File/Folder List
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 300.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Parent folder option
                    val currentDir = File(workspaceInfo.currentPath)
                    val parent = currentDir.parentFile
                    if (parent != null && parent.exists()) {
                        item {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(ObsidianElevated)
                                    .clickable { onSelectDirectory(parent) }
                                    .padding(10.dp)
                            ) {
                                Icon(imageVector = Icons.Default.Folder, contentDescription = null, tint = ElectricCyan, modifier = Modifier.size(18.dp))
                                Text("📁 .. (Parent Directory)", color = TextPrimary, fontSize = 13.sp)
                            }
                        }
                    }

                    items(workspaceInfo.items) { item ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (item.isDirectory) ObsidianElevated else ObsidianBlack)
                                .clickable(enabled = item.isDirectory) { onSelectDirectory(File(item.path)) }
                                .padding(10.dp)
                        ) {
                            Icon(
                                imageVector = if (item.isDirectory) Icons.Default.Folder else Icons.Default.InsertDriveFile,
                                contentDescription = null,
                                tint = if (item.isDirectory) ElectricCyan else TextMuted,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = item.name,
                                color = if (item.isDirectory) TextPrimary else TextSecondary,
                                fontSize = 13.sp,
                                fontWeight = if (item.isDirectory) FontWeight.SemiBold else FontWeight.Normal
                            )
                        }
                    }
                }
            } else {
                // History View
                if (conversations.isEmpty()) {
                    Text(
                        text = "No previous conversations found.",
                        color = TextMuted,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(vertical = 16.dp)
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 350.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(conversations) { conv ->
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(ObsidianElevated)
                                    .clickable {
                                        onResumeConversation(conv.id)
                                        onDismiss()
                                    }
                                    .padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = "Session: ${conv.id.take(8)}...",
                                    color = ElectricCyan,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = conv.snippet,
                                    color = TextSecondary,
                                    fontSize = 13.sp,
                                    maxLines = 2
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
