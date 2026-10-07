package com.yashwant.agymobile.ui.components

import android.content.ClipboardManager
import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yashwant.agymobile.model.AuthState
import com.yashwant.agymobile.ui.theme.*

enum class AuthTab {
    GOOGLE_OAUTH,
    GEMINI_API_KEY
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthBottomSheet(
    authState: AuthState,
    onDismiss: () -> Unit,
    onStartLogin: () -> Unit,
    onLaunchBrowser: (String) -> Unit,
    onSubmitCode: (String) -> Unit,
    onSaveApiKey: (String) -> Unit = {},
    onImportJson: (String) -> Unit,
    onSignOut: () -> Unit,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableStateOf(AuthTab.GOOGLE_OAUTH) }
    var authCodeInput by remember { mutableStateOf("") }
    var apiKeyInput by remember { mutableStateOf("") }
    var showApiKey by remember { mutableStateOf(false) }
    var rawJsonInput by remember { mutableStateOf("") }
    var showJsonImport by remember { mutableStateOf(false) }

    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                try {
                    val clip = (context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager)
                        ?.primaryClip
                    val text = clip?.getItemAt(0)?.text?.toString()?.trim()
                    if (!text.isNullOrBlank()) {
                        if (text.startsWith("4/0A") || text.contains("code=")) {
                            authCodeInput = text
                        } else if (text.startsWith("AIzaSy")) {
                            apiKeyInput = text
                            selectedTab = AuthTab.GEMINI_API_KEY
                        }
                    }
                } catch (_: Exception) {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = ObsidianSurface,
        scrimColor = Color.Black.copy(alpha = 0.70f),
        dragHandle = {
            BottomSheetDefaults.DragHandle(color = ObsidianBorder)
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 6.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Antigravity Brand Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(
                            Brush.linearGradient(
                                if (authState.isAuthenticated) listOf(EmeraldSuccess, Color(0xFF047857))
                                else listOf(ElectricCyan, AntigravityViolet)
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (authState.isAuthenticated) Icons.Default.CheckCircle else Icons.Default.VpnKey,
                        contentDescription = "Auth Status",
                        tint = ObsidianBlack,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (authState.isAuthenticated) "Agent Connected" else "Antigravity Authentication",
                        color = TextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (authState.isAuthenticated) {
                            authState.userEmail ?: "Active Session"
                        } else {
                            "Choose an authentication method to power libagy"
                        },
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                }
            }

            // Error Display Card
            if (authState.error != null) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, CrimsonDanger.copy(alpha = 0.5f), RoundedCornerShape(12.dp)),
                    colors = CardDefaults.cardColors(containerColor = ObsidianBlack)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Error",
                            tint = CrimsonDanger,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = authState.error,
                            color = CrimsonDanger,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            if (authState.isAuthenticated) {
                // Logged In Status Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, EmeraldSuccess.copy(alpha = 0.5f), RoundedCornerShape(14.dp)),
                    colors = CardDefaults.cardColors(containerColor = ObsidianBlack)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(EmeraldSuccess)
                            )
                            Text(
                                text = "ACTIVE SESSION",
                                color = EmeraldSuccess,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                        }

                        Text(
                            text = authState.userEmail ?: "Active Account",
                            color = TextPrimary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )

                        Text(
                            text = "Engine is authenticated. For instant, unrestricted native execution on Android, you can also attach a Gemini API key.",
                            color = TextSecondary,
                            fontSize = 12.sp,
                            lineHeight = 17.sp
                        )
                    }
                }

                var showKeyEditorInActiveSession by remember { mutableStateOf(false) }

                if (showKeyEditorInActiveSession) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, AntigravityViolet.copy(alpha = 0.5f), RoundedCornerShape(12.dp)),
                        colors = CardDefaults.cardColors(containerColor = ObsidianBlack)
                    ) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text("Attach Gemini API Key", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            OutlinedTextField(
                                value = apiKeyInput,
                                onValueChange = { apiKeyInput = it },
                                placeholder = { Text("AIzaSy...", color = TextMuted, fontSize = 12.sp) },
                                singleLine = true,
                                visualTransformation = if (showApiKey) VisualTransformation.None else PasswordVisualTransformation(),
                                textStyle = LocalTextStyle.current.copy(color = TextPrimary, fontFamily = FontFamily.Monospace, fontSize = 13.sp),
                                trailingIcon = {
                                    IconButton(onClick = { showApiKey = !showApiKey }) {
                                        Icon(
                                            imageVector = if (showApiKey) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                            contentDescription = null,
                                            tint = TextSecondary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                },
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                                Button(
                                    onClick = {
                                        onSaveApiKey(apiKeyInput.trim())
                                        showKeyEditorInActiveSession = false
                                    },
                                    enabled = apiKeyInput.isNotBlank(),
                                    colors = ButtonDefaults.buttonColors(containerColor = AntigravityViolet),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("Save Key", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                                OutlinedButton(
                                    onClick = { onLaunchBrowser("https://aistudio.google.com/app/apikey") },
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("Get Free Key", fontSize = 12.sp)
                                }
                            }
                        }
                    }
                } else {
                    OutlinedButton(
                        onClick = { showKeyEditorInActiveSession = true },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = ElectricCyan),
                        border = androidx.compose.foundation.BorderStroke(1.dp, ElectricCyan.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(imageVector = Icons.Default.Key, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "Configure / Switch Gemini API Key", fontWeight = FontWeight.SemiBold)
                    }
                }

                Button(
                    onClick = onSignOut,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CrimsonDanger.copy(alpha = 0.15f),
                        contentColor = CrimsonDanger
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.ExitToApp,
                        contentDescription = "Sign Out",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Disconnect & Sign Out", fontWeight = FontWeight.Bold)
                }
            } else {
                // Dual Mode Tab Selector
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(ObsidianBlack)
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Google OAuth Tab
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (selectedTab == AuthTab.GOOGLE_OAUTH) ObsidianElevated else Color.Transparent)
                            .border(
                                width = if (selectedTab == AuthTab.GOOGLE_OAUTH) 1.dp else 0.dp,
                                color = if (selectedTab == AuthTab.GOOGLE_OAUTH) ElectricCyan else Color.Transparent,
                                shape = RoundedCornerShape(10.dp)
                            )
                            .clickable { selectedTab = AuthTab.GOOGLE_OAUTH }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccountCircle,
                                contentDescription = "Google",
                                tint = if (selectedTab == AuthTab.GOOGLE_OAUTH) ElectricCyan else TextMuted,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "Google Account",
                                color = if (selectedTab == AuthTab.GOOGLE_OAUTH) TextPrimary else TextMuted,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    // Gemini API Key Tab
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (selectedTab == AuthTab.GEMINI_API_KEY) ObsidianElevated else Color.Transparent)
                            .border(
                                width = if (selectedTab == AuthTab.GEMINI_API_KEY) 1.dp else 0.dp,
                                color = if (selectedTab == AuthTab.GEMINI_API_KEY) AntigravityViolet else Color.Transparent,
                                shape = RoundedCornerShape(10.dp)
                            )
                            .clickable { selectedTab = AuthTab.GEMINI_API_KEY }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Key,
                                contentDescription = "API Key",
                                tint = if (selectedTab == AuthTab.GEMINI_API_KEY) AntigravityViolet else TextMuted,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "Gemini API Key",
                                color = if (selectedTab == AuthTab.GEMINI_API_KEY) TextPrimary else TextMuted,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                // TAB 1: GOOGLE OAUTH
                if (selectedTab == AuthTab.GOOGLE_OAUTH) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, ObsidianBorder, RoundedCornerShape(14.dp)),
                        colors = CardDefaults.cardColors(containerColor = ObsidianBlack)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "Step 1: Open Google Sign-In",
                                color = TextPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Authorizes official Antigravity scopes (Code Assist, Cloud Platform, experiments).",
                                color = TextSecondary,
                                fontSize = 12.sp,
                                lineHeight = 16.sp
                            )

                            if (authState.authUrl != null) {
                                Button(
                                    onClick = { onLaunchBrowser(authState.authUrl) },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = ElectricCyan,
                                        contentColor = ObsidianBlack
                                    ),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.OpenInBrowser,
                                        contentDescription = "Open Browser",
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(text = "Launch Sign-In in Chrome", fontWeight = FontWeight.Bold)
                                }
                            } else {
                                Button(
                                    onClick = onStartLogin,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = ObsidianElevated,
                                        contentColor = ElectricCyan
                                    ),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    if (authState.isAuthenticating) {
                                        CircularProgressIndicator(
                                            color = ElectricCyan,
                                            modifier = Modifier.size(16.dp),
                                            strokeWidth = 2.dp
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(text = "Generating Official Auth Link...", fontSize = 13.sp)
                                    } else {
                                        Icon(
                                            imageVector = Icons.Default.PlayArrow,
                                            contentDescription = "Start",
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(text = "Generate Sign-In Link", fontWeight = FontWeight.SemiBold)
                                    }
                                }
                            }
                        }
                    }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, ObsidianBorder, RoundedCornerShape(14.dp)),
                        colors = CardDefaults.cardColors(containerColor = ObsidianBlack)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "Step 2: Submit Authorization Code",
                                color = TextPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )

                            OutlinedTextField(
                                value = authCodeInput,
                                onValueChange = { authCodeInput = it },
                                placeholder = { Text("Paste code (4/0A...) or full URL", color = TextMuted, fontSize = 12.sp) },
                                singleLine = true,
                                textStyle = LocalTextStyle.current.copy(
                                    color = TextPrimary,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 13.sp
                                ),
                                trailingIcon = {
                                    IconButton(onClick = {
                                        val clip = (context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager)
                                            ?.primaryClip
                                        val text = clip?.getItemAt(0)?.text?.toString()
                                        if (!text.isNullOrBlank()) {
                                            authCodeInput = text.trim()
                                        }
                                    }) {
                                        Icon(
                                            imageVector = Icons.Default.ContentPaste,
                                            contentDescription = "Paste",
                                            tint = ElectricCyan,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = ElectricCyan,
                                    unfocusedBorderColor = ObsidianBorder,
                                    focusedContainerColor = ObsidianSurface,
                                    unfocusedContainerColor = ObsidianSurface
                                ),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            )

                            Button(
                                onClick = { onSubmitCode(authCodeInput.trim()) },
                                enabled = authCodeInput.isNotBlank(),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = AntigravityViolet,
                                    contentColor = Color.White
                                ),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Complete",
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(text = "Complete Sign-In", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // TAB 2: GEMINI API KEY
                if (selectedTab == AuthTab.GEMINI_API_KEY) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, AntigravityViolet.copy(alpha = 0.4f), RoundedCornerShape(14.dp)),
                        colors = CardDefaults.cardColors(containerColor = ObsidianBlack)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Bolt,
                                    contentDescription = "Direct Mode",
                                    tint = AntigravityViolet,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = "Direct Gemini API Key (Bypass OAuth)",
                                    color = TextPrimary,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }

                            Text(
                                text = "Exports GEMINI_API_KEY and configures modelProvider: \"gemini\" in settings.json. Bypasses browser login and runs directly on phone with 1,500 free requests/day.",
                                color = TextSecondary,
                                fontSize = 12.sp,
                                lineHeight = 16.sp
                            )

                            OutlinedButton(
                                onClick = { onLaunchBrowser("https://aistudio.google.com/app/apikey") },
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = ElectricCyan),
                                border = androidx.compose.foundation.BorderStroke(1.dp, ElectricCyan.copy(alpha = 0.5f)),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(imageVector = Icons.Default.OpenInNew, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Get Free Key from Google AI Studio", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            }

                            OutlinedTextField(
                                value = apiKeyInput,
                                onValueChange = { apiKeyInput = it },
                                placeholder = { Text("Paste AI Studio API Key (AIzaSy...)", color = TextMuted, fontSize = 12.sp) },
                                singleLine = true,
                                visualTransformation = if (showApiKey) VisualTransformation.None else PasswordVisualTransformation(),
                                textStyle = LocalTextStyle.current.copy(
                                    color = TextPrimary,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 13.sp
                                ),
                                trailingIcon = {
                                    Row {
                                        IconButton(onClick = { showApiKey = !showApiKey }) {
                                            Icon(
                                                imageVector = if (showApiKey) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                                contentDescription = "Toggle visibility",
                                                tint = TextSecondary,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                        IconButton(onClick = {
                                            val clip = (context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager)
                                                ?.primaryClip
                                            val text = clip?.getItemAt(0)?.text?.toString()
                                            if (!text.isNullOrBlank()) {
                                                apiKeyInput = text.trim()
                                            }
                                        }) {
                                            Icon(
                                                imageVector = Icons.Default.ContentPaste,
                                                contentDescription = "Paste",
                                                tint = AntigravityViolet,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = AntigravityViolet,
                                    unfocusedBorderColor = ObsidianBorder,
                                    focusedContainerColor = ObsidianSurface,
                                    unfocusedContainerColor = ObsidianSurface
                                ),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            )

                            Button(
                                onClick = { onSaveApiKey(apiKeyInput.trim()) },
                                enabled = apiKeyInput.isNotBlank(),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = AntigravityViolet,
                                    contentColor = Color.White
                                ),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Activate",
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(text = "Save & Activate API Key", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // Advanced JSON Import Accordion
                TextButton(
                    onClick = { showJsonImport = !showJsonImport },
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    Icon(
                        imageVector = if (showJsonImport) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = "Toggle",
                        tint = TextSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (showJsonImport) "Hide Raw JSON Import" else "Advanced: Import raw oauth_creds.json",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                }

                AnimatedVisibility(visible = showJsonImport) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, ObsidianBorder, RoundedCornerShape(12.dp)),
                        colors = CardDefaults.cardColors(containerColor = ObsidianBlack)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                text = "Paste Credentials JSON",
                                color = TextPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )

                            OutlinedTextField(
                                value = rawJsonInput,
                                onValueChange = { rawJsonInput = it },
                                placeholder = { Text("{\"access_token\": \"...\", \"refresh_token\": \"...\"}", color = TextMuted, fontSize = 11.sp) },
                                minLines = 3,
                                maxLines = 6,
                                textStyle = LocalTextStyle.current.copy(
                                    color = TextPrimary,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 11.sp
                                ),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = ElectricCyan,
                                    unfocusedBorderColor = ObsidianBorder,
                                    focusedContainerColor = ObsidianSurface,
                                    unfocusedContainerColor = ObsidianSurface
                                ),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            )

                            Button(
                                onClick = { onImportJson(rawJsonInput.trim()) },
                                enabled = rawJsonInput.isNotBlank(),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = ObsidianElevated,
                                    contentColor = ElectricCyan
                                ),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(text = "Save Credentials", fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
