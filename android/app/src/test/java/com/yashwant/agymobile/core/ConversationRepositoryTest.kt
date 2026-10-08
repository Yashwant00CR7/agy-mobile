package com.yashwant.agymobile.core

import com.yashwant.agymobile.model.ChatMessage
import com.yashwant.agymobile.model.ConversationSession
import com.yashwant.agymobile.model.MessageRole
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

class ConversationRepositoryTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    @Test
    fun testSaveAndLoadConversation() {
        val root = tempFolder.newFolder("files_dir")
        val repo = ConversationRepository(root)

        val convId = "conv-persisted-123"
        val messages = listOf(
            ChatMessage(
                role = MessageRole.USER,
                content = "Fix the login bug in MainActivity.kt"
            ),
            ChatMessage(
                role = MessageRole.THOUGHT,
                content = "Analyzing the codebase..."
            ),
            ChatMessage(
                role = MessageRole.TOOL,
                content = "{\"path\": \"MainActivity.kt\"}",
                toolName = "view_file",
                toolCallId = "call_01"
            ),
            ChatMessage(
                role = MessageRole.DIFF,
                content = "+ fun fix()",
                diffFile = "MainActivity.kt"
            ),
            ChatMessage(
                role = MessageRole.ASSISTANT,
                content = "Bug resolved successfully!"
            )
        )

        val session = ConversationSession(
            id = convId,
            title = "Fix the login bug in MainActivity.kt",
            workspacePath = "/sdcard/workspaces/ProjectA",
            model = "Gemini 3.8 Flash (Medium)",
            createdAt = 1000L,
            updatedAt = 2000L,
            messages = messages
        )

        repo.saveConversation(session)

        val loaded = repo.loadConversation(convId)
        assertNotNull(loaded)
        assertEquals(convId, loaded!!.id)
        assertEquals("Fix the login bug in MainActivity.kt", loaded.title)
        assertEquals("/sdcard/workspaces/ProjectA", loaded.workspacePath)
        assertEquals("Gemini 3.8 Flash (Medium)", loaded.model)
        assertEquals(5, loaded.messages.size)

        assertEquals(MessageRole.USER, loaded.messages[0].role)
        assertEquals("Fix the login bug in MainActivity.kt", loaded.messages[0].content)

        assertEquals(MessageRole.THOUGHT, loaded.messages[1].role)
        assertEquals("Analyzing the codebase...", loaded.messages[1].content)

        assertEquals(MessageRole.TOOL, loaded.messages[2].role)
        assertEquals("view_file", loaded.messages[2].toolName)
        assertEquals("call_01", loaded.messages[2].toolCallId)

        assertEquals(MessageRole.DIFF, loaded.messages[3].role)
        assertEquals("MainActivity.kt", loaded.messages[3].diffFile)

        assertEquals(MessageRole.ASSISTANT, loaded.messages[4].role)
        assertEquals("Bug resolved successfully!", loaded.messages[4].content)
    }

    @Test
    fun testListConversationsOrderedByRecency() {
        val root = tempFolder.newFolder("files_dir")
        val repo = ConversationRepository(root)

        val s1 = ConversationSession(
            id = "c1",
            title = "Old conversation",
            workspacePath = "/path/1",
            updatedAt = 1000L,
            messages = listOf(ChatMessage(role = MessageRole.USER, content = "Old"))
        )
        val s2 = ConversationSession(
            id = "c2",
            title = "New conversation",
            workspacePath = "/path/2",
            updatedAt = 5000L,
            messages = listOf(ChatMessage(role = MessageRole.USER, content = "New"))
        )

        repo.saveConversation(s1)
        repo.saveConversation(s2)

        val list = repo.listConversations()
        assertEquals(2, list.size)
        assertEquals("c2", list[0].id)
        assertEquals("New conversation", list[0].snippet)
        assertEquals("/path/2", list[0].workspacePath)

        assertEquals("c1", list[1].id)
        assertEquals("Old conversation", list[1].snippet)
    }

    @Test
    fun testDeleteConversation() {
        val root = tempFolder.newFolder("files_dir")
        val repo = ConversationRepository(root)

        val s = ConversationSession(
            id = "delete-me",
            title = "To be deleted",
            messages = emptyList()
        )
        repo.saveConversation(s)
        assertNotNull(repo.loadConversation("delete-me"))

        val deleted = repo.deleteConversation("delete-me")
        assertTrue(deleted)
        assertNull(repo.loadConversation("delete-me"))
    }

    @Test
    fun testFastModelDefaultCanonicalization() {
        assertEquals("gemini-3.8-flash-medium", BootstrapManager.toCanonicalModelSlug(null))
        assertEquals("gemini-3.8-flash-medium", BootstrapManager.toCanonicalModelSlug("Gemini 2.5 Flash"))
        assertEquals("gemini-3.8-flash-medium", BootstrapManager.toCanonicalModelSlug("Gemini 3.8 Flash (Medium)"))
        assertEquals("gemini-3.8-flash-high", BootstrapManager.toCanonicalModelSlug("Gemini 3.8 Flash (High)"))
        assertEquals("gemini-3.8-flash-low", BootstrapManager.toCanonicalModelSlug("Gemini 3.8 Flash (Low)"))
    }
}
