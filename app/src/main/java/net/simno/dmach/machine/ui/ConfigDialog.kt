package net.simno.dmach.machine.ui

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.forEachGesture
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.changedToDown
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChanged
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import net.simno.dmach.R
import net.simno.dmach.core.LightLargeText
import net.simno.dmach.core.LightMediumText
import net.simno.dmach.theme.AppTheme

@Composable
fun ConfigDialog(
    configId: Int,
    tempo: Int,
    swing: Int,
    ignoreAudioFocus: Boolean,
    onTempo: (Int) -> Unit,
    onSwing: (Int) -> Unit,
    onAudioFocus: (Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    val surface = MaterialTheme.colorScheme.surface
    val primary = MaterialTheme.colorScheme.primary
    val onPrimary = MaterialTheme.colorScheme.onPrimary
    val shapeMedium = MaterialTheme.shapes.medium
    val paddingLarge = AppTheme.dimens.PaddingLarge
    val paddingSmall = AppTheme.dimens.PaddingSmall

    Dialog(
        properties = DialogProperties(usePlatformDefaultWidth = false),
        onDismissRequest = onDismiss
    ) {
        Column(
            modifier = Modifier
                .background(
                    color = primary,
                    shape = shapeMedium
                )
                .width(IntrinsicSize.Max)
                .padding(paddingSmall),
            verticalArrangement = Arrangement.spacedBy(paddingSmall)
        ) {
            ValueConfig(
                background = onPrimary,
                configId = configId,
                label = R.string.config_tempo,
                configValue = tempo,
                minValue = 1,
                maxValue = 1000,
                onValue = onTempo
            )
            ValueConfig(
                background = onPrimary,
                configId = configId,
                label = R.string.config_swing,
                configValue = swing,
                minValue = 0,
                maxValue = 50,
                onValue = onSwing
            )
            Row(
                modifier = Modifier
                    .background(
                        color = onPrimary,
                        shape = shapeMedium
                    )
                    .padding(
                        start = paddingLarge,
                        top = paddingSmall,
                        end = paddingSmall,
                        bottom = paddingSmall
                    )
            ) {
                LightMediumText(
                    text = stringResource(R.string.config_audiofocus),
                    modifier = Modifier
                        .weight(1f)
                        .wrapContentWidth(Alignment.Start)
                        .align(Alignment.CenterVertically)
                )
                Checkbox(
                    checked = ignoreAudioFocus,
                    modifier = Modifier
                        .weight(1f)
                        .wrapContentWidth(Alignment.End),
                    onCheckedChange = onAudioFocus,
                    colors = CheckboxDefaults.colors(
                        checkedColor = surface,
                        uncheckedColor = surface,
                        checkmarkColor = onPrimary
                    )
                )
            }
        }
    }
}

@Composable
private fun ValueConfig(
    background: Color,
    configId: Int,
    @StringRes label: Int,
    configValue: Int,
    minValue: Int,
    maxValue: Int,
    onValue: (Int) -> Unit
) {
    val surface = MaterialTheme.colorScheme.surface
    val shapeMedium = MaterialTheme.shapes.medium
    val paddingLarge = AppTheme.dimens.PaddingLarge
    val updatedOnValue by rememberUpdatedState(onValue)
    var value by remember { mutableStateOf(configValue) }

    Row(
        modifier = Modifier
            .background(
                color = background,
                shape = shapeMedium
            )
            .wrapContentWidth()
            .height(80.dp)
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
                    forEachGesture {
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

                        awaitPointerEventScope {
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
                }
            },
        horizontalArrangement = Arrangement.spacedBy(paddingLarge)
    ) {
        Icon(
            imageVector = Icons.Filled.Remove,
            contentDescription = null,
            tint = surface,
            modifier = Modifier
                .align(Alignment.CenterVertically)
                .size(36.dp)
        )
        Spacer(modifier = Modifier.size(64.dp))
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
                .width(64.dp)
        ) {
            LightMediumText(
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
        Spacer(modifier = Modifier.size(64.dp))
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
