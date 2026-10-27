package com.yashwant.agymobile.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
    QuickAction("Device UI Inspection", "inspect on-screen UI elements and summarize", "Read UI hierarchy via Accessibility Service"),
    QuickAction("Git Status", "git status", "Inspect uncommitted files and branch state"),
    QuickAction("Review Diffs", "review current diffs", "Run Standards and Spec code review"),
    QuickAction("Run Tests", "run project tests", "Execute automated test suite"),
    QuickAction("Explain Architecture", "summarize codebase architecture and entry points", "Scan workspace structure and entry points")
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
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Subtle Antigravity Brand Emblem
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(AntigravitySurfaceElevated),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = AgyIcons.Logo,
                contentDescription = "Antigravity",
                tint = Color.Unspecified,
                modifier = Modifier.size(26.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Greeting Header
        Text(
            text = "What can I help you build?",
            color = TextPrimary,
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.height(6.dp))

        // Subtle model & workspace context badge
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = selectedModel.substringBefore(" (").ifBlank { "Gemini Flash" },
                color = AntigravityBlue,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.clickable { onOpenModelPicker() }
            )
            Text(text = "•", color = TextMuted, fontSize = 12.sp)
            Text(
                text = workspacePath.ifBlank { "workspace" },
                color = TextSecondary,
                fontSize = 12.sp
            )
        }

        if (!authState.isAuthenticated) {
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "⚡ Tap to connect Gemini API key or sign in",
                color = AmberWarning,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.clickable { onOpenAuth() }
            )
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Clean Suggestion Prompt Cards (2x2 Grid)
        Column(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            QuickActions.chunked(2).forEach { rowItems ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    rowItems.forEach { item ->
                        Surface(
                            onClick = { onSelectPrompt(item.prompt) },
                            color = AntigravitySurface,
                            shape = RoundedCornerShape(14.dp),
                            border = androidx.compose.foundation.BorderStroke(0.5.dp, AntigravityBorder),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = item.title,
                                    color = TextPrimary,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = item.description,
                                    color = TextSecondary,
                                    fontSize = 11.sp,
                                    lineHeight = 15.sp,
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
