package com.yashwant.agymobile.core

import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

class WorkspaceManagerTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    @Test
    fun testGetWorkspaceInfoAndCreateProject() {
        val root = tempFolder.newFolder("workspaces")
        val manager = WorkspaceManager(root)

        val project1 = manager.createProject("ProjectAlpha")
        assertTrue(project1.exists())
        assertTrue(project1.isDirectory)

        val file1 = File(root, "notes.txt")
        file1.writeText("sample notes")

        val info = manager.getWorkspaceInfo(root)
        assertEquals(root.absolutePath, info.currentPath)
        assertEquals(2, info.items.size)

        // Directory sorted first
        assertTrue(info.items[0].isDirectory)
        assertEquals("ProjectAlpha", info.items[0].name)
        assertFalse(info.items[1].isDirectory)
        assertEquals("notes.txt", info.items[1].name)
    }

    @Test
    fun testGetRecentConversations() {
        val root = tempFolder.newFolder("workspaces")
        val brain = tempFolder.newFolder("brain")
        val manager = WorkspaceManager(root, brain)

        val conv1 = File(brain, "conv-abc-123/.system_generated/logs")
        conv1.mkdirs()
        File(conv1, "transcript.jsonl").writeText(
            JSONObject(mapOf("type" to "USER_INPUT", "content" to "Build native Android app")).toString() + "\n"
        )

        val convs = manager.getRecentConversations()
        assertEquals(1, convs.size)
        assertEquals("conv-abc-123", convs[0].id)
        assertEquals("Build native Android app", convs[0].snippet)
    }

    @Test
    fun testExternalDeviceWorkspace() {
        val root = tempFolder.newFolder("workspaces")
        val manager = WorkspaceManager(root)

        val externalDeviceFolder = tempFolder.newFolder("external_sdcard", "Download", "MyProject")
        File(externalDeviceFolder, "main.py").writeText("print('hello')")

        val info = manager.getWorkspaceInfo(externalDeviceFolder)
        assertEquals(externalDeviceFolder.absolutePath, info.currentPath)
        assertEquals(1, info.items.size)
        assertEquals("main.py", info.items[0].name)
        assertFalse(info.items[0].isDirectory)

        val sub = manager.createProject("submodule", externalDeviceFolder)
        assertTrue(sub.exists())
        assertEquals(File(externalDeviceFolder, "submodule").absolutePath, sub.absolutePath)
    }

    @Test
    fun testGetDeviceShortcuts() {
        val root = tempFolder.newFolder("workspaces")
        val manager = WorkspaceManager(root)
        val shortcuts = manager.getDeviceShortcuts()
        assertNotNull(shortcuts)
    }

    @Test
    fun testResolveTreeDocIdToPath() {
        val path1 = WorkspaceManager.resolveTreeDocIdToPath("primary:Download/MyCode")
        assertNotNull(path1)
        assertTrue(path1!!.endsWith("/Download/MyCode"))

        val pathRoot = WorkspaceManager.resolveTreeDocIdToPath("primary:")
        assertNotNull(pathRoot)

        val pathSecondary = WorkspaceManager.resolveTreeDocIdToPath("0123-4567:Projects")
        assertEquals("/storage/0123-4567/Projects", pathSecondary)

        assertNull(WorkspaceManager.resolveTreeDocIdToPath(null))
        assertNull(WorkspaceManager.resolveTreeDocIdToPath("invalid_format"))
    }

    @Test
    fun testWorkspacePersistenceAcrossRestarts() {
        val root = tempFolder.newFolder("workspaces_persist")
        val gemini = tempFolder.newFolder("gemini_persist")
        val settingsFile = File(gemini, "settings.json")
        val manager = WorkspaceManager(root, brainDir = File(gemini, "brain"), settingsFile = settingsFile)

        assertNull(manager.getLastWorkspacePath())
        assertEquals(root.absolutePath, manager.getInitialWorkspace().absolutePath)

        val phoneFolder = tempFolder.newFolder("device_storage", "Download", "MyProject")
        manager.saveLastWorkspacePath(phoneFolder.absolutePath)
        assertEquals(phoneFolder.absolutePath, manager.getLastWorkspacePath())
        assertEquals(phoneFolder.absolutePath, manager.getInitialWorkspace().absolutePath)

        // New manager instance simulating app restart
        val restartedManager = WorkspaceManager(root, brainDir = File(gemini, "brain"), settingsFile = settingsFile)
        assertEquals(phoneFolder.absolutePath, restartedManager.getLastWorkspacePath())
        assertEquals(phoneFolder.absolutePath, restartedManager.getInitialWorkspace().absolutePath)
    }

    @Test
    fun testDisplayNameAndStorageCheck() {
        val rootPath = WorkspaceManager.getPrimaryStoragePath()
        val rootFile = File(rootPath)
        assertEquals("Storage Root", WorkspaceManager.getDisplayName(rootFile))
        assertEquals("Storage Root", WorkspaceManager.getDisplayName(File("/storage/emulated/0")))

        val download = File(rootPath, "Download")
        assertEquals("Download", WorkspaceManager.getDisplayName(download))

        assertTrue(WorkspaceManager.isExternalStoragePath("/storage/emulated/0/Download"))
        assertTrue(WorkspaceManager.isExternalStoragePath("/sdcard/Download"))
        assertFalse(WorkspaceManager.isExternalStoragePath("/data/user/0/com.yashwant.agymobile/files"))
        assertFalse(WorkspaceManager.isExternalStoragePath(null))
    }

    @Test
    fun testDeleteBrainSession() {
        val root = tempFolder.newFolder("workspaces_del")
        val brain = tempFolder.newFolder("brain_del")
        val manager = WorkspaceManager(root, brain)

        val conv1 = File(brain, "conv-to-delete/.system_generated/logs")
        conv1.mkdirs()
        File(conv1, "transcript.jsonl").writeText("sample log")

        assertTrue(File(brain, "conv-to-delete").exists())
        val deleted = manager.deleteBrainSession("conv-to-delete")
        assertTrue(deleted)
        assertFalse(File(brain, "conv-to-delete").exists())
        assertFalse(manager.deleteBrainSession("non-existent"))
    }
}
