package net.simno.dmach.machine.ui

import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.forEachGesture
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.input.pointer.changedToDown
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChanged
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntSize
import net.simno.dmach.core.DrawableRect
import net.simno.dmach.core.draw
import net.simno.dmach.theme.AppTheme

@Composable
fun StepSequencer(
    sequenceId: Int,
    sequence: List<Int>,
    modifier: Modifier = Modifier,
    onSequence: (List<Int>) -> Unit
) {
    val tertiary = MaterialTheme.colorScheme.tertiary
    val onSurface = MaterialTheme.colorScheme.onSurface
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant
    val shapeSmall = MaterialTheme.shapes.small
    val paddingSmall = AppTheme.dimens.PaddingSmall
    val updatedOnSequence by rememberUpdatedState(onSequence)
    var drawables by remember { mutableStateOf<List<DrawableRect>>(emptyList()) }
    var sizeKey by remember { mutableStateOf(IntSize.Zero) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .onSizeChanged { sizeKey = it }
            .pointerInput(sequenceId, sizeKey) {
                val steps = sequence.toMutableList()
                val margin = paddingSmall.toPx()
                val stepSize = Size(
                    width = (size.width - (STEPS - 1f) * margin) / STEPS,
                    height = (size.height - (CHANNELS - 1f) * margin) / CHANNELS
                )
                val radius = shapeSmall.topStart.toPx(stepSize, this)
                val cornerRadius = CornerRadius(radius, radius)

                fun getDrawableSteps() = steps
                    .mapIndexed { stepIndex, step ->
                        MASKS.mapIndexed { maskIndex, mask ->
                            val left = (stepIndex % STEPS) * (stepSize.width + margin)
                            val top = (maskIndex + ((stepIndex / STEPS) * MASKS.size)) * (stepSize.height + margin)
                            val color = when {
                                step and mask > 0 -> tertiary
                                stepIndex % 8 < 4 -> onSurface
                                else -> onSurfaceVariant
                            }
                            DrawableRect(
                                color = color,
                                topLeft = Offset(left, top),
                                size = stepSize,
                                cornerRadius = cornerRadius
                            )
                        }
                    }
                    .flatten()

                fun onStepChange(stepChange: StepChange) {
                    steps[stepChange.index] = stepChange.changedStep
                    drawables = getDrawableSteps()
                    updatedOnSequence(steps)
                }

                drawables = getDrawableSteps()

                forEachGesture {
                    awaitPointerEventScope {
                        val firstPointer = awaitFirstDown()
                        if (firstPointer.changedToDown()) {
                            firstPointer.consume()
                        }

                        val firstStepChange = firstPointer.position
                            .takeIf { it.isValid(size) }
                            ?.let { position ->
                                StepChange(steps, stepSize, margin, position)
                            }
                        firstStepChange?.let(::onStepChange)

                        do {
                            val event = awaitPointerEvent()
                            event.changes.forEach { pointer ->
                                if (pointer.positionChanged()) {
                                    pointer.consume()
                                }
                                pointer.position
                                    .takeIf { it.isValid(size) }
                                    ?.let { position ->
                                        val stepChange = StepChange(steps, stepSize, margin, position)
                                        if (stepChange.isChecked == firstStepChange?.isChecked) {
                                            onStepChange(stepChange)
                                        }
                                    }
                            }
                        } while (event.changes.any { it.pressed })
                    }
                }
            }
            .drawBehind {
                drawables.forEach(::draw)
            }
    )
}

private const val CHANNELS = 6
private const val STEPS = 16
private val MASKS = intArrayOf(1, 2, 4)

private fun Offset.isValid(size: IntSize): Boolean =
    x.isFinite() &&
        y.isFinite() &&
        x >= 0 &&
        y >= 0 &&
        x <= size.width &&
        y <= size.height

private data class StepChange(
    val steps: List<Int>,
    val stepSize: Size,
    val margin: Float,
    val position: Offset
) {
    private val step = (position.x / (stepSize.width + margin)).toInt().coerceIn(0, STEPS - 1)
    private val channel = (position.y / (stepSize.height + margin)).toInt().coerceIn(0, CHANNELS - 1)
    private val mask = MASKS[channel % MASKS.size]
    val index = ((channel / MASKS.size) * STEPS) + step
    val isChecked = (steps[index] and mask) > 0
    val changedStep = steps[index] xor mask
}
