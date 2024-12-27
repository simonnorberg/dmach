package net.simno.dmach.machine.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import net.simno.dmach.R
import net.simno.dmach.core.LightMediumLabel
import net.simno.dmach.core.PredictiveBackProgress
import net.simno.dmach.data.Steps
import net.simno.dmach.data.Swing
import net.simno.dmach.data.Tempo
import net.simno.dmach.settings.Settings
import net.simno.dmach.theme.AppTheme

@Composable
fun ConfigDialog(
    configId: Int,
    tempo: Tempo,
    swing: Swing,
    steps: Steps,
    settings: Settings,
    onTempoChange: (Tempo) -> Unit,
    onSwingChange: (Swing) -> Unit,
    onStepsChange: (Steps) -> Unit,
    onSettingsChange: (Settings) -> Unit,
    modifier: Modifier = Modifier,
    onDismiss: () -> Unit
) {
    val surface = MaterialTheme.colorScheme.surface
    val primary = MaterialTheme.colorScheme.primary
    val onPrimary = MaterialTheme.colorScheme.onPrimary
    val shapeMedium = MaterialTheme.shapes.medium
    val paddingSmall = AppTheme.dimens.paddingSmall
    val paddingLarge = AppTheme.dimens.paddingLarge
    val configHeightSmall = AppTheme.dimens.configHeightSmall
    var showSettings by remember { mutableStateOf(false) }

    Dialog(
        properties = DialogProperties(usePlatformDefaultWidth = showSettings),
        onDismissRequest = onDismiss
    ) {
        var backProgress by remember { mutableFloatStateOf(0f) }
        var inPredictiveBack by remember { mutableStateOf(false) }
        PredictiveBackProgress(
            onProgress = { backProgress = it },
            onInPredictiveBack = { inPredictiveBack = it },
            onBack = onDismiss
        )

        Column(
            modifier = modifier
                .scale((1f - backProgress).coerceAtLeast(0.85f))
                .background(
                    color = primary,
                    shape = shapeMedium
                )
                .width(IntrinsicSize.Max)
                .padding(paddingSmall),
            verticalArrangement = Arrangement.spacedBy(paddingSmall)
        ) {
            if (showSettings) {
                Row(
                    modifier = Modifier
                        .background(
                            color = onPrimary,
                            shape = shapeMedium
                        )
                        .fillMaxWidth()
                        .height(configHeightSmall)
                        .clickable(onClick = { showSettings = false })
                        .padding(horizontal = paddingLarge),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.ChevronLeft,
                        contentDescription = null,
                        tint = surface,
                        modifier = Modifier.size(24.dp)
                    )
                    LightMediumLabel(stringResource(R.string.back))
                }
                ConfigCheckbox(
                    text = stringResource(R.string.audiofocus),
                    checked = settings.ignoreAudioFocus,
                    onCheckedChange = { checked ->
                        onSettingsChange(settings.copy(ignoreAudioFocus = checked))
                    }
                )
                ConfigCheckbox(
                    text = stringResource(R.string.sequencer_setting),
                    checked = settings.sequenceEnabled,
                    onCheckedChange = { checked ->
                        onSettingsChange(settings.copy(sequenceEnabled = checked))
                    }
                )
                ConfigCheckbox(
                    text = stringResource(R.string.sound_setting),
                    checked = settings.soundEnabled,
                    onCheckedChange = { checked ->
                        onSettingsChange(settings.copy(soundEnabled = checked))
                    }
                )
                ConfigCheckbox(
                    text = stringResource(R.string.pan_setting),
                    checked = settings.panEnabled,
                    onCheckedChange = { checked ->
                        onSettingsChange(settings.copy(panEnabled = checked))
                    }
                )
            } else {
                ConfigValue(
                    configId = configId,
                    label = R.string.bpm,
                    configValue = tempo.value,
                    minValue = 1,
                    maxValue = 1000,
                    onValueChange = { value -> onTempoChange(Tempo(value)) }
                )
                ConfigValue(
                    configId = configId,
                    label = R.string.swing,
                    configValue = swing.value,
                    minValue = 0,
                    maxValue = 50,
                    onValueChange = { value -> onSwingChange(Swing(value)) }
                )
                ConfigValue(
                    configId = configId,
                    label = R.string.steps,
                    configValue = steps.value,
                    minValue = 8,
                    maxValue = 16,
                    onValueChange = { value -> onStepsChange(Steps(value)) }
                )
                Row(
                    modifier = Modifier
                        .background(
                            color = onPrimary,
                            shape = shapeMedium
                        )
                        .fillMaxWidth()
                        .height(configHeightSmall)
                        .clickable(onClick = { showSettings = true })
                        .padding(horizontal = paddingLarge),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    LightMediumLabel(stringResource(R.string.more_settings))
                    Icon(
                        imageVector = Icons.Filled.ChevronRight,
                        contentDescription = null,
                        tint = surface,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}
