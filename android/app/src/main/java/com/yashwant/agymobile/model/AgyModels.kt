package com.yashwant.agymobile.model

import java.util.UUID

enum class AgentStatus {
    IDLE,
    RUNNING,
    WAITING_APPROVAL,
    ERROR
}

enum class MessageRole {
    USER,
    ASSISTANT,
    THOUGHT,
    TOOL,
    APPROVAL,
    DIFF,
    ERROR,
    SYSTEM
}

data class ChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val role: MessageRole,
    val content: String,
    val toolName: String? = null,
    val toolCallId: String? = null,
    val toolArgs: String? = null,
    val diffFile: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)

data class ConversationSession(
    val id: String,
    val title: String,
    val workspacePath: String? = null,
    val model: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val messages: List<ChatMessage> = emptyList()
)

sealed class AgyEvent {
    data class TurnStart(val conversationId: String?, val timestamp: Long = System.currentTimeMillis()) : AgyEvent()
    data class Thought(val delta: String, val timestamp: Long = System.currentTimeMillis()) : AgyEvent()
    data class Text(val delta: String, val timestamp: Long = System.currentTimeMillis()) : AgyEvent()
    data class ToolCall(
        val id: String,
        val name: String,
        val argsJson: String,
        val timestamp: Long = System.currentTimeMillis()
    ) : AgyEvent()
    data class ApprovalRequired(
        val id: String,
        val name: String,
        val argsJson: String,
        val description: String,
        val timestamp: Long = System.currentTimeMillis()
    ) : AgyEvent()
    data class ToolResult(
        val id: String,
        val name: String,
        val output: String?,
        val error: String?,
        val timestamp: Long = System.currentTimeMillis()
    ) : AgyEvent()
    data class Diff(val file: String, val diff: String, val timestamp: Long = System.currentTimeMillis()) : AgyEvent()
    data class TurnComplete(
        val conversationId: String?,
        val summary: String?,
        val timestamp: Long = System.currentTimeMillis()
    ) : AgyEvent()
    data class Status(
        val status: AgentStatus,
        val message: String? = null,
        val timestamp: Long = System.currentTimeMillis()
    ) : AgyEvent()
    data class AuthRequired(val authUrl: String, val timestamp: Long = System.currentTimeMillis()) : AgyEvent()
    data class AuthSuccess(val userEmail: String?, val timestamp: Long = System.currentTimeMillis()) : AgyEvent()
    data class Error(val message: String, val timestamp: Long = System.currentTimeMillis()) : AgyEvent()
}

data class AuthState(
    val isAuthenticated: Boolean = false,
    val userEmail: String? = null,
    val isAuthenticating: Boolean = false,
    val authUrl: String? = null,
    val error: String? = null
)

data class PromptRequest(
    val prompt: String,
    val conversationId: String? = null,
    val autonomous: Boolean = true,
    val workspaceDir: String? = null,
    val model: String? = null,
    val effort: String? = null
)
