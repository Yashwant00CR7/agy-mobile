package com.yashwant.agymobile.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Difference
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
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

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = AntigravityDarkBg,
        shape = RoundedCornerShape(12.dp)
    ) {
        Column {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .background(AntigravitySurface)
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Difference,
                    contentDescription = "Diff",
                    tint = AntigravityCyan,
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

            // Diff Body
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(horizontalScroll)
                    .padding(10.dp)
            ) {
                val lines = diffText.lines()
                lines.forEach { line ->
                    val textColor = when {
                        line.startsWith("+") -> EmeraldSuccess
                        line.startsWith("-") -> CrimsonDanger
                        line.startsWith("@@") -> AntigravityCyan
                        else -> TextSecondary
                    }
                    val lineBg = when {
                        line.startsWith("+") -> Color(0x1510B981)
                        line.startsWith("-") -> Color(0x15EF4444)
                        else -> Color.Transparent
                    }

                    Text(
                        text = line,
                        color = textColor,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        lineHeight = 15.sp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(lineBg)
                            .padding(horizontal = 4.dp, vertical = 1.dp)
                    )
                }
            }
        }
    }
}
