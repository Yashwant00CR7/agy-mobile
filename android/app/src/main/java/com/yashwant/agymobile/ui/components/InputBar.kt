package com.yashwant.agymobile.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun InputBar(
    promptText: String,
    onPromptChange: (String) -> Unit,
    isRunning: Boolean,
    onSend: () -> Unit,
    onCancel: () -> Unit,
    onVoiceClick: () -> Unit,
    isVoiceRecording: Boolean,
    onChipClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    ChatGptComposer(
        promptText = promptText,
        onPromptChange = onPromptChange,
        isRunning = isRunning,
        onSend = onSend,
        onCancel = onCancel,
        onVoiceClick = onVoiceClick,
        isVoiceRecording = isVoiceRecording,
        onQuickAction = onChipClick,
        modifier = modifier
    )
}
