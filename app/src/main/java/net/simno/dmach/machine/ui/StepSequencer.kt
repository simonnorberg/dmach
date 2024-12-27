package net.simno.dmach.machine.ui

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.input.pointer.changedToDown
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChanged
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntSize
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.toPersistentList
import net.simno.dmach.data.Patch.Companion.CHANNELS
import net.simno.dmach.data.Patch.Companion.MASKS
import net.simno.dmach.data.Patch.Companion.STEPS
import net.simno.dmach.data.Steps
import net.simno.dmach.theme.AppTheme
import net.simno.dmach.util.toPx

@Composable
fun StepSequencer(
    sequenceId: Int,
    sequence: PersistentList<Int>,
    sequenceLength: Steps,
    onSequenceChange: (PersistentList<Int>) -> Unit,
    modifier: Modifier = Modifier
) {
    val currentOnSequenceChange by rememberUpdatedState(onSequenceChange)

    val density = LocalDensity.current
    val tertiary = MaterialTheme.colorScheme.tertiary
    val onSurface = MaterialTheme.colorScheme.onSurface
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant
    val cornerSize = MaterialTheme.shapes.small.topStart
    val margin = AppTheme.dimens.paddingSmall.toPx()

    var size by remember { mutableStateOf(IntSize.Zero) }

    var stepChanges by remember { mutableIntStateOf(0) }
    val steps = remember(sequenceId) { mutableStateListOf(*sequence.toTypedArray()) }

    val stepSize = remember(size, margin, sequenceLength) {
        Size(
            width = (size.width - (sequenceLength.value - 1f) * margin) / sequenceLength.value,
            height = (size.height - (CHANNELS - 1f) * margin) / CHANNELS
        )
    }

    val cornerRadius = remember(stepSize) { cornerSize.toPx(stepSize, density).let { CornerRadius(it, it) } }

    val drawableSteps = remember(steps, stepChanges, stepSize) {
        steps
            .mapIndexed { stepIndex, step ->
                MASKS.mapIndexed { maskIndex, mask ->
                    val left = (stepIndex % STEPS) * (stepSize.width + margin)
                    val top = (maskIndex + ((stepIndex / STEPS) * MASKS.size)) * (stepSize.height + margin)
                    val color = when {
                        step and mask > 0 -> tertiary
                        stepIndex % 8 < 4 -> onSurface
                        else -> onSurfaceVariant
                    }
                    DrawableStep(
                        color = color,
                        offset = Offset(left, top)
                    )
                }
            }
            .flatten()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .onSizeChanged { size = it }
            .clipToBounds()
            .pointerInput(sequenceId, size) {
                fun onStepChange(stepChange: StepChange) {
                    steps[stepChange.index] = stepChange.changedStep
                    stepChanges++
                    currentOnSequenceChange(steps.toPersistentList())
                }

                awaitEachGesture {
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
            .drawBehind {
                drawableSteps.forEach {
                    drawRoundRect(
                        color = it.color,
                        topLeft = it.offset,
                        size = stepSize,
                        cornerRadius = cornerRadius,
                        alpha = 1.0f,
                        style = Fill
                    )
                }
            }
    )
}

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

private data class DrawableStep(
    val color: Color,
    val offset: Offset
)
