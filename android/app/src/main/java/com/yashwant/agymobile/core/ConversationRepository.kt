package com.yashwant.agymobile.core

import com.yashwant.agymobile.model.ChatMessage
import com.yashwant.agymobile.model.ConversationSession
import com.yashwant.agymobile.model.MessageRole
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

class ConversationRepository(
    val baseDir: File
) {
    val conversationsDir: File = File(baseDir, "conversations")

    init {
        conversationsDir.mkdirs()
    }

    fun saveConversation(session: ConversationSession) {
        try {
            conversationsDir.mkdirs()
            val targetFile = File(conversationsDir, "${session.id}.json")
            val json = JSONObject()
            json.put("id", session.id)
            json.put("title", session.title)
            json.put("workspacePath", session.workspacePath ?: "")
            json.put("model", session.model ?: "")
            json.put("createdAt", session.createdAt)
            json.put("updatedAt", session.updatedAt)

            val messagesArr = JSONArray()
            for (msg in session.messages) {
                val mJson = JSONObject()
                mJson.put("id", msg.id)
                mJson.put("role", msg.role.name)
                mJson.put("content", msg.content)
                if (msg.toolName != null) mJson.put("toolName", msg.toolName)
                if (msg.toolCallId != null) mJson.put("toolCallId", msg.toolCallId)
                if (msg.toolArgs != null) mJson.put("toolArgs", msg.toolArgs)
                if (msg.toolOutput != null) mJson.put("toolOutput", msg.toolOutput)
                if (msg.toolError != null) mJson.put("toolError", msg.toolError)
                if (msg.toolStatus != null) mJson.put("toolStatus", msg.toolStatus)
                if (msg.durationMs != null) mJson.put("durationMs", msg.durationMs)
                if (msg.diffFile != null) mJson.put("diffFile", msg.diffFile)
                mJson.put("timestamp", msg.timestamp)
                messagesArr.put(mJson)
            }
            json.put("messages", messagesArr)

            targetFile.writeText(json.toString(2))
        } catch (_: Exception) {}
    }

    fun loadConversation(id: String): ConversationSession? {
        val file = File(conversationsDir, "$id.json")
        if (!file.exists() || !file.canRead()) return null

        return try {
            val content = file.readText()
            val json = JSONObject(content)
            val sessionKey = json.optString("id", id)
            val title = json.optString("title", "Session ${id.take(8)}")
            val workspacePath = json.optString("workspacePath").ifBlank { null }
            val model = json.optString("model").ifBlank { null }
            val createdAt = json.optLong("createdAt", file.lastModified())
            val updatedAt = json.optLong("updatedAt", file.lastModified())

            val messagesList = mutableListOf<ChatMessage>()
            val messagesArr = json.optJSONArray("messages")
            if (messagesArr != null) {
                for (i in 0 until messagesArr.length()) {
                    val mJson = messagesArr.getJSONObject(i)
                    val roleStr = mJson.optString("role", "SYSTEM")
                    val role = try {
                        MessageRole.valueOf(roleStr)
                    } catch (_: Exception) {
                        MessageRole.SYSTEM
                    }
                    val msg = ChatMessage(
                        id = mJson.optString("id", java.util.UUID.randomUUID().toString()),
                        role = role,
                        content = mJson.optString("content", ""),
                        toolName = if (mJson.has("toolName")) mJson.getString("toolName") else null,
                        toolCallId = if (mJson.has("toolCallId")) mJson.getString("toolCallId") else null,
                        toolArgs = if (mJson.has("toolArgs")) mJson.getString("toolArgs") else null,
                        toolOutput = if (mJson.has("toolOutput")) mJson.getString("toolOutput") else null,
                        toolError = if (mJson.has("toolError")) mJson.getString("toolError") else null,
                        toolStatus = if (mJson.has("toolStatus")) mJson.getString("toolStatus") else null,
                        durationMs = if (mJson.has("durationMs")) mJson.getLong("durationMs") else null,
                        diffFile = if (mJson.has("diffFile")) mJson.getString("diffFile") else null,
                        timestamp = mJson.optLong("timestamp", System.currentTimeMillis())
                    )
                    messagesList.add(msg)
                }
            }

            ConversationSession(
                id = sessionKey,
                title = title,
                workspacePath = workspacePath,
                model = model,
                createdAt = createdAt,
                updatedAt = updatedAt,
                messages = messagesList
            )
        } catch (_: Exception) {
            null
        }
    }

    fun listConversations(limit: Int = 50): List<ConversationItem> {
        val files = conversationsDir.listFiles { f -> f.isFile && f.name.endsWith(".json") } ?: emptyArray()
        val items = mutableListOf<ConversationItem>()

        for (file in files) {
            val id = file.nameWithoutExtension
            try {
                val text = file.readText()
                val json = JSONObject(text)
                val title = json.optString("title", "Session ${id.take(8)}")
                val updatedAt = json.optLong("updatedAt", file.lastModified())
                val workspacePath = json.optString("workspacePath").ifBlank { null }
                val messagesCount = json.optJSONArray("messages")?.length() ?: 0

                items.add(
                    ConversationItem(
                        id = id,
                        lastModified = updatedAt,
                        snippet = title,
                        workspacePath = workspacePath,
                        messageCount = messagesCount
                    )
                )
            } catch (_: Exception) {
                items.add(
                    ConversationItem(
                        id = id,
                        lastModified = file.lastModified(),
                        snippet = "Session ${id.take(8)}"
                    )
                )
            }
        }

        items.sortByDescending { it.lastModified }
        return items.take(limit)
    }

    fun deleteConversation(id: String): Boolean {
        val file = File(conversationsDir, "$id.json")
        return if (file.exists()) file.delete() else false
    }
}
