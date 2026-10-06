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
    ERROR
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
    data class Error(val message: String, val timestamp: Long = System.currentTimeMillis()) : AgyEvent()
}

data class PromptRequest(
    val prompt: String,
    val conversationId: String? = null,
    val autonomous: Boolean = true,
    val workspaceDir: String? = null,
    val model: String? = null,
    val effort: String? = null
)
