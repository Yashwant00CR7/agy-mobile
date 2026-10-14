package com.yashwant.agymobile.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yashwant.agymobile.core.compiler.CompilerResult
import com.yashwant.agymobile.ui.theme.*

@Composable
fun ApkInstallCard(
    result: CompilerResult,
    onInstall: () -> Unit,
    onShare: () -> Unit,
    onSaveDownloads: () -> Unit,
    onFixWithAi: ((String) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        color = AntigravitySurface,
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, if (result.success) EmeraldSuccess.copy(alpha = 0.5f) else CrimsonDanger.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header: Icon + Titles + Success Badge
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (result.success) EmeraldGlow else CrimsonGlow),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (result.success) Icons.Default.Android else Icons.Default.Warning,
                        contentDescription = null,
                        tint = if (result.success) EmeraldSuccess else CrimsonDanger,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = result.appName.ifBlank { "Compiled APK" },
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = if (result.success) EmeraldSuccess.copy(alpha = 0.2f) else CrimsonDanger.copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = if (result.success) "READY TO INSTALL" else "BUILD FAILED",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (result.success) EmeraldSuccess else CrimsonDanger,
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Text(
                        text = result.packageName.ifBlank { "com.example.app" },
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        color = TextSecondary
                    )
                }
            }

            // Stats Row: Version • Size • Duration
            Row(
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(AntigravitySurfaceElevated)
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                StatItem(label = "Version", value = "v${result.versionName}")
                StatItem(label = "Size", value = if (result.sizeBytes > 0) "${result.sizeBytes / 1024} KB" else "N/A")
                StatItem(label = "Build Time", value = "${String.format("%.1f", result.durationMs / 1000.0)}s")
            }

            if (!result.success) {
                // Interactive Diagnostic Error Display
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(CrimsonDanger.copy(alpha = 0.1f))
                        .padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "Stage: ${result.failedStage?.name ?: "COMPILATION"}",
                        color = CrimsonDanger,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )

                    if (result.errors.isNotEmpty()) {
                        result.errors.take(4).forEach { err ->
                            Text(
                                text = "• ${err.file ?: "File"}:${err.line ?: "?"} => ${err.message}",
                                color = TextPrimary,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    } else {
                        Text(
                            text = result.errorMessage ?: "Unknown compilation error",
                            color = CrimsonDanger,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                if (onFixWithAi != null) {
                    Button(
                        onClick = {
                            val details = buildString {
                                appendLine("The on-device APK compilation failed with the following diagnostics:")
                                appendLine("Stage: ${result.failedStage?.name}")
                                if (!result.errorMessage.isNullOrBlank()) appendLine("Message: ${result.errorMessage}")
                                result.errors.forEach { err ->
                                    appendLine("- ${err.file ?: "File"}:${err.line ?: "?"} => ${err.message}")
                                }
                                appendLine("\nPlease inspect the errors above, fix the affected files in the workspace, and rebuild the APK.")
                            }
                            onFixWithAi(details)
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CrimsonDanger,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Build, contentDescription = null, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Ask AI to Fix & Recompile",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }
            } else {
                // Action Buttons
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Button(
                        onClick = onInstall,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = EmeraldSuccess,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                        modifier = Modifier.weight(1.3f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Download,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Install Now",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }

                    OutlinedButton(
                        onClick = onShare,
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = AntigravityCyan
                        ),
                        border = BorderStroke(1.dp, AntigravityBorder),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp),
                        modifier = Modifier.weight(0.9f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = null,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "Share", fontSize = 12.sp)
                    }

                    OutlinedButton(
                        onClick = onSaveDownloads,
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = TextSecondary
                        ),
                        border = BorderStroke(1.dp, AntigravityBorder),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.SaveAlt,
                            contentDescription = "Save to Downloads",
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StatItem(label: String, value: String) {
    Column {
        Text(text = label, fontSize = 9.sp, color = TextMuted)
        Text(text = value, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
    }
}
