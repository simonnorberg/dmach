package net.simno.dmach.machine.ui

import android.animation.ValueAnimator
import android.view.animation.DecelerateInterpolator
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.changedToDown
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChanged
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import java.util.Locale
import net.simno.dmach.R
import net.simno.dmach.core.DarkLargeText
import net.simno.dmach.core.DarkSmallText
import net.simno.dmach.data.Pan
import net.simno.dmach.theme.AppTheme
import net.simno.dmach.util.toPx

@Composable
fun PanFader(
    panId: Int,
    pan: Pan?,
    debug: Boolean,
    modifier: Modifier = Modifier,
    onPanChanged: (Pan) -> Unit
) {
    val rectHeight = AppTheme.dimens.rectHeight
    val buttonMedium = AppTheme.dimens.buttonMedium

    var debugPan by remember { mutableStateOf(pan) }

    LaunchedEffect(pan) {
        pan?.let {
            debugPan = it
        }
    }

    Box(
        modifier = modifier
            .width(buttonMedium)
            .fillMaxHeight()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(rectHeight)
                .align(Alignment.TopCenter)
                .padding(top = 2.dp)
        ) {
            DarkLargeText(
                text = stringResource(R.string.pan_right),
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .wrapContentSize()
                    .align(Alignment.Center)
            )
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(rectHeight)
                .align(Alignment.BottomCenter)
                .padding(bottom = 2.dp)
        ) {
            DarkLargeText(
                text = stringResource(R.string.pan_left),
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .wrapContentSize()
                    .align(Alignment.Center)
            )
        }
        if (debug) {
            DarkSmallText(
                text = debugPan?.let { String.format(Locale.US, "%.2f", it.value) }.orEmpty(),
                modifier = Modifier.align(Alignment.Center)
            )
        }
        Fader(
            panId = panId,
            pan = pan,
            onPanChanged = {
                debugPan = it
                onPanChanged(it)
            }
        )
    }
}

@Composable
private fun Fader(
    panId: Int,
    pan: Pan?,
    modifier: Modifier = Modifier,
    onPanChanged: (Pan) -> Unit
) {
    val updatedOnPanChanged by rememberUpdatedState(onPanChanged)

    val haptic = LocalHapticFeedback.current
    val density = LocalDensity.current
    val secondary = MaterialTheme.colorScheme.secondary
    val cornerSize = MaterialTheme.shapes.small.topStart
    val rectHeight = AppTheme.dimens.rectHeight.toPx()
    val strokeWidth = AppTheme.dimens.paddingSmall.toPx()

    var size by remember { mutableStateOf(IntSize.Zero) }

    val rectSize = remember(strokeWidth, size.width, rectHeight) {
        Size(size.width.toFloat() - strokeWidth, rectHeight)
    }
    val offset = remember(rectHeight) { rectHeight / 2f }
    val cornerRadius = remember(rectSize) { cornerSize.toPx(rectSize, density).let { CornerRadius(it, it) } }
    val stroke = remember(strokeWidth) { Stroke(width = strokeWidth) }
    val minX = remember(strokeWidth) { strokeWidth / 2f }
    val minY = remember(strokeWidth, offset) { offset + (strokeWidth / 2f) }
    val maxY = remember(minY, size.height) { size.height - minY }

    var panPosition by remember(panId, maxY) {
        mutableStateOf(
            pan?.let {
                // Convert position value [0.0-1.0] to pixels.
                if (pan.value == 0.5f) {
                    size.height / 2f
                } else {
                    (1 - pan.value) * ((maxY - minY) + minY)
                }
            }
        )
    }

    val panOffset = remember(panPosition, offset, minX) {
        panPosition?.let {
            val newY = it
                .coerceAtLeast(minY)
                .coerceAtMost(maxY)
            Offset(minX, newY - offset)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .clipToBounds()
            .onSizeChanged { size = it }
            .pointerInput(panId, size) {
                var centerAnimator: ValueAnimator? = null
                var isCentered = true
                val center = size.height / 2f
                val left = center + (offset / 2f)
                val right = center - (offset / 2f)

                fun notifyPosition(y: Float, notifyCenter: Boolean = false) {
                    // Convert pixels to a position value [0.0-1.0]
                    val pos = if (notifyCenter) {
                        // Pixel conversion is not exact. Set y to 0.5 if we know it is centered.
                        0.5f
                    } else {
                        1 - ((y - minY) / (maxY - minY)).coerceIn(0f, 1f)
                    }
                    updatedOnPanChanged(Pan(pos))
                }

                fun animateToCenter(y: Float, center: Float) {
                    centerAnimator?.cancel()
                    centerAnimator = ValueAnimator
                        .ofFloat(y, center)
                        .apply {
                            duration = 100L
                            interpolator = DecelerateInterpolator()
                            addUpdateListener { animation ->
                                (animation.animatedValue as Float).let {
                                    panPosition = it
                                    notifyPosition(it, notifyCenter = it == center)
                                }
                            }
                            start()
                        }
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                }

                fun onPointerDownOrMove(y: Float) {
                    if (y < left && y > right) {
                        if (!isCentered) {
                            isCentered = true
                            animateToCenter(y, center)
                        }
                    } else {
                        isCentered = false
                        panPosition = y
                        notifyPosition(y)
                    }
                }

                awaitEachGesture {
                    val firstPointer = awaitFirstDown()
                    if (firstPointer.changedToDown()) {
                        firstPointer.consume()
                    }
                    onPointerDownOrMove(firstPointer.position.y)

                    do {
                        val event = awaitPointerEvent()
                        event.changes.forEach { pointer ->
                            if (pointer.positionChanged()) {
                                pointer.consume()
                            }
                            onPointerDownOrMove(pointer.position.y)
                        }
                    } while (event.changes.any { it.pressed })
                }
            }
            .drawBehind {
                panOffset?.let {
                    drawRoundRect(
                        color = secondary,
                        topLeft = it,
                        size = rectSize,
                        cornerRadius = cornerRadius,
                        style = stroke,
                        alpha = 0.94f
                    )
                }
            }
    )
}
