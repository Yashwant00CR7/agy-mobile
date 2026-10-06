package com.yashwant.agymobile.ui.components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yashwant.agymobile.ui.theme.*

@Composable
fun ThoughtCard(
    thoughtText: String,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxWidth(0.92f)
            .clip(RoundedCornerShape(12.dp))
            .background(ObsidianSurface)
            .border(1.dp, VioletGlow, RoundedCornerShape(12.dp))
            .clickable { isExpanded = !isExpanded }
            .animateContentSize()
            .padding(12.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = "Thinking",
                        tint = AntigravityViolet,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "Agent Reasoning",
                        color = AntigravityViolet,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Icon(
                    imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = "Toggle",
                    tint = TextMuted,
                    modifier = Modifier.size(16.dp)
                )
            }

            if (isExpanded) {
                Text(
                    text = thoughtText,
                    color = TextSecondary,
                    fontSize = 13.sp,
                    fontStyle = FontStyle.Italic,
                    lineHeight = 18.sp
                )
            } else {
                Text(
                    text = thoughtText.take(120).replace("\n", " ") + if (thoughtText.length > 120) "..." else "",
                    color = TextMuted,
                    fontSize = 12.sp,
                    fontStyle = FontStyle.Italic,
                    maxLines = 2
                )
            }
        }
    }
}
