package com.yashwant.agymobile.ui

import com.yashwant.agymobile.core.ConversationItem
import com.yashwant.agymobile.model.ChatMessage
import com.yashwant.agymobile.model.MessageRole
import com.yashwant.agymobile.ui.components.QuickActions
import com.yashwant.agymobile.ui.theme.*
import org.junit.Assert.*
import org.junit.Test
import java.util.concurrent.TimeUnit

class ChatComponentTest {

    @Test
    fun testChatMessageModels() {
        val userMsg = ChatMessage(
            role = MessageRole.USER,
            content = "Build standalone app"
        )
        assertEquals(MessageRole.USER, userMsg.role)
        assertEquals("Build standalone app", userMsg.content)

        val toolMsg = ChatMessage(
            role = MessageRole.TOOL,
            content = "{\"cmd\":\"ls\"}",
            toolName = "exec_cmd"
        )
        assertEquals(MessageRole.TOOL, toolMsg.role)
        assertEquals("exec_cmd", toolMsg.toolName)

        val approvalMsg = ChatMessage(
            role = MessageRole.APPROVAL,
            content = "Please approve git push",
            toolCallId = "call_abc",
            toolName = "git_push"
        )
        assertEquals(MessageRole.APPROVAL, approvalMsg.role)
        assertEquals("call_abc", approvalMsg.toolCallId)
    }

    @Test
    fun testQuickActionsPresets() {
        assertTrue(QuickActions.isNotEmpty())
        assertEquals(5, QuickActions.size)
        assertTrue(QuickActions.any { it.title == "Device UI Inspection" })
        assertTrue(QuickActions.any { it.title == "Git Status" })
        assertTrue(QuickActions.any { it.title == "Review Diffs" })
    }

    @Test
    fun testChatGptIcons() {
        assertNotNull(AgyIcons.NewChat)
        assertNotNull(AgyIcons.ArrowUp)
        assertNotNull(AgyIcons.Plus)
        assertNotNull(AgyIcons.Sparkle)
    }

    @Test
    fun testAntigravityDarkTokens() {
        assertNotNull(AntigravityDarkBg)
        assertNotNull(AntigravitySurface)
        assertNotNull(AntigravitySurfaceElevated)
        assertNotNull(UserBubbleDark)
        assertNotNull(SendButtonActive)
        assertEquals(0x13, (AntigravityDarkBg.red * 255).toInt())
        assertEquals(0x13, (AntigravityDarkBg.green * 255).toInt())
        assertEquals(0x14, (AntigravityDarkBg.blue * 255).toInt())
    }

    @Test
    fun testConversationItemWithWorkspaceMetadata() {
        val item = ConversationItem(
            id = "conv-xyz",
            lastModified = 5000L,
            snippet = "Refactor auth",
            workspacePath = "/sdcard/workspaces/FinanceApp",
            messageCount = 12
        )
        assertEquals("conv-xyz", item.id)
        assertEquals(5000L, item.lastModified)
        assertEquals("Refactor auth", item.snippet)
        assertEquals("/sdcard/workspaces/FinanceApp", item.workspacePath)
        assertEquals(12, item.messageCount)
    }
}
