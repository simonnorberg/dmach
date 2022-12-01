package net.simno.dmach.machine.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.forEachGesture
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.changedToDown
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChanged
import androidx.compose.ui.layout.layout
import net.simno.dmach.core.DarkLargeText
import net.simno.dmach.core.DrawableCircle
import net.simno.dmach.core.draw
import net.simno.dmach.data.Position
import net.simno.dmach.theme.AppTheme

@Composable
fun ChaosPad(
    settingId: Int,
    position: Position?,
    horizontalText: String,
    verticalText: String,
    modifier: Modifier = Modifier,
    onPosition: (Position) -> Unit
) {
    val secondary = MaterialTheme.colorScheme.secondary
    val shapeSmall = MaterialTheme.shapes.small
    val paddingSmall = AppTheme.dimens.PaddingSmall

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                color = secondary,
                shape = shapeSmall
            )
    ) {
        DarkLargeText(
            text = verticalText.uppercase(),
            modifier = Modifier
                .layout { measurable, constraints ->
                    val placeable = measurable.measure(constraints)
                    layout(placeable.height, placeable.width) {
                        placeable.place(
                            x = -(placeable.width / 2 - placeable.height / 2),
                            y = -(placeable.height / 2 - placeable.width / 2)
                        )
                    }
                }
                .rotate(-90f)
                .align(Alignment.CenterStart)
        )
        DarkLargeText(
            text = horizontalText.uppercase(),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = paddingSmall)
        )
        Circle(
            settingId = settingId,
            position = position,
            onPosition = onPosition
        )
    }
}

@Composable
private fun Circle(
    settingId: Int,
    position: Position?,
    onPosition: (Position) -> Unit,
    modifier: Modifier = Modifier
) {
    val surface = MaterialTheme.colorScheme.surface
    val circleRadius = AppTheme.dimens.CircleRadius
    val paddingSmall = AppTheme.dimens.PaddingSmall
    val updatedOnPosition by rememberUpdatedState(onPosition)
    var circle by remember { mutableStateOf<DrawableCircle?>(null) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(settingId) {
                val strokeWidth = paddingSmall.toPx()
                val radius = circleRadius.toPx()
                val stroke = Stroke(width = strokeWidth)
                val minX = strokeWidth / 2f + radius
                val minY = strokeWidth / 2f + radius
                val maxX = size.width - minX
                val maxY = size.height - minY

                fun notifyPosition(x: Float, y: Float) {
                    // Convert pixels to a position value [0.0-1.0]
                    val posX = ((x - minX) / (maxX - minX)).coerceIn(0f, 1f)
                    val posY = 1 - ((y - minY) / (maxY - minY)).coerceIn(0f, 1f)
                    updatedOnPosition(Position(posX, posY))
                }

                fun getDrawableCircle(x: Float, y: Float): DrawableCircle {
                    val newX = x
                        .coerceAtLeast(minX)
                        .coerceAtMost(maxX)
                    val newY = y
                        .coerceAtLeast(minY)
                        .coerceAtMost(maxY)
                    return DrawableCircle(
                        color = surface,
                        radius = radius,
                        style = stroke,
                        center = Offset(newX, newY),
                        alpha = 0.94f
                    )
                }

                fun onPointerDownOrMove(pointer: PointerInputChange) {
                    circle = getDrawableCircle(pointer.position.x, pointer.position.y)
                    notifyPosition(pointer.position.x, pointer.position.y)
                }

                if (position != null) {
                    // Convert position value [0.0-1.0] to pixels.
                    val newX = position.x * (maxX - minX) + minX
                    val newY = (1 - position.y) * (maxY - minY) + minY
                    circle = getDrawableCircle(newX, newY)
                }

                forEachGesture {
                    awaitPointerEventScope {
                        val firstPointer = awaitFirstDown()
                        if (firstPointer.changedToDown()) {
                            firstPointer.consume()
                        }
                        onPointerDownOrMove(firstPointer)

                        do {
                            val event = awaitPointerEvent()
                            event.changes.forEach { pointer ->
                                if (pointer.positionChanged()) {
                                    pointer.consume()
                                }
                                onPointerDownOrMove(pointer)
                            }
                        } while (event.changes.any { it.pressed })
                    }
                }
            }
            .drawBehind {
                circle?.let(::draw)
            }
    )
}
