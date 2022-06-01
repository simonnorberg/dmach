package net.simno.dmach.core

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.DrawStyle
import androidx.compose.ui.graphics.drawscope.Fill

data class DrawableRect(
    val color: Color,
    val topLeft: Offset,
    val size: Size,
    val cornerRadius: CornerRadius,
    val alpha: Float = 1.0f,
    val style: DrawStyle = Fill
)

fun DrawScope.draw(rect: DrawableRect) {
    drawRoundRect(
        color = rect.color,
        topLeft = rect.topLeft,
        size = rect.size,
        cornerRadius = rect.cornerRadius,
        alpha = rect.alpha,
        style = rect.style
    )
}
