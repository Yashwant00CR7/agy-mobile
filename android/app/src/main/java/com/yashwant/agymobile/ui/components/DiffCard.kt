package com.yashwant.agymobile.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Difference
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yashwant.agymobile.ui.theme.*

@Composable
fun DiffCard(
    fileName: String,
    diffText: String,
    modifier: Modifier = Modifier
) {
    val horizontalScroll = rememberScrollState()

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(ObsidianBlack)
            .border(1.dp, ObsidianBorder, RoundedCornerShape(12.dp))
    ) {
        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .background(ObsidianElevated)
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Difference,
                    contentDescription = "Diff",
                    tint = ElectricCyan,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = fileName,
                    color = TextPrimary,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
                    .horizontalScroll(horizontalScroll)
            ) {
                val lines = diffText.lines()
                lines.forEach { line ->
                    val (bgColor, textColor) = when {
                        line.startsWith("+") -> Pair(Color(0x1A10B981), EmeraldSuccess)
                        line.startsWith("-") -> Pair(Color(0x1AEF4444), CrimsonDanger)
                        line.startsWith("@@") -> Pair(Color(0x1A8B5CF6), AntigravityViolet)
                        else -> Pair(Color.Transparent, TextSecondary)
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(bgColor)
                            .padding(horizontal = 4.dp, vertical = 1.dp)
                    ) {
                        Text(
                            text = line,
                            color = textColor,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            lineHeight = 15.sp
                        )
                    }
                }
            }
        }
    }
}
