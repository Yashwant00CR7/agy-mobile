package com.yashwant.agymobile.core

import com.yashwant.agymobile.model.AgentStatus
import com.yashwant.agymobile.model.AgyEvent
import com.yashwant.agymobile.model.PromptRequest
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONObject
import java.io.BufferedReader
import java.io.File
import java.io.InputStream
import java.io.InputStreamReader
import java.util.concurrent.ConcurrentHashMap

class AgyProcessEngine(
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob()),
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    private val _status = MutableStateFlow(AgentStatus.IDLE)
    val status: StateFlow<AgentStatus> = _status.asStateFlow()

    private val _events = MutableSharedFlow<AgyEvent>(extraBufferCapacity = 64)
    val events: SharedFlow<AgyEvent> = _events.asSharedFlow()

    private var activeProcess: Process? = null
    private var activeConversationId: String? = null
    private val pendingApprovals = ConcurrentHashMap<String, CompletableDeferred<Boolean>>()
    @Volatile private var turnCompleteEmitted = false

    fun getActiveConversationId(): String? = activeConversationId
    fun isRunning(): Boolean = _status.value == AgentStatus.RUNNING || _status.value == AgentStatus.WAITING_APPROVAL

    fun parseLine(line: String, autonomous: Boolean): AgyEvent? {
        val trimmed = line.trim()
        if (trimmed.isEmpty()) return null

        try {
            val json = JSONObject(trimmed)

            // Extract conversation ID across schemas
            val convId = when {
                json.has("conversationId") -> json.getString("conversationId")
                json.has("conversation_id") -> json.getString("conversation_id")
                json.optJSONObject("step_update")?.has("conversation_id") == true -> json.getJSONObject("step_update").getString("conversation_id")
                json.optJSONObject("result")?.has("conversation_id") == true -> json.getJSONObject("result").getString("conversation_id")
                else -> null
            }
            if (!convId.isNullOrBlank()) {
                activeConversationId = convId
            }

            val type = json.optString("type", json.optString("event", ""))

            // Real agy step_update schema
            if (type == "step_update" && json.has("step_update")) {
                val step = json.getJSONObject("step_update")

                if (step.has("thought") || step.has("thinking_delta") || step.has("thinking")) {
                    val delta = step.optString("thought", step.optString("thinking_delta", step.optString("thinking", "")))
                    return AgyEvent.Thought(delta)
                }

                if (step.has("text_delta")) {
                    return AgyEvent.Text(step.getString("text_delta"))
                }

                if (step.has("tool_call") || step.optString("step_type") == "tool_call" || step.optString("step_type") == "tool_use") {
                    val call = if (step.has("tool_call")) step.getJSONObject("tool_call") else step
                    val id = call.optString("id", "call_${System.currentTimeMillis()}")
                    val name = call.optString("name", call.optString("tool", "unknown"))
                    val argsObj = call.optJSONObject("args") ?: call.optJSONObject("parameters") ?: JSONObject()
                    val argsJson = argsObj.toString()

                    val requiresApproval = !autonomous && (step.optBoolean("requiresApproval", !autonomous))
                    if (requiresApproval) {
                        val desc = step.optString("description", "Approval required to execute $name")
                        return AgyEvent.ApprovalRequired(id, name, argsJson, desc)
                    }
                    return AgyEvent.ToolCall(id, name, argsJson)
                }

                if (step.has("tool_result") || step.optString("step_type") == "tool_result") {
                    val res = if (step.has("tool_result")) step.getJSONObject("tool_result") else step
                    val id = res.optString("id", "")
                    val name = res.optString("name", "")
                    val output = if (res.has("output")) res.getString("output") else null
                    val error = if (res.has("error")) res.getString("error") else null
                    return AgyEvent.ToolResult(id, name, output, error)
                }

                if (step.has("diff")) {
                    val file = step.optString("file", step.optString("path", "file"))
                    val diff = step.optString("diff", "")
                    return AgyEvent.Diff(file, diff)
                }

                return null
            }

            // Real agy result schema
            if (type == "result" && json.has("result")) {
                val res = json.getJSONObject("result")
                val summary = if (res.has("response")) res.getString("response") else if (res.has("status")) res.getString("status") else null
                turnCompleteEmitted = true
                return AgyEvent.TurnComplete(activeConversationId, summary)
            }

            // Mock / fallback NDJSON schemas
            if (type == "thought" || json.has("thought")) {
                val delta = json.optString("delta", json.optString("thought", json.optString("content", "")))
                return AgyEvent.Thought(delta)
            }

            if (type == "text" || type == "model_chunk" || json.has("text")) {
                val delta = json.optString("delta", json.optString("text", json.optString("content", "")))
                return AgyEvent.Text(delta)
            }

            if (type == "tool_call" || json.has("tool_call")) {
                val call = if (json.has("tool_call")) json.getJSONObject("tool_call") else json
                val id = call.optString("id", "call_${System.currentTimeMillis()}")
                val name = call.optString("name", call.optString("tool", "unknown"))
                val argsObj = call.optJSONObject("args") ?: call.optJSONObject("parameters") ?: JSONObject()
                val argsJson = argsObj.toString()

                val requiresApproval = !autonomous && (json.optBoolean("requiresApproval", true))
                if (requiresApproval) {
                    val desc = json.optString("description", "Approval required to execute $name")
                    return AgyEvent.ApprovalRequired(id, name, argsJson, desc)
                }

                return AgyEvent.ToolCall(id, name, argsJson)
            }

            if (type == "tool_result" || json.has("tool_result")) {
                val res = if (json.has("tool_result")) json.getJSONObject("tool_result") else json
                val id = res.optString("id", "")
                val name = res.optString("name", "")
                val output = if (res.has("output")) res.getString("output") else null
                val error = if (res.has("error")) res.getString("error") else null
                return AgyEvent.ToolResult(id, name, output, error)
            }

            if (type == "diff" || json.has("diff")) {
                val file = json.optString("file", json.optString("path", "file"))
                val diff = json.optString("diff", "")
                return AgyEvent.Diff(file, diff)
            }

            if (type == "turn_complete" || json.optBoolean("done", false)) {
                val summary = if (json.has("summary")) json.getString("summary") else null
                return AgyEvent.TurnComplete(activeConversationId, summary)
            }

            if (json.has("content")) {
                return AgyEvent.Text(json.getString("content"))
            }
        } catch (_: Exception) {
            // Non-JSON plaintext fallback
            val authUrl = AuthManager.extractOAuthUrl(trimmed)
            if (authUrl != null) {
                return AgyEvent.AuthRequired(authUrl)
            }
            return AgyEvent.Text("$trimmed\n")
        }

        return null
    }

    suspend fun processStream(inputStream: InputStream, autonomous: Boolean) = withContext(dispatcher) {
        val reader = BufferedReader(InputStreamReader(inputStream))
        var line: String? = null

        while (isActive) {
            val currentLine = reader.readLine() ?: break
            val event = parseLine(currentLine, autonomous)
            if (event != null) {
                if (event is AgyEvent.ApprovalRequired) {
                    _status.value = AgentStatus.WAITING_APPROVAL
                    val deferred = CompletableDeferred<Boolean>()
                    pendingApprovals[event.id] = deferred
                    _events.emit(event)

                    // Suspend until user resolves approval
                    try {
                        val approved = deferred.await()
                        _status.value = AgentStatus.RUNNING
                        _events.emit(AgyEvent.Status(AgentStatus.RUNNING, if (approved) "Approved" else "Denied"))
                    } finally {
                        pendingApprovals.remove(event.id)
                    }
                } else {
                    _events.emit(event)
                }
            }
        }
    }

    fun resolveApproval(callId: String, approved: Boolean): Boolean {
        val deferred = pendingApprovals[callId] ?: return false
        return deferred.complete(approved)
    }

    fun startTurn(
        agyExecutable: File,
        request: PromptRequest,
        envVars: Map<String, String>
    ): Job {
        if (isRunning()) {
            throw IllegalStateException("Agent turn is already running")
        }

        turnCompleteEmitted = false
        _status.value = AgentStatus.RUNNING

        return scope.launch {
            _events.emit(AgyEvent.Status(AgentStatus.RUNNING, "Starting agy in ${request.workspaceDir ?: "."}"))
            _events.emit(AgyEvent.TurnStart(request.conversationId))

            val args = mutableListOf(
                agyExecutable.absolutePath,
                "-p", request.prompt,
                "--output-format", "stream-json"
            )

            if (request.autonomous) {
                args.add("--dangerously-skip-permissions")
            }

            if (!request.conversationId.isNullOrBlank()) {
                args.add("--conversation")
                args.add(request.conversationId)
            }

            val modelName = request.model?.takeIf { it.isNotBlank() } ?: "Gemini 3.8 Flash (High)"
            args.add("--model")
            args.add(modelName)

            if (!request.effort.isNullOrBlank()) {
                args.add("--effort")
                args.add(request.effort)
            }

            val processBuilder = ProcessBuilder(args)
            if (request.workspaceDir != null) {
                val dir = File(request.workspaceDir)
                if (dir.exists()) processBuilder.directory(dir)
            }

            processBuilder.environment().putAll(envVars)

            try {
                val process = processBuilder.start()
                activeProcess = process

                val errorBuffer = StringBuilder()
                val errorJob = launch(Dispatchers.IO) {
                    try {
                        val errReader = process.errorStream.bufferedReader()
                        var errLine: String?
                        while (isActive) {
                            errLine = errReader.readLine() ?: break
                            errorBuffer.append(errLine).append("\n")
                            val authUrl = AuthManager.extractOAuthUrl(errLine)
                            if (authUrl != null) {
                                _events.emit(AgyEvent.AuthRequired(authUrl))
                            }
                        }
                    } catch (_: Exception) {}
                }

                // Process standard output
                val streamJob = launch {
                    processStream(process.inputStream, request.autonomous)
                }

                val exitCode = process.waitFor()
                streamJob.join()
                errorJob.cancelAndJoin()

                if (exitCode == 0) {
                    if (!turnCompleteEmitted) {
                        turnCompleteEmitted = true
                        _events.emit(AgyEvent.TurnComplete(activeConversationId, "Turn completed successfully"))
                    }
                    _status.value = AgentStatus.IDLE
                    _events.emit(AgyEvent.Status(AgentStatus.IDLE))
                } else {
                    val rawErr = errorBuffer.toString().trim()
                    val cleanErr = parseAgyErrorMessage(rawErr, exitCode)
                    _events.emit(AgyEvent.Error(cleanErr))
                    _status.value = AgentStatus.ERROR
                    _events.emit(AgyEvent.Status(AgentStatus.ERROR, "Process exited with code $exitCode"))
                }
            } catch (e: Exception) {
                if (e !is CancellationException) {
                    _events.emit(AgyEvent.Error(e.message ?: "Unknown process execution error"))
                    _status.value = AgentStatus.ERROR
                    _events.emit(AgyEvent.Status(AgentStatus.ERROR, e.message))
                }
            } finally {
                activeProcess = null
                if (_status.value != AgentStatus.ERROR) {
                    _status.value = AgentStatus.IDLE
                }
            }
        }
    }

    fun sendInput(input: String): Boolean {
        val proc = activeProcess ?: return false
        return try {
            proc.outputStream.bufferedWriter().apply {
                write(input)
                newLine()
                flush()
            }
            true
        } catch (_: Exception) {
            false
        }
    }

    fun cancel(): Boolean {
        val proc = activeProcess ?: return false
        proc.destroyForcibly()
        activeProcess = null
        _status.value = AgentStatus.IDLE
        scope.launch {
            _events.emit(AgyEvent.Status(AgentStatus.IDLE, "Turn cancelled"))
        }
        return true
    }

    companion object {
        fun parseAgyErrorMessage(raw: String, exitCode: Int): String {
            val trimmed = raw.trim()
            if (trimmed.isEmpty()) return "Process exited with code $exitCode"

            // Check if JSON error is inside AGY_ERROR:
            val agyErrorPrefix = "AGY_ERROR: "
            val idx = trimmed.indexOf(agyErrorPrefix)
            if (idx != -1) {
                val jsonStr = trimmed.substring(idx + agyErrorPrefix.length).trim()
                try {
                    val obj = org.json.JSONObject(jsonStr)
                    val shortErr = obj.optString("short_error", "")
                    if (shortErr.isNotBlank()) return shortErr
                } catch (_: Exception) {}
            }

            // Check for 'error: ' prefix in first line
            for (line in trimmed.lines()) {
                val lineTrimmed = line.trim()
                if (lineTrimmed.startsWith("error:", ignoreCase = true)) {
                    return lineTrimmed.removePrefix("error:").removePrefix("ERROR:").trim()
                }
            }

            return trimmed
        }
    }
}
