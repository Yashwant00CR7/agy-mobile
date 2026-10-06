package com.yashwant.agymobile

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.*
import com.yashwant.agymobile.service.AgyAgentService
import com.yashwant.agymobile.ui.components.AuthBottomSheet
import com.yashwant.agymobile.ui.drawers.WorkspaceBottomSheet
import com.yashwant.agymobile.ui.screens.ChatScreen
import com.yashwant.agymobile.ui.theme.AgyTheme
import com.yashwant.agymobile.ui.util.BrowserHelper
import com.yashwant.agymobile.viewmodel.AgyViewModel
import java.io.File
import java.util.Locale

class MainActivity : ComponentActivity() {

    private val viewModel: AgyViewModel by viewModels()
    private var boundService: AgyAgentService? = null
    private var isBound = false
    private var speechRecognizer: SpeechRecognizer? = null

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as? AgyAgentService.LocalBinder
            val svc = binder?.getService()
            boundService = svc
            isBound = true
            svc?.let { viewModel.attachService(it) }
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            boundService = null
            isBound = false
        }
    }

    @OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Start & Bind Foreground Service
        AgyAgentService.startService(this)
        val serviceIntent = Intent(this, AgyAgentService::class.java)
        bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE)

        initSpeechRecognizer()

        setContent {
            AgyTheme {
                val messages by viewModel.messages.collectAsState()
                val promptText by viewModel.promptText.collectAsState()
                val isRunning by viewModel.isRunning.collectAsState()
                val isAutonomous by viewModel.isAutonomous.collectAsState()
                val workspaceInfo by viewModel.workspaceInfo.collectAsState()
                val conversations by viewModel.conversations.collectAsState()
                val isVoiceRecording by viewModel.isVoiceRecording.collectAsState()
                val authState by viewModel.authState.collectAsState()
                val showAuthSheet by viewModel.showAuthSheet.collectAsState()

                var showWorkspaceSheet by remember { mutableStateOf(false) }

                val workspaceDir = File(workspaceInfo.currentPath)
                val workspaceName = workspaceDir.name.ifBlank { "Workspace" }

                ChatScreen(
                    messages = messages,
                    promptText = promptText,
                    onPromptChange = { viewModel.onPromptChange(it) },
                    isRunning = isRunning,
                    isAutonomous = isAutonomous,
                    onToggleAutonomous = { viewModel.toggleAutonomous() },
                    workspaceName = workspaceName,
                    gitBranch = workspaceInfo.gitBranch,
                    onOpenMenu = { showWorkspaceSheet = true },
                    onSend = { viewModel.sendPrompt(boundService) },
                    onCancel = { viewModel.cancel(boundService) },
                    onApprove = { toolId -> viewModel.resolveApproval(toolId, true, boundService) },
                    onDeny = { toolId -> viewModel.resolveApproval(toolId, false, boundService) },
                    onVoiceClick = { toggleVoiceDictation() },
                    isVoiceRecording = isVoiceRecording,
                    authState = authState,
                    onOpenAuth = { viewModel.openAuthSheet() }
                )

                if (showAuthSheet) {
                    AuthBottomSheet(
                        authState = authState,
                        onDismiss = { viewModel.dismissAuthSheet() },
                        onStartLogin = { viewModel.startLogin() },
                        onLaunchBrowser = { url -> BrowserHelper.openUrl(this@MainActivity, url) },
                        onSubmitCode = { code -> viewModel.submitAuthCode(code) },
                        onImportJson = { json -> viewModel.importCredentials(json) },
                        onSignOut = { viewModel.signOut() }
                    )
                }

                if (showWorkspaceSheet) {
                    WorkspaceBottomSheet(
                        workspaceInfo = workspaceInfo,
                        conversations = conversations,
                        onSelectDirectory = { dir -> viewModel.selectWorkspace(dir) },
                        onCreateProject = { name -> viewModel.createProject(name) },
                        onResumeConversation = { id -> viewModel.resumeConversation(id) },
                        onDismiss = { showWorkspaceSheet = false },
                        authState = authState,
                        onOpenAuth = { viewModel.openAuthSheet() },
                        onSignOut = { viewModel.signOut() }
                    )
                }
            }
        }
    }

    private fun initSpeechRecognizer() {
        if (SpeechRecognizer.isRecognitionAvailable(this)) {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this).apply {
                setRecognitionListener(object : RecognitionListener {
                    override fun onResults(results: Bundle?) {
                        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        if (!matches.isNullOrEmpty()) {
                            viewModel.onPromptChange(matches[0])
                        }
                        viewModel.setVoiceRecording(false)
                    }

                    override fun onError(error: Int) {
                        viewModel.setVoiceRecording(false)
                    }

                    override fun onReadyForSpeech(params: Bundle?) {}
                    override fun onBeginningOfSpeech() {}
                    override fun onRmsChanged(rmsdB: Float) {}
                    override fun onBufferReceived(buffer: ByteArray?) {}
                    override fun onEndOfSpeech() {
                        viewModel.setVoiceRecording(false)
                    }
                    override fun onPartialResults(partialResults: Bundle?) {}
                    override fun onEvent(eventType: Int, params: Bundle?) {}
                })
            }
        }
    }

    private fun toggleVoiceDictation() {
        val recognizer = speechRecognizer ?: return
        if (viewModel.isVoiceRecording.value) {
            recognizer.stopListening()
            viewModel.setVoiceRecording(false)
        } else {
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            }
            recognizer.startListening(intent)
            viewModel.setVoiceRecording(true)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        speechRecognizer?.destroy()
        if (isBound) {
            unbindService(serviceConnection)
            isBound = false
        }
    }
}
