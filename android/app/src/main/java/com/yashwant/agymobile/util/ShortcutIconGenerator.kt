package com.yashwant.agymobile.util

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.drawable.Drawable

enum class IconShape {
    CIRCLE,
    SQUIRCLE,
    ROUNDED_SQUARE,
    TEARDROP
}

data class ShortcutDesign(
    val label: String,
    val shape: IconShape = IconShape.SQUIRCLE,
    val backgroundColor: Int = 0xFF1E1F22.toInt(),
    val accentColor: Int = 0xFF4285F4.toInt(),
    val emojiOrText: String? = null,
    val centerDrawable: Drawable? = null,
    val showBorder: Boolean = true
)

object ShortcutIconGenerator {

    fun createIconBitmap(design: ShortcutDesign, size: Int = 192): Bitmap? {
        return try {
            val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888) ?: return null
            val canvas = Canvas(bitmap)
            val padding = size * 0.06f
            val rect = RectF(padding, padding, size - padding, size - padding)
            val shapePath = getShapePath(rect, design.shape)

            // Draw background shape
            val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = design.backgroundColor
                style = Paint.Style.FILL
            }
            canvas.drawPath(shapePath, bgPaint)

            // Draw accent border
            if (design.showBorder) {
                val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = design.accentColor
                    style = Paint.Style.STROKE
                    strokeWidth = size * 0.04f
                }
                canvas.drawPath(shapePath, borderPaint)
            }

            // Draw center content
            if (design.centerDrawable != null) {
                val iconPadding = size * 0.22f
                val iconLeft = iconPadding.toInt()
                val iconTop = iconPadding.toInt()
                val iconRight = (size - iconPadding).toInt()
                val iconBottom = (size - iconPadding).toInt()
                design.centerDrawable.setBounds(iconLeft, iconTop, iconRight, iconBottom)
                design.centerDrawable.draw(canvas)
            } else if (!design.emojiOrText.isNullOrBlank()) {
                val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = design.accentColor
                    textSize = size * 0.42f
                    textAlign = Paint.Align.CENTER
                }
                val fontMetrics = textPaint.fontMetrics
                val baseline = (size / 2f) - ((fontMetrics.descent + fontMetrics.ascent) / 2f)
                canvas.drawText(design.emojiOrText, size / 2f, baseline, textPaint)
            }

            bitmap
        } catch (_: Throwable) {
            null
        }
    }

    fun getShapePath(rect: RectF, shape: IconShape): Path {
        val path = Path()
        when (shape) {
            IconShape.CIRCLE -> {
                path.addOval(rect, Path.Direction.CW)
            }
            IconShape.SQUIRCLE -> {
                val rx = rect.width() * 0.35f
                val ry = rect.height() * 0.35f
                path.addRoundRect(rect, rx, ry, Path.Direction.CW)
            }
            IconShape.ROUNDED_SQUARE -> {
                val rx = rect.width() * 0.18f
                val ry = rect.height() * 0.18f
                path.addRoundRect(rect, rx, ry, Path.Direction.CW)
            }
            IconShape.TEARDROP -> {
                val radii = floatArrayOf(
                    rect.width() * 0.45f, rect.width() * 0.45f, // Top-left
                    rect.width() * 0.45f, rect.width() * 0.45f, // Top-right
                    rect.width() * 0.08f, rect.width() * 0.08f, // Bottom-right
                    rect.width() * 0.45f, rect.width() * 0.45f  // Bottom-left
                )
                path.addRoundRect(rect, radii, Path.Direction.CW)
            }
        }
        return path
    }
}
