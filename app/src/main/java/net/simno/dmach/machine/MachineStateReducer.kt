package net.simno.dmach.machine

import net.simno.dmach.util.logError

object MachineStateReducer : (ViewState, Result) -> ViewState {
    override fun invoke(previousState: ViewState, result: Result) = when (result) {
        is ErrorResult -> {
            logError("MachineStateReducer", "ErrorResult", result.error)
            previousState
        }
        is LoadResult -> previousState.copy(
            title = result.title,
            ignoreAudioFocus = result.ignoreAudioFocus,
            sequenceId = result.sequenceId,
            sequence = result.sequence,
            selectedChannel = result.selectedChannel,
            selectedSetting = result.selectedSetting,
            settingsSize = result.settingsSize,
            settingId = result.settingId,
            hText = result.hText,
            vText = result.vText,
            position = result.position,
            panId = result.panId,
            pan = result.pan,
            tempo = result.tempo,
            swing = result.swing
        )
        is PlaybackResult -> previousState.copy(
            isPlaying = result.isPlaying,
            position = null,
            pan = null
        )
        PlayPauseResult -> previousState.copy(
            position = null,
            pan = null
        )
        is AudioFocusResult -> previousState.copy(
            ignoreAudioFocus = result.ignoreAudioFocus,
            position = null,
            pan = null
        )
        is ConfigResult -> previousState.copy(
            showConfig = true,
            configId = result.configId,
            position = null,
            pan = null
        )
        DismissResult -> previousState.copy(
            showConfig = false,
            position = null,
            pan = null
        )
        is ChangeSequenceResult -> previousState.copy(
            sequenceId = result.sequenceId,
            sequence = result.sequence,
            position = null,
            pan = null
        )
        is SelectChannelResult -> previousState.copy(
            selectedChannel = result.selectedChannel,
            selectedSetting = result.selectedSetting,
            settingId = result.settingId,
            settingsSize = result.settingsSize,
            hText = result.hText,
            vText = result.vText,
            position = result.position,
            panId = result.panId,
            pan = result.pan
        )
        is SelectSettingResult -> previousState.copy(
            selectedSetting = result.selectedSetting,
            settingId = result.settingId,
            hText = result.hText,
            vText = result.vText,
            position = result.position
        )
        ChangePositionResult -> previousState.copy(
            position = null,
            pan = null
        )
        ChangePanResult -> previousState.copy(
            position = null,
            pan = null
        )
        is ChangeTempoResult -> previousState.copy(
            position = null,
            pan = null,
            tempo = result.tempo
        )
        is ChangeSwingResult -> previousState.copy(
            position = null,
            pan = null,
            swing = result.swing
        )
    }
}
