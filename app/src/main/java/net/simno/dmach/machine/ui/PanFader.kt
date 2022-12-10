package net.simno.dmach.machine.ui

import android.animation.ValueAnimator
import android.view.animation.DecelerateInterpolator
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.forEachGesture
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.changedToDown
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChanged
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import net.simno.dmach.R
import net.simno.dmach.core.DarkLargeText
import net.simno.dmach.core.DrawableRect
import net.simno.dmach.core.draw
import net.simno.dmach.data.Pan
import net.simno.dmach.theme.AppTheme

@Composable
fun PanFader(
    panId: Int,
    pan: Pan?,
    modifier: Modifier = Modifier,
    onPan: (Pan) -> Unit
) {
    val rectHeight = AppTheme.dimens.RectHeight
    val buttonMedium = AppTheme.dimens.ButtonMedium

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
        Fader(
            panId = panId,
            pan = pan,
            onPan = onPan,
            modifier = modifier
        )
    }
}

@Composable
private fun Fader(
    panId: Int,
    pan: Pan?,
    onPan: (Pan) -> Unit,
    modifier: Modifier = Modifier
) {
    val secondary = MaterialTheme.colorScheme.secondary
    val shapeSmall = MaterialTheme.shapes.small
    val rectHeight = AppTheme.dimens.RectHeight
    val paddingSmall = AppTheme.dimens.PaddingSmall
    val updatedOnPan by rememberUpdatedState(onPan)
    var rect by remember { mutableStateOf<DrawableRect?>(null) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(panId) {
                val strokeWidth = paddingSmall.toPx()
                val rectHeightPx = rectHeight.toPx()
                val rectSize = Size(size.width.toFloat(), rectHeightPx)
                val offset = rectHeightPx / 2f
                val radius = shapeSmall.topStart.toPx(rectSize, this)
                val cornerRadius = CornerRadius(radius, radius)
                val stroke = Stroke(width = strokeWidth)

                val minX = strokeWidth / 2f
                val minY = offset - (strokeWidth / 2f)
                val maxY = size.height - minY

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
                    updatedOnPan(Pan(pos))
                }

                fun getDrawableRect(y: Float): DrawableRect {
                    val newY = y
                        .coerceAtLeast(minY)
                        .coerceAtMost(maxY)
                    return DrawableRect(
                        color = secondary,
                        topLeft = Offset(minX, newY - offset),
                        size = rectSize,
                        cornerRadius = cornerRadius,
                        style = stroke,
                        alpha = 0.94f
                    )
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
                                    rect = getDrawableRect(it)
                                    notifyPosition(it, notifyCenter = it == center)
                                }
                            }
                            start()
                        }
                }

                fun onPointerDownOrMove(y: Float) {
                    if (y < left && y > right) {
                        if (!isCentered) {
                            isCentered = true
                            animateToCenter(y, center)
                        }
                    } else {
                        isCentered = false
                        rect = getDrawableRect(y)
                        notifyPosition(y)
                    }
                }

                if (pan != null) {
                    // Convert position value [0.0-1.0] to pixels.
                    val newY = if (pan.value == 0.5f) size.height / 2f else (1 - pan.value) * (maxY - minY) + minY
                    rect = getDrawableRect(newY)
                }

                forEachGesture {
                    awaitPointerEventScope {
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
            }
            .drawBehind {
                rect?.let(::draw)
            }
    )
}
