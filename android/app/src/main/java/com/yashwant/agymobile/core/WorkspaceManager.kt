package com.yashwant.agymobile.core

import org.json.JSONObject
import java.io.File

data class WorkspaceItem(
    val name: String,
    val path: String,
    val isDirectory: Boolean,
    val isGitRepo: Boolean,
    val size: Long = 0
)

data class WorkspaceInfo(
    val currentPath: String,
    val gitBranch: String?,
    val isGitRepo: Boolean,
    val items: List<WorkspaceItem>
)

data class ConversationItem(
    val id: String,
    val lastModified: Long,
    val snippet: String,
    val workspacePath: String? = null,
    val messageCount: Int = 0
)

data class DeviceShortcut(
    val label: String,
    val iconKey: String,
    val file: File
)

class WorkspaceManager(
    val rootWorkspaceDir: File,
    val brainDir: File = File(rootWorkspaceDir.parentFile, ".gemini/antigravity-cli/brain"),
    val settingsFile: File = File(rootWorkspaceDir.parentFile, ".gemini/settings.json")
) {

    fun getLastWorkspacePath(): String? {
        if (!settingsFile.exists()) return null
        return try {
            val json = JSONObject(settingsFile.readText())
            if (json.has("lastWorkspacePath")) {
                val path = json.getString("lastWorkspacePath")
                if (path.isNotBlank() && File(path).exists()) path else null
            } else null
        } catch (_: Exception) {
            null
        }
    }

    fun saveLastWorkspacePath(path: String) {
        try {
            settingsFile.parentFile?.mkdirs()
            val json = if (settingsFile.exists()) {
                try { JSONObject(settingsFile.readText()) } catch (_: Exception) { JSONObject() }
            } else JSONObject()
            json.put("lastWorkspacePath", path)
            settingsFile.writeText(json.toString(2))
        } catch (_: Exception) {}
    }

    fun getInitialWorkspace(): File {
        val lastPath = getLastWorkspacePath()
        if (lastPath != null) {
            val dir = File(lastPath)
            if (dir.exists() && dir.isDirectory) return dir
        }
        try {
            val rootPath = getPrimaryStoragePath()
            val downloads = File(rootPath, "Download")
            if (downloads.exists() && downloads.isDirectory && downloads.canRead()) {
                return downloads
            }
            val root = File(rootPath)
            if (root.exists() && root.isDirectory && root.canRead()) {
                return root
            }
        } catch (_: Exception) {}
        return rootWorkspaceDir
    }

    fun getWorkspaceInfo(targetDir: File = rootWorkspaceDir): WorkspaceInfo {
        val dir = if (targetDir.exists() && targetDir.isDirectory) targetDir else rootWorkspaceDir
        dir.mkdirs()

        val isGit = File(dir, ".git").exists()
        var gitBranch: String? = null

        if (isGit) {
            val headFile = File(dir, ".git/HEAD")
            if (headFile.exists()) {
                val headContent = headFile.readText().trim()
                gitBranch = if (headContent.startsWith("ref: refs/heads/")) {
                    headContent.removePrefix("ref: refs/heads/")
                } else {
                    headContent.take(7)
                }
            }
        }

        val items = mutableListOf<WorkspaceItem>()
        val files = dir.listFiles() ?: emptyArray()

        for (file in files) {
            if (file.name == ".DS_Store") continue
            val isDir = file.isDirectory
            items.add(
                WorkspaceItem(
                    name = file.name,
                    path = file.absolutePath,
                    isDirectory = isDir,
                    isGitRepo = isDir && File(file, ".git").exists(),
                    size = if (isDir) 0 else file.length()
                )
            )
        }

        items.sortWith(compareBy({ !it.isDirectory }, { it.name.lowercase() }))

        return WorkspaceInfo(
            currentPath = dir.absolutePath,
            gitBranch = gitBranch,
            isGitRepo = isGit,
            items = items
        )
    }

    fun createProject(name: String, parentDir: File = rootWorkspaceDir): File {
        val targetParent = if (parentDir.exists() && parentDir.isDirectory) parentDir else rootWorkspaceDir
        val projectDir = File(targetParent, name)
        projectDir.mkdirs()
        return projectDir
    }

    fun getDeviceShortcuts(): List<DeviceShortcut> {
        val shortcuts = mutableListOf<DeviceShortcut>()
        try {
            val rootPath = getPrimaryStoragePath()
            val extStorage = File(rootPath)
            if (extStorage.exists()) {
                shortcuts.add(DeviceShortcut("Storage Root", "storage", extStorage))
                val downloads = File(extStorage, "Download")
                if (downloads.exists()) {
                    shortcuts.add(DeviceShortcut("Downloads", "download", downloads))
                }
                val docs = File(extStorage, "Documents")
                if (docs.exists()) {
                    shortcuts.add(DeviceShortcut("Documents", "document", docs))
                }
            }
        } catch (_: Exception) {}
        return shortcuts
    }

    companion object {
        fun getPrimaryStoragePath(): String {
            return try {
                android.os.Environment.getExternalStorageDirectory().absolutePath
            } catch (_: Throwable) {
                "/storage/emulated/0"
            }
        }

        fun resolveTreeDocIdToPath(docId: String?): String? {
            if (docId == null) return null
            val parts = docId.split(":")
            if (parts.size >= 2) {
                val type = parts[0]
                val subPath = parts[1]
                if ("primary".equals(type, ignoreCase = true)) {
                    val root = getPrimaryStoragePath()
                    return if (subPath.isBlank()) root else "$root/$subPath"
                } else {
                    return "/storage/$type/$subPath"
                }
            }
            return null
        }

        fun isExternalStoragePath(path: String?): Boolean {
            if (path == null) return false
            return path.startsWith("/storage/") || path.startsWith("/sdcard")
        }

        fun getDisplayName(dir: File): String {
            val path = dir.absolutePath
            val primary = getPrimaryStoragePath()
            if (path == primary || path == "/storage/emulated/0" || dir.name == "0") {
                return "Storage Root"
            }
            return dir.name.ifBlank { "Workspace" }
        }
    }

    fun getLastWorkspace(): File? {
        val path = getLastWorkspacePath() ?: return null
        val file = File(path)
        return if (file.exists() && file.isDirectory) file else null
    }

    fun saveLastWorkspace(dir: File) {
        saveLastWorkspacePath(dir.absolutePath)
    }

    fun getRecentConversations(limit: Int = 20): List<ConversationItem> {
        if (!brainDir.exists()) return emptyList()

        val conversations = mutableListOf<ConversationItem>()
        val dirs = brainDir.listFiles { f -> f.isDirectory } ?: emptyArray()

        for (convDir in dirs) {
            val convId = convDir.name
            val transcriptFile = File(convDir, ".system_generated/logs/transcript.jsonl")
            var snippet = "Session ${convId.take(8)}"
            var lastMod = convDir.lastModified()

            if (transcriptFile.exists()) {
                lastMod = transcriptFile.lastModified()
                try {
                    transcriptFile.forEachLine { line ->
                        if (line.isNotBlank()) {
                            val json = JSONObject(line)
                            if (json.optString("type") == "USER_INPUT" && json.has("content")) {
                                snippet = json.getString("content").take(80).replace("\n", " ")
                                return@forEachLine
                            }
                        }
                    }
                } catch (_: Exception) {}
            }

            conversations.add(ConversationItem(convId, lastMod, snippet))
        }

        conversations.sortByDescending { it.lastModified }
        return conversations.take(limit)
    }
}
