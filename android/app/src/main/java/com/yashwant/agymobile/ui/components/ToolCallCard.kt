package com.yashwant.agymobile.ui.components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yashwant.agymobile.ui.theme.*
import org.json.JSONObject

@Composable
fun ToolCallCard(
    toolName: String,
    argsJson: String,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(false) }
    val displayLabel = remember(toolName, argsJson) {
        formatToolDisplayLabel(toolName, argsJson)
    }

    Column(
        modifier = modifier
            .animateContentSize()
            .padding(vertical = 2.dp)
    ) {
        // Sleek Collapsible Tool Pill
        Surface(
            onClick = { isExpanded = !isExpanded },
            color = AntigravitySurface,
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
            ) {
                Icon(
                    imageVector = AgyIcons.TerminalSpark,
                    contentDescription = null,
                    tint = AntigravityCyan,
                    modifier = Modifier.size(13.dp)
                )
                Text(
                    text = displayLabel,
                    color = TextPrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
                Icon(
                    imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = TextMuted,
                    modifier = Modifier.size(14.dp)
                )
            }
        }

        // Expanded Output / JSON arguments
        if (isExpanded) {
            Spacer(modifier = Modifier.height(6.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(AntigravityDarkBg)
                    .padding(12.dp)
            ) {
                val scrollState = rememberScrollState()
                Text(
                    text = formatPrettyJson(argsJson),
                    color = TextSecondary,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp,
                    lineHeight = 16.sp,
                    modifier = Modifier.horizontalScroll(scrollState)
                )
            }
        }
    }
}

private fun formatToolDisplayLabel(name: String, args: String): String {
    return try {
        if (name.contains("bash", ignoreCase = true) || name.contains("command", ignoreCase = true)) {
            val json = JSONObject(args)
            val cmd = json.optString("command", json.optString("cmd", ""))
            if (cmd.isNotBlank()) "Ran bash: $cmd" else "Ran bash command"
        } else {
            "Used tool: $name"
        }
    } catch (_: Exception) {
        "Used tool: $name"
    }
}

private fun formatPrettyJson(raw: String): String {
    return try {
        val obj = JSONObject(raw)
        obj.toString(2)
    } catch (_: Exception) {
        raw
    }
}
