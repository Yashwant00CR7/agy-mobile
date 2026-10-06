package com.yashwant.agymobile.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.yashwant.agymobile.core.BootstrapManager
import com.yashwant.agymobile.core.ConversationItem
import com.yashwant.agymobile.core.WorkspaceInfo
import com.yashwant.agymobile.core.WorkspaceManager
import com.yashwant.agymobile.model.*
import com.yashwant.agymobile.service.AgyAgentService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File

class AgyViewModel(application: Application) : AndroidViewModel(application) {

    val bootstrapManager = BootstrapManager(
        baseDir = application.filesDir,
        nativeLibDir = File(application.applicationInfo.nativeLibraryDir)
    )

    val workspaceManager = WorkspaceManager(
        rootWorkspaceDir = bootstrapManager.defaultWorkspaceDir,
        brainDir = File(application.filesDir, "home/.gemini/antigravity-cli/brain")
    )

    private val _messages = MutableStateFlow<List<ChatMessage>>(
        listOf(
            ChatMessage(
                role = MessageRole.ASSISTANT,
                content = "Antigravity Native Ready. Running in standalone mobile environment."
            )
        )
    )
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    private val _promptText = MutableStateFlow("")
    val promptText: StateFlow<String> = _promptText.asStateFlow()

    private val _isRunning = MutableStateFlow(false)
    val isRunning: StateFlow<Boolean> = _isRunning.asStateFlow()

    private val _isAutonomous = MutableStateFlow(true)
    val isAutonomous: StateFlow<Boolean> = _isAutonomous.asStateFlow()

    private val _workspaceInfo = MutableStateFlow(workspaceManager.getWorkspaceInfo())
    val workspaceInfo: StateFlow<WorkspaceInfo> = _workspaceInfo.asStateFlow()

    private val _conversations = MutableStateFlow(workspaceManager.getRecentConversations())
    val conversations: StateFlow<List<ConversationItem>> = _conversations.asStateFlow()

    private val _isVoiceRecording = MutableStateFlow(false)
    val isVoiceRecording: StateFlow<Boolean> = _isVoiceRecording.asStateFlow()

    private var activeConversationId: String? = null

    init {
        bootstrapManager.initializeDirectories()
        bootstrapManager.setupAgyFromNativeLib()
    }

    fun onPromptChange(newText: String) {
        _promptText.value = newText
    }

    fun toggleAutonomous() {
        _isAutonomous.value = !_isAutonomous.value
    }

    fun setVoiceRecording(recording: Boolean) {
        _isVoiceRecording.value = recording
    }

    fun selectWorkspace(directory: File) {
        _workspaceInfo.value = workspaceManager.getWorkspaceInfo(directory)
    }

    fun createProject(name: String) {
        val created = workspaceManager.createProject(name)
        selectWorkspace(created)
    }

    fun resumeConversation(convId: String) {
        activeConversationId = convId
        _messages.value = _messages.value + ChatMessage(
            role = MessageRole.ASSISTANT,
            content = "Resumed session $convId"
        )
    }

    private var eventsCollectionJob: kotlinx.coroutines.Job? = null
    private var boundServiceInstance: AgyAgentService? = null

    fun attachService(service: AgyAgentService) {
        if (boundServiceInstance !== service || eventsCollectionJob == null || eventsCollectionJob?.isActive == false) {
            eventsCollectionJob?.cancel()
            boundServiceInstance = service
            eventsCollectionJob = viewModelScope.launch {
                service.engine.events.collect { event ->
                    handleEvent(event)
                }
            }
        }
    }

    fun sendPrompt(service: AgyAgentService? = null) {
        val text = _promptText.value.trim()
        if (text.isBlank() || _isRunning.value) return

        _messages.value = _messages.value + ChatMessage(role = MessageRole.USER, content = text)
        _promptText.value = ""
        _isRunning.value = true

        val agyExe = bootstrapManager.getAgyExecutable()
        val engine = service?.engine

        if (service != null) {
            attachService(service)
        }

        if (agyExe != null && engine != null) {
            val env = bootstrapManager.getEnvironmentVariables()
            val request = PromptRequest(
                prompt = text,
                conversationId = activeConversationId,
                autonomous = _isAutonomous.value,
                workspaceDir = _workspaceInfo.value.currentPath
            )

            engine.startTurn(agyExe, request, env)
        } else {
            val reason = when {
                agyExe == null -> "Antigravity binary (libagy.so) is not found in native libraries. Please ensure the APK was installed with native libraries extracted."
                engine == null -> "Agent background engine is not yet connected. Please check service permissions."
                else -> "Engine not ready."
            }
            viewModelScope.launch {
                _messages.value = _messages.value + ChatMessage(
                    role = MessageRole.ERROR,
                    content = reason
                )
                _isRunning.value = false
            }
        }
    }

    private fun handleEvent(event: AgyEvent) {
        when (event) {
            is AgyEvent.TurnStart -> {
                _isRunning.value = true
                event.conversationId?.let { activeConversationId = it }
            }
            is AgyEvent.Thought -> {
                val current = _messages.value
                val last = current.lastOrNull()
                if (last?.role == MessageRole.THOUGHT) {
                    _messages.value = current.dropLast(1) + last.copy(content = last.content + event.delta)
                } else {
                    _messages.value = current + ChatMessage(role = MessageRole.THOUGHT, content = event.delta)
                }
            }
            is AgyEvent.Text -> {
                val current = _messages.value
                val last = current.lastOrNull()
                if (last?.role == MessageRole.ASSISTANT) {
                    _messages.value = current.dropLast(1) + last.copy(content = last.content + event.delta)
                } else {
                    _messages.value = current + ChatMessage(role = MessageRole.ASSISTANT, content = event.delta)
                }
            }
            is AgyEvent.ToolCall -> {
                _messages.value = _messages.value + ChatMessage(
                    role = MessageRole.TOOL,
                    content = event.argsJson,
                    toolName = event.name,
                    toolCallId = event.id,
                    toolArgs = event.argsJson
                )
            }
            is AgyEvent.ApprovalRequired -> {
                _messages.value = _messages.value + ChatMessage(
                    role = MessageRole.APPROVAL,
                    content = event.description,
                    toolName = event.name,
                    toolCallId = event.id,
                    toolArgs = event.argsJson
                )
            }
            is AgyEvent.Diff -> {
                _messages.value = _messages.value + ChatMessage(
                    role = MessageRole.DIFF,
                    content = event.diff,
                    diffFile = event.file
                )
            }
            is AgyEvent.TurnComplete -> {
                _isRunning.value = false
                event.conversationId?.let { activeConversationId = it }
                _conversations.value = workspaceManager.getRecentConversations()
            }
            is AgyEvent.Error -> {
                _messages.value = _messages.value + ChatMessage(
                    role = MessageRole.ERROR,
                    content = event.message
                )
                _isRunning.value = false
            }
            is AgyEvent.Status -> {
                if (event.status == AgentStatus.IDLE) {
                    _isRunning.value = false
                }
            }
            else -> {}
        }
    }

    fun cancel(service: AgyAgentService? = null) {
        service?.engine?.cancel()
        _isRunning.value = false
    }

    fun resolveApproval(callId: String, approved: Boolean, service: AgyAgentService? = null) {
        service?.engine?.resolveApproval(callId, approved)
    }
}
