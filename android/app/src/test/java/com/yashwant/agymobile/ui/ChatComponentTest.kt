package com.yashwant.agymobile.ui

import com.yashwant.agymobile.model.ChatMessage
import com.yashwant.agymobile.model.MessageRole
import org.junit.Assert.*
import org.junit.Test

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
}
