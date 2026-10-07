package com.yashwant.agymobile.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yashwant.agymobile.model.AuthState
import com.yashwant.agymobile.ui.theme.*

data class QuickAction(
    val title: String,
    val prompt: String,
    val description: String
)

val QuickActions = listOf(
    QuickAction("git status", "git status", "Inspect uncommitted files and branch state"),
    QuickAction("review diffs", "review current diffs", "Run Standards and Spec review on recent changes"),
    QuickAction("run tests", "run project tests", "Execute test suite test-first"),
    QuickAction("analyze repo", "summarize codebase architecture and entry points", "Scan workspace structure and dependencies")
)

@Composable
fun WelcomeHero(
    authState: AuthState,
    selectedModel: String,
    workspacePath: String,
    onOpenModelPicker: () -> Unit,
    onOpenAuth: () -> Unit,
    onSelectPrompt: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Glowing Brand Delta Emblem
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(CyanGlow, Color.Transparent)
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(ObsidianElevated)
                    .border(1.5.dp, ElectricCyan, RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = AgyIcons.Logo,
                    contentDescription = "Antigravity",
                    tint = Color.Unspecified,
                    modifier = Modifier.size(32.dp)
                )
            }
        }

        // Title and Subtitle
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "ANTIGRAVITY",
                color = TextPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 2.sp
            )
            Text(
                text = "Autonomous Agent Engine • ARM64 Native",
                color = TextSecondary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
        }

        // Environment Status Matrix Card
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(ObsidianSurface)
                .border(1.dp, HairlineBorder, RoundedCornerShape(14.dp))
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Engine Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(EmeraldSuccess)
                    )
                    Text(
                        text = "Core Engine",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                }
                Text(
                    text = "libagy.so • Ready",
                    color = EmeraldSuccess,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Active Model Row (Clickable)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onOpenModelPicker() },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = AgyIcons.ModelChip,
                        contentDescription = null,
                        tint = ElectricCyan,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = "Active Model",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = selectedModel.substringBefore(" ("),
                        color = ElectricCyan,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Icon(
                        imageVector = AgyIcons.ChevronDown,
                        contentDescription = "Select",
                        tint = ElectricCyan,
                        modifier = Modifier.size(12.dp)
                    )
                }
            }

            // Auth Row (Clickable if not logged in)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onOpenAuth() },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = AgyIcons.CyberKey,
                        contentDescription = null,
                        tint = if (authState.isAuthenticated) EmeraldSuccess else AmberWarning,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = "Credentials",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                }
                Text(
                    text = authState.userEmail ?: if (authState.isAuthenticated) "Active" else "Tap to Sign In",
                    color = if (authState.isAuthenticated) TextPrimary else AmberWarning,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        // Quick Launch Actions Header
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = "SUGGESTED WORKFLOWS",
                color = TextMuted,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
        }

        // 2x2 Grid of Quick Actions
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            QuickActions.chunked(2).forEach { rowActions ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    rowActions.forEach { action ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(ObsidianElevated)
                                .border(1.dp, HairlineBorder, RoundedCornerShape(10.dp))
                                .clickable { onSelectPrompt(action.prompt) }
                                .padding(12.dp)
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PlayArrow,
                                        contentDescription = null,
                                        tint = ElectricCyan,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Text(
                                        text = action.title,
                                        color = ElectricCyan,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Text(
                                    text = action.description,
                                    color = TextSecondary,
                                    fontSize = 10.sp,
                                    lineHeight = 14.sp,
                                    maxLines = 2
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
