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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import net.simno.dmach.R
import net.simno.dmach.core.LightMediumText
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
    onTempo: (Tempo) -> Unit,
    onSwing: (Swing) -> Unit,
    onSteps: (Steps) -> Unit,
    onSettings: (Settings) -> Unit,
    onDismiss: () -> Unit
) {
    val surface = MaterialTheme.colorScheme.surface
    val primary = MaterialTheme.colorScheme.primary
    val onPrimary = MaterialTheme.colorScheme.onPrimary
    val shapeMedium = MaterialTheme.shapes.medium
    val paddingSmall = AppTheme.dimens.PaddingSmall
    val paddingLarge = AppTheme.dimens.PaddingLarge
    val configHeightSmall = AppTheme.dimens.ConfigHeightSmall

    val updatedOnTempo by rememberUpdatedState(onTempo)
    val updatedOnSwing by rememberUpdatedState(onSwing)
    val updatedOnSteps by rememberUpdatedState(onSteps)
    val updatedOnSettings by rememberUpdatedState(onSettings)

    var showSettings by remember { mutableStateOf(false) }

    Dialog(
        properties = DialogProperties(usePlatformDefaultWidth = showSettings),
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
                    LightMediumText(stringResource(R.string.back))
                }
                ConfigCheckbox(
                    text = stringResource(R.string.audiofocus),
                    checked = settings.ignoreAudioFocus,
                    onCheckedChange = { checked ->
                        updatedOnSettings(settings.copy(ignoreAudioFocus = checked))
                    }
                )
                ConfigCheckbox(
                    text = stringResource(R.string.sequencer_setting),
                    checked = settings.sequenceEnabled,
                    onCheckedChange = { checked ->
                        updatedOnSettings(settings.copy(sequenceEnabled = checked))
                    }
                )
                ConfigCheckbox(
                    text = stringResource(R.string.sound_setting),
                    checked = settings.soundEnabled,
                    onCheckedChange = { checked ->
                        updatedOnSettings(settings.copy(soundEnabled = checked))
                    }
                )
                ConfigCheckbox(
                    text = stringResource(R.string.pan_setting),
                    checked = settings.panEnabled,
                    onCheckedChange = { checked ->
                        updatedOnSettings(settings.copy(panEnabled = checked))
                    }
                )
            } else {
                ConfigValue(
                    configId = configId,
                    label = R.string.bpm,
                    configValue = tempo.value,
                    minValue = 1,
                    maxValue = 1000,
                    onValue = { value -> updatedOnTempo(Tempo(value)) }
                )
                ConfigValue(
                    configId = configId,
                    label = R.string.swing,
                    configValue = swing.value,
                    minValue = 0,
                    maxValue = 50,
                    onValue = { value -> updatedOnSwing(Swing(value)) }
                )
                ConfigValue(
                    configId = configId,
                    label = R.string.steps,
                    configValue = steps.value,
                    minValue = 8,
                    maxValue = 16,
                    onValue = { value -> updatedOnSteps(Steps(value)) }
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
                    LightMediumText(stringResource(R.string.more_settings))
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
