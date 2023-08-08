package net.simno.dmach.machine.ui

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.changedToDown
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChanged
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import net.simno.dmach.core.LightLargeText
import net.simno.dmach.core.LightMediumLabel
import net.simno.dmach.theme.AppTheme

@Composable
fun ConfigValue(
    configId: Int,
    @StringRes label: Int,
    configValue: Int,
    minValue: Int,
    maxValue: Int,
    modifier: Modifier = Modifier,
    onValue: (Int) -> Unit
) {
    val surface = MaterialTheme.colorScheme.surface
    val onPrimary = MaterialTheme.colorScheme.onPrimary
    val shapeMedium = MaterialTheme.shapes.medium
    val paddingLarge = AppTheme.dimens.paddingLarge
    val configHeight = AppTheme.dimens.configHeight
    val updatedOnValue by rememberUpdatedState(onValue)
    var value by remember { mutableStateOf(configValue) }

    Row(
        modifier = modifier
            .background(
                color = onPrimary,
                shape = shapeMedium
            )
            .fillMaxWidth()
            .height(configHeight)
            .padding(horizontal = paddingLarge)
            .pointerInput(configId) {
                val width = size.width.toFloat()
                val center = width / 2f
                value = configValue
                    .coerceAtLeast(minValue)
                    .coerceAtMost(maxValue)
                var delay = 500L
                var change = 1

                fun calculateChangeAndDelay(x: Float) {
                    val validX = x.coerceIn(0f, width)

                    change = if (validX > center) 1 else -1

                    val delayMultiplier = if (validX <= center) {
                        validX / center
                    } else {
                        1 - ((validX - center) / (width - center))
                    }

                    delay = (((MAX_DELAY - MIN_DELAY) * delayMultiplier) + MIN_DELAY).toLong()
                }

                coroutineScope {
                    awaitEachGesture {
                        val job = launch(start = CoroutineStart.LAZY) {
                            runCatching {
                                while (isActive) {
                                    val newValue = value + change
                                    if (newValue in minValue..maxValue) {
                                        value = newValue
                                        updatedOnValue(newValue)
                                    }
                                    delay(delay)
                                }
                            }
                        }

                        val firstPointer = awaitFirstDown()
                        if (firstPointer.changedToDown()) {
                            firstPointer.consume()
                        }
                        calculateChangeAndDelay(firstPointer.position.x)
                        job.start()

                        do {
                            val event = awaitPointerEvent()
                            event.changes.forEach { pointer ->
                                if (pointer.positionChanged()) {
                                    pointer.consume()
                                }
                                calculateChangeAndDelay(pointer.position.x)
                            }
                        } while (event.changes.any { it.pressed })

                        job.cancel()
                    }
                }
            },
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Icon(
            imageVector = Icons.Filled.Remove,
            contentDescription = null,
            tint = surface,
            modifier = Modifier
                .align(Alignment.CenterVertically)
                .size(36.dp)
        )
        Spacer(modifier = Modifier.size(96.dp))
        Icon(
            imageVector = Icons.Filled.Remove,
            contentDescription = null,
            tint = surface,
            modifier = Modifier
                .align(Alignment.CenterVertically)
                .size(18.dp)
        )
        Column(
            modifier = Modifier
                .align(Alignment.CenterVertically)
                .defaultMinSize(minWidth = 64.dp)
        ) {
            LightMediumLabel(
                text = stringResource(label).uppercase(),
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
            LightLargeText(
                text = "$value",
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }
        Icon(
            imageVector = Icons.Filled.Add,
            contentDescription = null,
            tint = surface,
            modifier = Modifier
                .align(Alignment.CenterVertically)
                .size(18.dp)
        )
        Spacer(modifier = Modifier.size(96.dp))
        Icon(
            imageVector = Icons.Filled.Add,
            contentDescription = null,
            tint = surface,
            modifier = Modifier
                .align(Alignment.CenterVertically)
                .size(36.dp)
        )
    }
}

private const val MAX_DELAY = 400f
private const val MIN_DELAY = 32f
