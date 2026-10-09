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
import java.io.BufferedWriter
import java.io.File
import java.io.InputStream
import java.io.InputStreamReader
import java.util.concurrent.ConcurrentHashMap

enum class LogLevel { INFO, WARN, ERROR, DEBUG }

data class EngineLogEntry(
    val timestamp: Long = System.currentTimeMillis(),
    val tag: String,
    val message: String,
    val level: LogLevel = LogLevel.INFO
)

class AgyProcessEngine(
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob()),
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    private val _status = MutableStateFlow(AgentStatus.IDLE)
    val status: StateFlow<AgentStatus> = _status.asStateFlow()

    private val _events = MutableSharedFlow<AgyEvent>(extraBufferCapacity = 64)
    val events: SharedFlow<AgyEvent> = _events.asSharedFlow()

    private val _engineLogs = MutableStateFlow<List<EngineLogEntry>>(emptyList())
    val engineLogs: StateFlow<List<EngineLogEntry>> = _engineLogs.asStateFlow()

    private var activeProcess: Process? = null
    private var activeConversationId: String? = null
    private val pendingApprovals = ConcurrentHashMap<String, CompletableDeferred<Boolean>>()
    @Volatile private var turnCompleteEmitted = false
    @Volatile private var isPlannedShutdown = false

    // Persistent stream-json daemon session management (REQ-25)
    data class DaemonConfig(
        val conversationId: String?,
        val workspaceDir: String?,
        val modelSlug: String,
        val effort: String,
        val autonomous: Boolean
    )
    private var daemonConfig: DaemonConfig? = null
    private var daemonWriter: BufferedWriter? = null
    private var daemonReaderJob: Job? = null
    private var daemonErrorJob: Job? = null
    private var currentTurnDeferred: CompletableDeferred<Unit>? = null

    fun getActiveConversationId(): String? = activeConversationId
    fun isRunning(): Boolean = _status.value == AgentStatus.RUNNING || _status.value == AgentStatus.WAITING_APPROVAL

    fun log(tag: String, message: String, level: LogLevel = LogLevel.INFO) {
        val entry = EngineLogEntry(tag = tag, message = message, level = level)
        val current = _engineLogs.value
        _engineLogs.value = if (current.size >= 250) current.drop(current.size - 249) + entry else current + entry
    }

    fun clearLogs() {
        _engineLogs.value = emptyList()
    }

    private fun isStreamCloseException(e: Throwable): Boolean {
        val msg = e.message?.lowercase() ?: ""
        return msg.contains("read interrupted by close()") ||
                msg.contains("stream closed") ||
                msg.contains("closed by peer") ||
                msg.contains("bad file descriptor") ||
                msg.contains("broken pipe") ||
                msg.contains("socket closed") ||
                e is java.io.InterruptedIOException ||
                e is CancellationException
    }

    fun stopDaemon() {
        isPlannedShutdown = true
        log("Engine", "Stopping daemon process", LogLevel.INFO)
        try {
            daemonReaderJob?.cancel()
            daemonErrorJob?.cancel()
            daemonWriter?.close()
        } catch (_: Exception) {}
        try {
            activeProcess?.destroyForcibly()
        } catch (_: Exception) {}
        daemonReaderJob = null
        daemonErrorJob = null
        daemonWriter = null
        activeProcess = null
        daemonConfig = null
        currentTurnDeferred?.complete(Unit)
        currentTurnDeferred = null
    }

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
                if (daemonConfig != null && daemonConfig?.conversationId != convId) {
                    daemonConfig = daemonConfig?.copy(conversationId = convId)
                }
            }

            val type = json.optString("type", json.optString("event", ""))

            // Real agy step_update schema
            if (type == "step_update" && json.has("step_update")) {
                val step = json.getJSONObject("step_update")
                val stepType = step.optString("step_type", "")

                if (step.has("text_delta")) {
                    return AgyEvent.Text(step.getString("text_delta"))
                }

                if (step.has("thought_delta")) {
                    return AgyEvent.Thought(step.getString("thought_delta"))
                }

                if (stepType == "thought" || stepType == "thinking") {
                    val delta = step.optString("delta", step.optString("text_delta", step.optString("thought", "")))
                    if (delta.isNotEmpty()) {
                        return AgyEvent.Thought(delta)
                    }
                }

                if (step.has("tool_call") || step.optString("step_type") == "tool_call" || step.optString("step_type") == "tool_use") {
                    val call = if (step.has("tool_call")) step.getJSONObject("tool_call") else step
                    val id = call.optString("id", "call_${System.currentTimeMillis()}")
                    val name = call.optString("name", call.optString("tool", "unknown"))
                    val argsObj = call.optJSONObject("args") ?: call.optJSONObject("parameters") ?: JSONObject()
                    val argsJson = argsObj.toString()
                    log("ToolCall", "$name (${argsJson.take(80)})", LogLevel.INFO)

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
                    log("ToolResult", "$name: ${(output ?: error ?: "ok").take(80)}", if (error != null) LogLevel.WARN else LogLevel.INFO)
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
                if (res.optString("status") == "ERROR") {
                    val err = res.optString("error", "Unknown agent error")
                    log("Engine", "Agent result error: $err", LogLevel.ERROR)
                    return AgyEvent.Error(err)
                }
                val summary = if (res.has("response")) res.getString("response") else if (res.has("status")) res.getString("status") else null
                turnCompleteEmitted = true
                log("Engine", "Turn complete for $activeConversationId", LogLevel.INFO)
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
                    if (event is AgyEvent.TurnComplete || event is AgyEvent.Error) {
                        _status.value = if (event is AgyEvent.Error) AgentStatus.ERROR else AgentStatus.IDLE
                        currentTurnDeferred?.complete(Unit)
                    }
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

        val rawModelSlug = BootstrapManager.toCanonicalModelSlug(request.model)
        val isFixed = BootstrapManager.isFixedTierModel(rawModelSlug)
        val cleanModelSlug = if (!isFixed) {
            rawModelSlug.removeSuffix("-high").removeSuffix("-medium").removeSuffix("-low")
        } else rawModelSlug

        val effort = if (isFixed) {
            BootstrapManager.fixedTierFor(rawModelSlug) ?: "high"
        } else {
            request.effort?.lowercase() ?: "low"
        }

        val targetConfig = DaemonConfig(
            conversationId = request.conversationId,
            workspaceDir = request.workspaceDir,
            modelSlug = cleanModelSlug,
            effort = effort,
            autonomous = request.autonomous
        )

        return scope.launch {
            _events.emit(AgyEvent.Status(AgentStatus.RUNNING, "Starting agy in ${request.workspaceDir ?: "."}"))
            _events.emit(AgyEvent.TurnStart(request.conversationId))

            val turnDeferred = CompletableDeferred<Unit>()
            currentTurnDeferred = turnDeferred

            val isDaemonAlive = activeProcess?.isAlive == true
            val isSameSession = targetConfig.conversationId == null ||
                    targetConfig.conversationId == activeConversationId ||
                    targetConfig.conversationId == daemonConfig?.conversationId

            val canReuseDaemon = isDaemonAlive &&
                    daemonWriter != null &&
                    daemonConfig != null &&
                    isSameSession &&
                    daemonConfig?.workspaceDir == targetConfig.workspaceDir &&
                    daemonConfig?.modelSlug == targetConfig.modelSlug &&
                    daemonConfig?.effort == targetConfig.effort &&
                    daemonConfig?.autonomous == targetConfig.autonomous

            if (canReuseDaemon) {
                // REQ-25 / REQ-38: Fast path - Reuse persistent warm process over stdin (Zero cold start)
                log("Engine", "Reusing warm daemon process (session: ${request.conversationId})", LogLevel.INFO)
                try {
                    currentTurnDeferred = turnDeferred
                    turnCompleteEmitted = false

                    val userMsg = JSONObject().apply {
                        put("event", "user")
                        put("message", JSONObject().apply {
                            put("content", request.prompt)
                        })
                    }
                    val writer = daemonWriter!!
                    writer.write(userMsg.toString())
                    writer.newLine()
                    writer.flush()
                } catch (e: Exception) {
                    log("Engine", "Error writing to warm daemon: ${e.message}, restarting", LogLevel.WARN)
                    stopDaemon()
                }
            }

            if (activeProcess?.isAlive != true || daemonWriter == null) {
                // Cold path: spawn daemon process with stream-json
                stopDaemon()
                isPlannedShutdown = false
                log("Engine", "Spawning cold agy daemon (model: $cleanModelSlug, dir: ${request.workspaceDir ?: "."})", LogLevel.INFO)

                val args = mutableListOf(
                    agyExecutable.absolutePath,
                    "--input-format", "stream-json",
                    "--output-format", "stream-json",
                    "--mode", "accept-edits"
                )

                if (request.autonomous) {
                    args.add("--dangerously-skip-permissions")
                }

                if (!request.conversationId.isNullOrBlank()) {
                    args.add("--conversation")
                    args.add(request.conversationId)
                }

                args.add("--model")
                args.add(cleanModelSlug)

                // REQ-24: Only pass --effort if model is NOT fixed-tier
                if (!isFixed) {
                    args.add("--effort")
                    args.add(effort)
                }

                val processBuilder = ProcessBuilder(args)
                val finalEnv = envVars.toMutableMap()
                if (request.workspaceDir != null) {
                    val dir = File(request.workspaceDir)
                    if (dir.exists()) {
                        processBuilder.directory(dir)
                    }
                    finalEnv["ANTIGRAVITY_WORKSPACE_ROOT"] = request.workspaceDir
                    finalEnv["WORKSPACE_DIR"] = request.workspaceDir
                    finalEnv["PWD"] = request.workspaceDir
                }
                processBuilder.environment().putAll(finalEnv)

                try {
                    val process = processBuilder.start()
                    activeProcess = process
                    daemonConfig = targetConfig
                    val writer = process.outputStream.bufferedWriter()
                    daemonWriter = writer

                    val errorBuffer = StringBuilder()
                    daemonErrorJob = launch(Dispatchers.IO) {
                        try {
                            val errReader = process.errorStream.bufferedReader()
                            var errLine: String?
                            while (isActive) {
                                errLine = errReader.readLine() ?: break
                                errorBuffer.append(errLine).append("\n")
                                log("Stderr", errLine, LogLevel.WARN)
                                val authUrl = AuthManager.extractOAuthUrl(errLine)
                                if (authUrl != null) {
                                    _events.emit(AgyEvent.AuthRequired(authUrl))
                                }
                            }
                        } catch (_: Exception) {}
                    }

                    daemonReaderJob = launch(Dispatchers.IO) {
                        try {
                            processStream(process.inputStream, request.autonomous)
                        } catch (e: Exception) {
                            if (!isPlannedShutdown && !isStreamCloseException(e)) {
                                log("Engine", "Stream read error: ${e.message}", LogLevel.ERROR)
                                _events.emit(AgyEvent.Error(e.message ?: "Stream read error"))
                            }
                        } finally {
                            if (_status.value == AgentStatus.RUNNING && !isPlannedShutdown) {
                                val rawErr = errorBuffer.toString().trim()
                                val cleanErr = parseAgyErrorMessage(rawErr, process.exitValueSafe())
                                log("Engine", "Abnormal process termination: $cleanErr", LogLevel.ERROR)
                                _events.emit(AgyEvent.Error(cleanErr))
                                _status.value = AgentStatus.ERROR
                            }
                            turnDeferred.complete(Unit)
                        }
                    }

                    // Send initial turn prompt over stdin
                    val userMsg = JSONObject().apply {
                        put("event", "user")
                        put("message", JSONObject().apply {
                            put("content", request.prompt)
                        })
                    }
                    writer.write(userMsg.toString())
                    writer.newLine()
                    writer.flush()
                } catch (e: Exception) {
                    stopDaemon()
                    if (e !is CancellationException) {
                        _events.emit(AgyEvent.Error(e.message ?: "Failed to start agent process"))
                        _status.value = AgentStatus.ERROR
                    }
                    turnDeferred.complete(Unit)
                }
            }

            try {
                turnDeferred.await()
            } finally {
                if (_status.value != AgentStatus.ERROR) {
                    _status.value = AgentStatus.IDLE
                }
            }
        }
    }

    private fun Process.exitValueSafe(): Int {
        return try {
            exitValue()
        } catch (_: Exception) {
            -1
        }
    }

    fun sendInput(input: String): Boolean {
        val writer = daemonWriter ?: return false
        return try {
            writer.write(input)
            writer.newLine()
            writer.flush()
            true
        } catch (_: Exception) {
            false
        }
    }

    fun cancel(): Boolean {
        stopDaemon()
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
