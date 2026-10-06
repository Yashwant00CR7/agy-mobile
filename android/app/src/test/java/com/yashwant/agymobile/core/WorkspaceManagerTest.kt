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
}
