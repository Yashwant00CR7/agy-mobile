package com.yashwant.agymobile.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

object AgyIcons {
    /**
     * Antigravity Official Chevron Delta Logo Mark
     * An inverted aerodynamic geometric chevron representing levitation & antigravity.
     */
    val Logo: ImageVector by lazy {
        ImageVector.Builder(
            name = "AgyLogo",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                fill = SolidColor(Color(0xFF00E5FF)),
                pathFillType = PathFillType.EvenOdd
            ) {
                moveTo(12f, 2f)
                lineTo(22f, 18f)
                lineTo(17.5f, 18f)
                lineTo(12f, 8.5f)
                lineTo(6.5f, 18f)
                lineTo(2f, 18f)
                close()
            }
            path(
                fill = SolidColor(Color(0xFF8B5CF6))
            ) {
                moveTo(12f, 12f)
                lineTo(15.5f, 18f)
                lineTo(8.5f, 18f)
                close()
            }
        }.build()
    }

    /**
     * Terminal Spark Icon
     */
    val TerminalSpark: ImageVector by lazy {
        ImageVector.Builder(
            name = "TerminalSpark",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                stroke = SolidColor(Color(0xFF00E5FF)),
                strokeLineWidth = 2.2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(4f, 7f)
                lineTo(10f, 12f)
                lineTo(4f, 17f)
            }
            path(
                stroke = SolidColor(Color(0xFF8B5CF6)),
                strokeLineWidth = 2.2f,
                strokeLineCap = StrokeCap.Round
            ) {
                moveTo(13f, 17f)
                lineTo(20f, 17f)
            }
        }.build()
    }

    /**
     * Futuristic Send Plane
     */
    val SendPlane: ImageVector by lazy {
        ImageVector.Builder(
            name = "SendPlane",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                fill = SolidColor(Color(0xFF080C14))
            ) {
                moveTo(2.01f, 21f)
                lineTo(23f, 12f)
                lineTo(2.01f, 3f)
                lineTo(2f, 10f)
                lineTo(17f, 12f)
                lineTo(2f, 14f)
                close()
            }
        }.build()
    }

    /**
     * Cyber Key Security
     */
    val CyberKey: ImageVector by lazy {
        ImageVector.Builder(
            name = "CyberKey",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                stroke = SolidColor(Color(0xFF8B5CF6)),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(15f, 7f)
                arcTo(4f, 4f, 0f, true, true, 12f, 10.87f)
                lineTo(7f, 16f)
                lineTo(7f, 19f)
                lineTo(4f, 19f)
                lineTo(4f, 16f)
                lineTo(8.13f, 11.87f)
            }
        }.build()
    }
}
