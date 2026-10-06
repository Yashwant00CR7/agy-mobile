package com.yashwant.agymobile.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yashwant.agymobile.model.AuthState
import com.yashwant.agymobile.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthBottomSheet(
    authState: AuthState,
    onDismiss: () -> Unit,
    onStartLogin: () -> Unit,
    onLaunchBrowser: (String) -> Unit,
    onSubmitCode: (String) -> Unit,
    onImportJson: (String) -> Unit,
    onSignOut: () -> Unit,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
) {
    val context = LocalContext.current
    var authCodeInput by remember { mutableStateOf("") }
    var rawJsonInput by remember { mutableStateOf("") }
    var showJsonImport by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = ObsidianSurface,
        scrimColor = Color.Black.copy(alpha = 0.65f),
        dragHandle = {
            BottomSheetDefaults.DragHandle(color = ObsidianBorder)
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (authState.isAuthenticated) EmeraldGlow else CyanGlow),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (authState.isAuthenticated) Icons.Default.CheckCircle else Icons.Default.AccountCircle,
                        contentDescription = "Auth Status",
                        tint = if (authState.isAuthenticated) EmeraldSuccess else ElectricCyan,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (authState.isAuthenticated) "Google Account Connected" else "Sign In with Google",
                        color = TextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (authState.isAuthenticated) {
                            authState.userEmail ?: "Active Session"
                        } else {
                            "Required to run Antigravity agent natively"
                        },
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                }
            }

            // Error Display
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
                // Logged In Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, EmeraldSuccess.copy(alpha = 0.4f), RoundedCornerShape(12.dp)),
                    colors = CardDefaults.cardColors(containerColor = ObsidianBlack)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Connected Account",
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                        Text(
                            text = authState.userEmail ?: "Google User",
                            color = EmeraldSuccess,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Stored securely in app sandbox ~/.gemini/oauth_creds.json",
                            color = TextMuted,
                            fontSize = 11.sp
                        )
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
                    Text(text = "Sign Out", fontWeight = FontWeight.Bold)
                }
            } else {
                // Unauthenticated Login Steps

                // Step 1: Launch Browser
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
                            text = "Step 1: Open Google Sign-In",
                            color = TextPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Open Chrome to approve Google access and copy the authorization code.",
                            color = TextSecondary,
                            fontSize = 12.sp
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
                                Text(text = "Launch Sign-In Browser", fontWeight = FontWeight.Bold)
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
                                    Text(text = "Generating Auth Link...", fontSize = 13.sp)
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

                // Step 2: Code Entry
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
                            text = "Step 2: Submit Authorization Code",
                            color = TextPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )

                        OutlinedTextField(
                            value = authCodeInput,
                            onValueChange = { authCodeInput = it },
                            placeholder = { Text("Paste authorization code (4/0A...)", color = TextMuted, fontSize = 12.sp) },
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

                // Advanced / Fallback Section
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
                        text = if (showJsonImport) "Hide Manual Token Import" else "Advanced: Import oauth_creds.json directly",
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
                            Text(
                                text = "Paste the contents of an existing ~/.gemini/oauth_creds.json file:",
                                color = TextMuted,
                                fontSize = 11.sp
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
                                Text(text = "Save & Apply Credentials", fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
