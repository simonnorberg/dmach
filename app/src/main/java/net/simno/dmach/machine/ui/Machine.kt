package net.simno.dmach.machine.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import net.simno.dmach.R
import net.simno.dmach.core.DarkMediumText
import net.simno.dmach.core.DarkSmallText
import net.simno.dmach.core.IconButton
import net.simno.dmach.core.TextButton
import net.simno.dmach.data.Channel
import net.simno.dmach.machine.state.Action
import net.simno.dmach.machine.state.AudioFocusAction
import net.simno.dmach.machine.state.ChangePanAction
import net.simno.dmach.machine.state.ChangePositionAction
import net.simno.dmach.machine.state.ChangeSequenceAction
import net.simno.dmach.machine.state.ChangeSwingAction
import net.simno.dmach.machine.state.ChangeTempoAction
import net.simno.dmach.machine.state.ConfigAction
import net.simno.dmach.machine.state.DismissAction
import net.simno.dmach.machine.state.PlayPauseAction
import net.simno.dmach.machine.state.SelectChannelAction
import net.simno.dmach.machine.state.SelectSettingAction
import net.simno.dmach.machine.state.ViewState
import net.simno.dmach.theme.AppTheme

@Composable
fun Machine(
    state: ViewState,
    onAction: (Action) -> Unit,
    onClickPatch: () -> Unit
) {
    val shapeMedium = MaterialTheme.shapes.medium
    val buttonLarge = AppTheme.dimens.ButtonLarge
    val buttonSmall = AppTheme.dimens.ButtonSmall
    val paddingLarge = AppTheme.dimens.PaddingLarge
    val paddingMedium = AppTheme.dimens.PaddingMedium
    val paddingSmall = AppTheme.dimens.PaddingSmall
    val updatedOnAction by rememberUpdatedState(onAction)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding()
            .navigationBarsPadding()
            .safeDrawingPadding()
            .padding(paddingSmall)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(.16f)
                .padding(start = paddingSmall, top = paddingSmall, end = paddingSmall),
            horizontalArrangement = Arrangement.spacedBy(paddingMedium)
        ) {
            IconButton(
                icon = Icons.Filled.PlayArrow,
                iconPadding = paddingSmall,
                description = R.string.description_play,
                selected = state.isPlaying,
                modifier = Modifier
                    .width(buttonLarge)
                    .wrapContentWidth(),
                onClick = { updatedOnAction(PlayPauseAction) }
            )
            IconButton(
                icon = Icons.Filled.Tune,
                description = R.string.description_config,
                selected = state.showConfig,
                modifier = Modifier
                    .width(buttonLarge)
                    .wrapContentWidth(),
                onClick = { updatedOnAction(ConfigAction) }
            )
            IconButton(
                icon = Icons.Filled.DeleteForever,
                description = R.string.description_reset,
                selected = false,
                modifier = Modifier
                    .width(buttonLarge)
                    .wrapContentWidth(),
                onClick = { updatedOnAction(ChangeSequenceAction.Empty) }
            )
            IconButton(
                icon = Icons.Filled.Refresh,
                description = R.string.description_random,
                selected = false,
                modifier = Modifier
                    .width(buttonLarge)
                    .wrapContentWidth(),
                onClick = { updatedOnAction(ChangeSequenceAction.Randomize()) }
            )
            Row(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxSize()
                    .clip(shapeMedium)
                    .clickable(onClick = onClickPatch)
                    .padding(paddingSmall),
                horizontalArrangement = Arrangement.spacedBy(paddingLarge)
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .align(Alignment.CenterVertically)
                        .padding(horizontal = paddingSmall),
                    verticalArrangement = Arrangement.Center
                ) {
                    DarkSmallText(stringResource(R.string.patch_name).uppercase())
                    DarkMediumText(state.title)
                }
                Column(
                    modifier = Modifier.align(Alignment.CenterVertically),
                    verticalArrangement = Arrangement.Center
                ) {
                    DarkSmallText(stringResource(R.string.patch_swing).uppercase())
                    DarkMediumText(state.swing.toString())
                }
                Column(
                    modifier = Modifier.align(Alignment.CenterVertically),
                    verticalArrangement = Arrangement.Center
                ) {
                    DarkSmallText(stringResource(R.string.patch_bpm).uppercase())
                    DarkMediumText(state.tempo.toString())
                }
            }
        }
        Row {
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(buttonSmall)
                    .padding(start = paddingSmall, top = paddingSmall, bottom = paddingSmall),
                verticalArrangement = Arrangement.spacedBy(paddingSmall)
            ) {
                ChannelName.values().forEachIndexed { index, channelName ->
                    TextButton(
                        text = channelName.name,
                        selected = state.selectedChannel == index,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxSize(),
                        onClick = { updatedOnAction(SelectChannelAction(index, state.selectedChannel == index)) }
                    )
                }
            }
            if (state.selectedChannel == Channel.NONE_ID) {
                StepSequencer(
                    sequenceId = state.sequenceId,
                    sequence = state.sequence,
                    modifier = Modifier.padding(paddingSmall),
                    onSequence = { sequence -> updatedOnAction(ChangeSequenceAction.Edit(state.sequenceId, sequence)) }
                )
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(buttonSmall)
                        .padding(start = paddingSmall, top = paddingSmall, bottom = paddingSmall),
                    verticalArrangement = Arrangement.spacedBy(paddingSmall)
                ) {
                    (1..6).forEachIndexed { index, name ->
                        TextButton(
                            text = name.toString(),
                            selected = state.selectedSetting == index,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxSize(),
                            enabled = state.settingsSize > index,
                            radioButton = true,
                            onClick = { updatedOnAction(SelectSettingAction(index)) }
                        )
                    }
                }
                ChaosPad(
                    settingId = state.settingId,
                    position = state.position,
                    horizontalText = state.hText,
                    verticalText = state.vText,
                    modifier = Modifier
                        .weight(1f)
                        .padding(paddingSmall),
                    onPosition = { updatedOnAction(ChangePositionAction(it)) }
                )
                PanFader(
                    panId = state.panId,
                    pan = state.pan,
                    modifier = Modifier
                        .padding(top = paddingSmall, end = paddingSmall, bottom = paddingSmall),
                    onPan = { updatedOnAction(ChangePanAction(it)) }
                )
            }
        }
    }

    if (state.showConfig) {
        ConfigDialog(
            configId = state.configId,
            tempo = state.tempo,
            swing = state.swing,
            ignoreAudioFocus = state.ignoreAudioFocus,
            onTempo = { updatedOnAction(ChangeTempoAction(it)) },
            onSwing = { updatedOnAction(ChangeSwingAction(it)) },
            onAudioFocus = { updatedOnAction(AudioFocusAction(it)) },
            onDismiss = { updatedOnAction(DismissAction) }
        )
    }
}

private enum class ChannelName { BD, SD, CP, TT, CB, HH }
