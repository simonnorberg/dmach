package net.simno.dmach.core

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.DrawStyle
import androidx.compose.ui.graphics.drawscope.Fill

data class DrawableCircle(
    val color: Color,
    val radius: Float,
    val center: Offset,
    val alpha: Float = 1.0f,
    val style: DrawStyle = Fill
)

fun DrawScope.draw(circle: DrawableCircle) {
    drawCircle(
        color = circle.color,
        radius = circle.radius,
        center = circle.center,
        alpha = circle.alpha,
        style = circle.style
    )
}
