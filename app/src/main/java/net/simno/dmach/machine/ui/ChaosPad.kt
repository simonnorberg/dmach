package net.simno.dmach.machine.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.changedToDown
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChanged
import androidx.compose.ui.layout.layout
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import java.util.Locale
import net.simno.dmach.core.DarkLargeText
import net.simno.dmach.core.DarkSmallText
import net.simno.dmach.data.Position
import net.simno.dmach.theme.AppTheme
import net.simno.dmach.util.toPx

@Composable
fun ChaosPad(
    settingId: Int,
    position: Position?,
    horizontalText: String,
    verticalText: String,
    debug: Boolean,
    modifier: Modifier = Modifier,
    onPositionChanged: (Position) -> Unit
) {
    val secondary = MaterialTheme.colorScheme.secondary
    val shapeSmall = MaterialTheme.shapes.small
    val paddingSmall = AppTheme.dimens.paddingSmall

    var debugPosition by remember { mutableStateOf(position) }

    LaunchedEffect(position) {
        position?.let {
            debugPosition = it
        }
    }

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
        if (debug) {
            DarkSmallText(
                text = debugPosition?.let { String.format(Locale.US, "%.2f", it.y) }.orEmpty(),
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
                    .align(Alignment.TopStart)
                    .padding(end = 4.dp)
            )
            DarkSmallText(
                text = debugPosition?.let { String.format(Locale.US, "%.2f", it.x) }.orEmpty(),
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 4.dp)
            )
        }
        Circle(
            settingId = settingId,
            position = position,
            onPositionChanged = {
                debugPosition = it
                onPositionChanged(it)
            }
        )
    }
}

@Composable
private fun Circle(
    settingId: Int,
    position: Position?,
    modifier: Modifier = Modifier,
    onPositionChanged: (Position) -> Unit
) {
    val updatedOnPositionChanged by rememberUpdatedState(onPositionChanged)

    val surface = MaterialTheme.colorScheme.surface
    val radius = AppTheme.dimens.circleRadius.toPx()
    val strokeWidth = AppTheme.dimens.paddingSmall.toPx()

    var size by remember { mutableStateOf(IntSize.Zero) }

    val stroke = remember(strokeWidth) { Stroke(width = strokeWidth) }
    val minX = remember(strokeWidth, radius) { strokeWidth / 2f + radius }
    val minY = remember(strokeWidth, radius) { strokeWidth / 2f + radius }
    val maxX = remember(minX, size.width) { size.width - minX }
    val maxY = remember(minY, size.height) { size.height - minY }

    var circlePosition by remember(settingId, maxX, maxY) {
        mutableStateOf(
            position?.let {
                // Convert position value [0.0-1.0] to pixels.
                val newX = it.x * ((maxX - minX) + minX)
                val newY = (1 - it.y) * ((maxY - minY) + minY)
                Position(newX, newY)
            }
        )
    }

    val circleOffset = remember(circlePosition) {
        circlePosition?.let {
            val newX = it.x
                .coerceAtLeast(minX)
                .coerceAtMost(maxX)
            val newY = it.y
                .coerceAtLeast(minY)
                .coerceAtMost(maxY)
            Offset(newX, newY)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .clipToBounds()
            .onSizeChanged { size = it }
            .pointerInput(settingId, size) {
                fun notifyPosition(x: Float, y: Float) {
                    // Convert pixels to a position value [0.0-1.0]
                    val posX = ((x - minX) / (maxX - minX)).coerceIn(0f, 1f)
                    val posY = 1 - ((y - minY) / (maxY - minY)).coerceIn(0f, 1f)
                    updatedOnPositionChanged(Position(posX, posY))
                }

                fun onPointerDownOrMove(pointer: PointerInputChange) {
                    circlePosition = Position(pointer.position.x, pointer.position.y)
                    notifyPosition(pointer.position.x, pointer.position.y)
                }

                awaitEachGesture {
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
            .drawBehind {
                circleOffset?.let {
                    drawCircle(
                        color = surface,
                        radius = radius,
                        style = stroke,
                        center = it,
                        alpha = 0.94f
                    )
                }
            }
    )
}
