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
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.rememberDrawerState
import com.yashwant.agymobile.core.WorkspaceManager
import com.yashwant.agymobile.service.AgyAgentService
import com.yashwant.agymobile.ui.components.AuthBottomSheet
import com.yashwant.agymobile.ui.components.DeviceFolderPickerModal
import com.yashwant.agymobile.ui.drawers.ChatGptDrawer
import com.yashwant.agymobile.ui.screens.ChatScreen
import com.yashwant.agymobile.ui.theme.AgyTheme
import com.yashwant.agymobile.ui.util.BrowserHelper
import com.yashwant.agymobile.viewmodel.AgyViewModel
import kotlinx.coroutines.launch
import java.io.File
import java.util.Locale

class MainActivity : ComponentActivity() {

    private val viewModel: AgyViewModel by viewModels()
    private var boundService: AgyAgentService? = null
    private var isBound = false
    private var speechRecognizer: SpeechRecognizer? = null

    private val openDocumentTreeLauncher = registerForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.OpenDocumentTree()
    ) { uri: android.net.Uri? ->
        if (uri != null) {
            handleSelectedFolderUri(uri)
        }
    }

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
        androidx.core.view.WindowCompat.setDecorFitsSystemWindows(window, false)

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
                val selectedModel by viewModel.selectedModel.collectAsState()
                val selectedEffort by viewModel.selectedEffort.collectAsState()
                val engineLogs by viewModel.engineLogs.collectAsState()

                val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
                val coroutineScope = rememberCoroutineScope()
                var showFolderPickerModal by remember { mutableStateOf(false) }

                var hasStorage by remember { mutableStateOf(hasStoragePermission()) }
                val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
                DisposableEffect(lifecycleOwner) {
                    val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
                        if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                            hasStorage = hasStoragePermission()
                        }
                    }
                    lifecycleOwner.lifecycle.addObserver(observer)
                    onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
                }

                val workspaceDir = File(workspaceInfo.currentPath)
                val workspaceName = WorkspaceManager.getDisplayName(workspaceDir)

                ModalNavigationDrawer(
                    drawerState = drawerState,
                    drawerContent = {
                        val closeDrawer: () -> Unit = { coroutineScope.launch { drawerState.close() } }
                        ChatGptDrawer(
                            workspaceInfo = workspaceInfo,
                            conversations = conversations,
                            onSelectDirectory = { dir ->
                                if (WorkspaceManager.isExternalStoragePath(dir.absolutePath) && !hasStoragePermission()) {
                                    requestStoragePermission()
                                }
                                viewModel.selectWorkspace(dir, notify = true)
                                closeDrawer()
                            },
                            onCreateProject = { name ->
                                viewModel.createProject(name)
                                closeDrawer()
                            },
                            onResumeConversation = { id ->
                                viewModel.resumeConversation(id)
                                closeDrawer()
                            },
                            onNewChat = {
                                viewModel.clearChat()
                                closeDrawer()
                            },
                            authState = authState,
                            onOpenAuth = {
                                viewModel.openAuthSheet()
                                closeDrawer()
                            },
                            onSignOut = { viewModel.signOut() },
                            onCloseDrawer = closeDrawer,
                            onOpenFolderPicker = {
                                closeDrawer()
                                showFolderPickerModal = true
                            },
                            deviceShortcuts = viewModel.getDeviceShortcuts(),
                            onDeleteConversation = { id -> viewModel.deleteConversation(id) },
                            engineLogs = engineLogs,
                            onClearEngineLogs = { viewModel.clearEngineLogs() }
                        )
                    }
                ) {
                    ChatScreen(
                        messages = messages,
                        promptText = promptText,
                        onPromptChange = { viewModel.onPromptChange(it) },
                        isRunning = isRunning,
                        isAutonomous = isAutonomous,
                        onToggleAutonomous = { viewModel.toggleAutonomous() },
                        workspaceName = workspaceName,
                        gitBranch = workspaceInfo.gitBranch,
                        onOpenMenu = { coroutineScope.launch { drawerState.open() } },
                        onSend = { viewModel.sendPrompt(boundService) },
                        onCancel = { viewModel.cancel(boundService) },
                        onApprove = { toolId -> viewModel.resolveApproval(toolId, true, boundService) },
                        onDeny = { toolId -> viewModel.resolveApproval(toolId, false, boundService) },
                        onVoiceClick = { toggleVoiceDictation() },
                        isVoiceRecording = isVoiceRecording,
                        authState = authState,
                        onOpenAuth = { viewModel.openAuthSheet() },
                        selectedModel = selectedModel,
                        onSelectModel = { viewModel.selectModel(it) },
                        selectedEffort = selectedEffort,
                        onSelectEffort = { viewModel.setEffort(it) },
                        onClearChat = { viewModel.clearChat() },
                        hasStoragePermission = hasStorage,
                        onRequestStoragePermission = { requestStoragePermission() },
                        onOpenFolderPicker = { showFolderPickerModal = true }
                    )
                }

                if (showAuthSheet) {
                    AuthBottomSheet(
                        authState = authState,
                        onDismiss = { viewModel.dismissAuthSheet() },
                        onStartLogin = { viewModel.startLogin() },
                        onLaunchBrowser = { url -> BrowserHelper.openUrl(this@MainActivity, url) },
                        onSubmitCode = { code -> viewModel.submitAuthCode(code) },
                        onSaveApiKey = { key -> viewModel.saveApiKey(key) },
                        onImportJson = { json -> viewModel.importCredentials(json) },
                        onSignOut = { viewModel.signOut() }
                    )
                }

                if (showFolderPickerModal) {
                    DeviceFolderPickerModal(
                        initialDirectory = workspaceDir,
                        onSelectDirectory = { dir ->
                            if (WorkspaceManager.isExternalStoragePath(dir.absolutePath) && !hasStoragePermission()) {
                                requestStoragePermission()
                            }
                            viewModel.selectWorkspace(dir, notify = true)
                            showFolderPickerModal = false
                        },
                        onDismiss = { showFolderPickerModal = false },
                        hasStoragePermission = hasStorage,
                        onRequestStoragePermission = { requestStoragePermission() },
                        deviceShortcuts = viewModel.getDeviceShortcuts()
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

    fun hasStoragePermission(): Boolean {
        return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            android.os.Environment.isExternalStorageManager()
        } else {
            androidx.core.content.ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.READ_EXTERNAL_STORAGE
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        }
    }

    fun requestStoragePermission() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            try {
                val intent = Intent(android.provider.Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION).apply {
                    data = android.net.Uri.parse("package:$packageName")
                }
                startActivity(intent)
            } catch (_: Exception) {
                val fallbackIntent = Intent(android.provider.Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
                startActivity(fallbackIntent)
            }
        } else {
            androidx.core.app.ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    android.Manifest.permission.READ_EXTERNAL_STORAGE,
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE
                ),
                1001
            )
        }
    }

    fun resolveTreeUriToPath(uri: android.net.Uri): String? {
        try {
            contentResolver.takePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            )
        } catch (_: Exception) {}

        val docId = try {
            android.provider.DocumentsContract.getTreeDocumentId(uri)
        } catch (_: Exception) {
            null
        }

        val fromDocId = WorkspaceManager.resolveTreeDocIdToPath(docId)
        if (fromDocId != null) return fromDocId

        val rawPath = uri.path
        if (rawPath != null && rawPath.contains("primary:")) {
            val sub = rawPath.substringAfter("primary:")
            val root = WorkspaceManager.getPrimaryStoragePath()
            return if (sub.isBlank()) root else "$root/$sub"
        }

        return null
    }

    private fun handleSelectedFolderUri(uri: android.net.Uri) {
        val resolvedPath = resolveTreeUriToPath(uri)
        if (resolvedPath != null) {
            val folder = File(resolvedPath)
            if (!hasStoragePermission()) {
                requestStoragePermission()
            }
            viewModel.selectWorkspace(folder)
        } else {
            val fallback = android.os.Environment.getExternalStorageDirectory()
            viewModel.selectWorkspace(fallback)
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
