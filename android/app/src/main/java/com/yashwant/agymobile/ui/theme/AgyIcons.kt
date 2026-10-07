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

    /**
     * AI Neural Processor / Model Chip Icon
     */
    val ModelChip: ImageVector by lazy {
        ImageVector.Builder(
            name = "ModelChip",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                stroke = SolidColor(Color(0xFF00E5FF)),
                strokeLineWidth = 1.8f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(6f, 6f)
                lineTo(18f, 6f)
                lineTo(18f, 18f)
                lineTo(6f, 18f)
                close()
                moveTo(9f, 9f)
                lineTo(15f, 9f)
                lineTo(15f, 15f)
                lineTo(9f, 15f)
                close()
                // Pins
                moveTo(9f, 2f); lineTo(9f, 6f)
                moveTo(15f, 2f); lineTo(15f, 6f)
                moveTo(9f, 18f); lineTo(9f, 22f)
                moveTo(15f, 18f); lineTo(15f, 22f)
                moveTo(2f, 9f); lineTo(6f, 9f)
                moveTo(2f, 15f); lineTo(6f, 15f)
                moveTo(18f, 9f); lineTo(22f, 9f)
                moveTo(18f, 15f); lineTo(22f, 15f)
            }
        }.build()
    }

    /**
     * Mini Down Chevron
     */
    val ChevronDown: ImageVector by lazy {
        ImageVector.Builder(
            name = "ChevronDown",
            defaultWidth = 16.dp,
            defaultHeight = 16.dp,
            viewportWidth = 16f,
            viewportHeight = 16f
        ).apply {
            path(
                stroke = SolidColor(Color(0xFF94A3B8)),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(4f, 6f)
                lineTo(8f, 10f)
                lineTo(12f, 6f)
            }
        }.build()
    }

    /**
     * Git Branch Icon
     */
    val GitBranch: ImageVector by lazy {
        ImageVector.Builder(
            name = "GitBranch",
            defaultWidth = 16.dp,
            defaultHeight = 16.dp,
            viewportWidth = 16f,
            viewportHeight = 16f
        ).apply {
            path(
                stroke = SolidColor(Color(0xFF00E5FF)),
                strokeLineWidth = 1.6f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            ) {
                moveTo(5f, 4f); lineTo(5f, 12f)
                moveTo(5f, 7f); curveTo(5f, 9f, 11f, 8f, 11f, 10f); lineTo(11f, 12f)
            }
            path(fill = SolidColor(Color(0xFF00E5FF))) {
                moveTo(5f, 3.5f)
                arcTo(1.5f, 1.5f, 0f, true, true, 5f, 4f)
                moveTo(5f, 12f)
                arcTo(1.5f, 1.5f, 0f, true, true, 5f, 12.5f)
                moveTo(11f, 12f)
                arcTo(1.5f, 1.5f, 0f, true, true, 11f, 12.5f)
            }
        }.build()
    }
}
