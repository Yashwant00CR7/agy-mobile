package com.yashwant.agymobile.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import com.yashwant.agymobile.core.DeviceShortcut
import com.yashwant.agymobile.core.WorkspaceManager
import com.yashwant.agymobile.ui.theme.*
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeviceFolderPickerModal(
    initialDirectory: File,
    onSelectDirectory: (File) -> Unit,
    onDismiss: () -> Unit,
    hasStoragePermission: Boolean,
    onRequestStoragePermission: () -> Unit,
    deviceShortcuts: List<DeviceShortcut> = emptyList(),
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
) {
    var currentDir by remember {
        mutableStateOf(
            if (initialDirectory.exists() && initialDirectory.isDirectory) initialDirectory
            else {
                val primary = File(WorkspaceManager.getPrimaryStoragePath())
                if (primary.exists()) primary else initialDirectory
            }
        )
    }

    var showCreateFolderDialog by remember { mutableStateOf(false) }
    var newFolderName by remember { mutableStateOf("") }
    var refreshKey by remember { mutableIntStateOf(0) }

    val subFolders = remember(currentDir, refreshKey) {
        try {
            currentDir.listFiles()
                ?.filter { it.isDirectory && !it.name.startsWith(".") }
                ?.sortedBy { it.name.lowercase() }
                ?: emptyList()
        } catch (_: Exception) {
            emptyList()
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = AntigravityDarkBg,
        scrimColor = Color.Black.copy(alpha = 0.70f),
        dragHandle = {
            BottomSheetDefaults.DragHandle(color = AntigravityBorder)
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.88f)
                .padding(horizontal = 16.dp)
        ) {
            // Header Row: Title and Dismiss
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(AntigravityBlue.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.FolderSpecial,
                            contentDescription = null,
                            tint = AntigravityBlue,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "Choose Device Folder",
                            color = TextPrimary,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Native AI workspace on local phone storage",
                            color = TextMuted,
                            fontSize = 11.sp
                        )
                    }
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = TextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // Storage Permission Warning Banner (if not granted)
            if (!hasStoragePermission) {
                Surface(
                    color = AmberWarning.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, AmberWarning.copy(alpha = 0.35f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = AmberWarning,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "Full Storage Access Required",
                                color = TextPrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            text = "Android blocks direct folder access unless All Files Access is granted in Settings.",
                            color = TextSecondary,
                            fontSize = 11.sp,
                            lineHeight = 15.sp
                        )
                        Button(
                            onClick = onRequestStoragePermission,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AmberWarning,
                                contentColor = Color.Black
                            ),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Grant Access in Android Settings", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Quick Shortcut Chips (Downloads, Documents, Storage Root)
            val allShortcuts = if (deviceShortcuts.isNotEmpty()) deviceShortcuts else {
                listOf(
                    DeviceShortcut("Downloads", "download", File(WorkspaceManager.getPrimaryStoragePath(), "Download")),
                    DeviceShortcut("Documents", "document", File(WorkspaceManager.getPrimaryStoragePath(), "Documents")),
                    DeviceShortcut("Phone Root", "phone", File(WorkspaceManager.getPrimaryStoragePath()))
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                allShortcuts.forEach { shortcut ->
                    val isSelected = currentDir.absolutePath == shortcut.file.absolutePath
                    Surface(
                        onClick = {
                            if (shortcut.file.exists()) {
                                currentDir = shortcut.file
                            }
                        },
                        color = if (isSelected) AntigravityBlue.copy(alpha = 0.2f) else AntigravitySurface,
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isSelected) AntigravityBlue else AntigravityBorder
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            val icon = when (shortcut.iconKey) {
                                "download" -> Icons.Default.Download
                                "document" -> Icons.Default.Description
                                else -> Icons.Default.PhoneAndroid
                            }
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = if (isSelected) AntigravityBlue else TextSecondary,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = shortcut.label,
                                color = if (isSelected) TextPrimary else TextSecondary,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    }
                }
            }

            // Path Breadcrumb & Up Navigation with Clickable Segments
            Surface(
                color = AntigravitySurfaceElevated,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                ) {
                    val parent = currentDir.parentFile
                    val canGoUp = parent != null && parent.exists() && parent.canRead() &&
                            (parent.absolutePath.startsWith("/storage") || parent.absolutePath.startsWith("/sdcard"))

                    if (canGoUp) {
                        IconButton(
                            onClick = { parent?.let { currentDir = it } },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Go Up",
                                tint = AntigravityBlue,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    } else {
                        Icon(
                            imageVector = Icons.Default.Folder,
                            contentDescription = null,
                            tint = AntigravityBlue,
                            modifier = Modifier
                                .padding(start = 4.dp, end = 4.dp)
                                .size(16.dp)
                        )
                    }

                    // Clickable breadcrumb segments
                    val segments = remember(currentDir) {
                        val segList = mutableListOf<Pair<String, File>>()
                        var curr: File? = currentDir
                        while (curr != null) {
                            val name = WorkspaceManager.getDisplayName(curr)
                            segList.add(0, Pair(name, curr))
                            if (curr.absolutePath == "/storage/emulated/0" || curr.absolutePath == "/storage" || curr.parentFile == null) break
                            curr = curr.parentFile
                        }
                        segList
                    }

                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .horizontalScroll(rememberScrollState()),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        segments.forEachIndexed { idx, (name, file) ->
                            val isLast = idx == segments.lastIndex
                            Text(
                                text = name,
                                color = if (isLast) TextPrimary else AntigravityBlue,
                                fontSize = 12.sp,
                                fontWeight = if (isLast) FontWeight.Bold else FontWeight.Medium,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .clickable(enabled = !isLast) { currentDir = file }
                                    .padding(horizontal = 4.dp, vertical = 2.dp)
                            )
                            if (!isLast) {
                                Text(
                                    text = "/",
                                    color = TextMuted,
                                    fontSize = 11.sp,
                                    modifier = Modifier.padding(horizontal = 1.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Folder List
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(vertical = 6.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                if (subFolders.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.FolderOpen,
                                    contentDescription = null,
                                    tint = TextMuted,
                                    modifier = Modifier.size(36.dp)
                                )
                                Text(
                                    text = "No subfolders found",
                                    color = TextMuted,
                                    fontSize = 13.sp
                                )
                                Text(
                                    text = "Tap 'Select Current Folder' below to use this directory",
                                    color = TextSecondary,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                } else {
                    items(subFolders, key = { it.absolutePath }) { folder ->
                        val count = folder.listFiles()?.count { !it.name.startsWith(".") } ?: 0
                        Surface(
                            onClick = { currentDir = folder },
                            color = AntigravitySurface,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Folder,
                                        contentDescription = null,
                                        tint = AntigravityBlue,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Column {
                                        Text(
                                            text = folder.name,
                                            color = TextPrimary,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = "$count items",
                                            color = TextMuted,
                                            fontSize = 11.sp
                                        )
                                    }
                                }

                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                    contentDescription = "Open folder",
                                    tint = TextSecondary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Bottom Actions: "+ New Folder" & "Select This Folder"
            Surface(
                color = AntigravitySurfaceElevated,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.padding(10.dp)
                ) {
                    // New Folder Button
                    OutlinedButton(
                        onClick = { showCreateFolderDialog = true },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = Color.Transparent,
                            contentColor = TextPrimary
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.dp, AntigravityBorder),
                        modifier = Modifier.height(44.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CreateNewFolder,
                            contentDescription = "New Folder",
                            tint = EmeraldSuccess,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("New", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }

                    // Select Folder Button
                    Button(
                        onClick = {
                            onSelectDirectory(currentDir)
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AntigravityBlue,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Select",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Select \"${WorkspaceManager.getDisplayName(currentDir)}\"",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }

    // Dialog for creating a new subfolder in the current directory
    if (showCreateFolderDialog) {
        var folderCreationError by remember { mutableStateOf<String?>(null) }

        AlertDialog(
            onDismissRequest = {
                showCreateFolderDialog = false
                folderCreationError = null
            },
            title = {
                Text(
                    text = "Create Folder in ${WorkspaceManager.getDisplayName(currentDir)}",
                    color = TextPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    OutlinedTextField(
                        value = newFolderName,
                        onValueChange = {
                            newFolderName = it
                            folderCreationError = null
                        },
                        placeholder = { Text("Folder name (e.g., MyAgyProject)") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedBorderColor = AntigravityBlue,
                            unfocusedBorderColor = AntigravityBorder,
                            focusedContainerColor = AntigravitySurface,
                            unfocusedContainerColor = AntigravitySurface
                        )
                    )
                    folderCreationError?.let { err ->
                        Text(text = err, color = CrimsonDanger, fontSize = 11.sp)
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val name = newFolderName.trim()
                        if (name.isNotBlank()) {
                            val newDir = File(currentDir, name)
                            val ok = try { newDir.mkdirs() || newDir.exists() } catch (_: Exception) { false }
                            if (ok) {
                                newFolderName = ""
                                folderCreationError = null
                                showCreateFolderDialog = false
                                refreshKey++
                                currentDir = newDir
                            } else {
                                folderCreationError = "Could not create directory. Check storage permissions."
                            }
                        }
                    }
                ) {
                    Text("Create", color = AntigravityBlue, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showCreateFolderDialog = false
                    folderCreationError = null
                }) {
                    Text("Cancel", color = TextSecondary)
                }
            },
            containerColor = AntigravitySurface,
            textContentColor = TextPrimary
        )
    }
}
