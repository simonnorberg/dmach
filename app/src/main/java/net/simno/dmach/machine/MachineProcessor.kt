package net.simno.dmach.machine

import android.media.AudioManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.flatMapMerge
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onEach
import net.simno.dmach.data.Channel
import net.simno.dmach.data.Patch
import net.simno.dmach.data.withPan
import net.simno.dmach.data.withPosition
import net.simno.dmach.data.withSelectedSetting
import net.simno.dmach.db.PatchRepository
import net.simno.dmach.playback.AudioFocus
import net.simno.dmach.playback.PlaybackObserver
import net.simno.dmach.playback.PureData

class MachineProcessor(
    private val pureData: PureData,
    private val audioFocus: AudioFocus,
    private val playbackObservers: Set<PlaybackObserver>,
    private val patchRepository: PatchRepository
) : (Flow<Action>) -> Flow<Result> {

    override fun invoke(actions: Flow<Action>): Flow<Result> = merge(
        actions.filterIsInstance<LoadAction>().let(load),
        actions.filterIsInstance<PlaybackAction>().let(playback),
        actions.filterIsInstance<PlayPauseAction>().let(playPause),
        actions.filterIsInstance<AudioFocusAction>().let(audioFocusProcessor),
        actions.filterIsInstance<ConfigAction>().let(config),
        actions.filterIsInstance<DismissAction>().let(dismiss),
        actions.filterIsInstance<ChangeSeqenceAction>().let(changeSequence),
        actions.filterIsInstance<SelectChannelAction>().let(selectChannel),
        actions.filterIsInstance<SelectSettingAction>().let(selectSetting),
        actions.filterIsInstance<ChangePositionAction>().let(changePosition),
        actions.filterIsInstance<ChangePanAction>().let(changePan),
        actions.filterIsInstance<ChangeTempoAction>().let(changeTempo),
        actions.filterIsInstance<ChangeSwingAction>().let(changeSwing)
    )

    private val load: (Flow<LoadAction>) -> Flow<LoadResult> = { actions ->
        actions
            .flatMapMerge {
                patchRepository.activePatch()
            }
            .sendToPureData { patch ->
                pureData.changeSequence(patch.sequence)
                pureData.changeTempo(patch.tempo)
                pureData.changeSwing(patch.swing)
                patch.channels.forEach { channel ->
                    pureData.changePan(channel.name, channel.pan)
                    channel.settings.forEach { setting ->
                        pureData.changeSetting(channel.name, setting)
                    }
                }
                playbackObservers.forEach { it.updateInfo(patch.title, patch.tempo) }
            }
            .computeResult { patch ->
                val channel = patch.channel
                LoadResult(
                    ignoreAudioFocus = audioFocus.isIgnoreAudioFocus(),
                    sequence = patch.sequence,
                    selectedChannel = patch.selectedChannel,
                    selectedSetting = channel.selectedSetting,
                    settingsSize = channel.settings.size,
                    hText = channel.setting.hText,
                    vText = channel.setting.vText,
                    position = channel.setting.position,
                    pan = channel.pan,
                    tempo = patch.tempo,
                    swing = patch.swing
                )
            }
    }

    private val playback: (Flow<PlaybackAction>) -> Flow<PlaybackResult> = { actions ->
        actions
            .flatMapMerge {
                audioFocus.audioFocus()
            }
            .map { audioFocus ->
                audioFocus == AudioManager.AUDIOFOCUS_GAIN
            }
            .sendToPureData { hasFocus ->
                if (hasFocus) {
                    playbackObservers.forEach { it.onPlaybackStart() }
                } else {
                    playbackObservers.forEach { it.onPlaybackStop() }
                }
            }
            .computeResult { isPlaying ->
                PlaybackResult(isPlaying)
            }
    }

    private val playPause: (Flow<PlayPauseAction>) -> Flow<PlayPauseResult> = { actions ->
        actions
            .onEach {
                audioFocus.toggleFocus()
            }
            .computeResult {
                PlayPauseResult
            }
    }

    private val audioFocusProcessor: (Flow<AudioFocusAction>) -> Flow<AudioFocusResult> = { actions ->
        actions
            .onEach { action ->
                audioFocus.setIgnoreAudioFocus(action.ignoreAudioFocus)
            }
            .computeResult {
                AudioFocusResult(audioFocus.isIgnoreAudioFocus())
            }
    }

    private val config: (Flow<ConfigAction>) -> Flow<ConfigResult> = { actions ->
        actions
            .computeResult {
                ConfigResult
            }
    }

    private val dismiss: (Flow<DismissAction>) -> Flow<DismissResult> = { actions ->
        actions
            .computeResult {
                val patch = patchRepository.unsavedPatch()
                playbackObservers.forEach { it.updateInfo(patch.title, patch.tempo) }
                DismissResult
            }
    }

    private val changeSequence: (Flow<ChangeSeqenceAction>) -> Flow<ChangeSequenceResult> = { actions ->
        actions
            .modifyPatch { action, patch ->
                patch.copy(sequence = action.sequence)
            }
            .sendToPureData { patch ->
                pureData.changeSequence(patch.sequence)
            }
            .computeResult { patch ->
                ChangeSequenceResult(patch.sequence)
            }
    }

    private val selectChannel: (Flow<SelectChannelAction>) -> Flow<SelectChannelResult> = { actions ->
        actions
            .modifyPatch { action, patch ->
                val selectedChannel = if (action.isSelected) Channel.NONE_ID else action.channel
                patch.copy(selectedChannel = selectedChannel)
            }
            .computeResult { patch ->
                val channel = patch.channel
                SelectChannelResult(
                    patch.selectedChannel,
                    channel.selectedSetting,
                    channel.settings.size,
                    channel.setting.hText,
                    channel.setting.vText,
                    channel.setting.position,
                    channel.pan
                )
            }
    }

    private val selectSetting: (Flow<SelectSettingAction>) -> Flow<SelectSettingResult> = { actions ->
        actions
            .modifyPatch { action, patch ->
                patch.withSelectedSetting(action.setting)
            }
            .computeResult { patch ->
                val channel = patch.channel
                SelectSettingResult(
                    channel.selectedSetting,
                    channel.setting.hText,
                    channel.setting.vText,
                    channel.setting.position,
                    channel.pan
                )
            }
    }

    private val changePosition: (Flow<ChangePositionAction>) -> Flow<ChangePositionResult> = { actions ->
        actions
            .modifyPatch { action, patch ->
                patch.withPosition(action.position)
            }
            .sendToPureData { patch ->
                val channel = patch.channel
                pureData.changeSetting(channel.name, channel.setting)
            }
            .computeResult {
                ChangePositionResult
            }
    }

    private val changePan: (Flow<ChangePanAction>) -> Flow<ChangePanResult> = { actions ->
        actions
            .modifyPatch { action, patch ->
                patch.withPan(action.pan)
            }
            .sendToPureData { patch ->
                val channel = patch.channel
                pureData.changePan(channel.name, channel.pan)
            }
            .computeResult {
                ChangePanResult
            }
    }

    private val changeTempo: (Flow<ChangeTempoAction>) -> Flow<ChangeTempoResult> = { actions ->
        actions
            .modifyPatch { action, patch ->
                patch.copy(tempo = action.tempo)
            }
            .sendToPureData { patch ->
                pureData.changeTempo(patch.tempo)
            }
            .computeResult { patch ->
                ChangeTempoResult(patch.tempo)
            }
    }

    private val changeSwing: (Flow<ChangeSwingAction>) -> Flow<ChangeSwingResult> = { actions ->
        actions
            .modifyPatch { action, patch ->
                patch.copy(swing = action.swing)
            }
            .sendToPureData { patch ->
                pureData.changeSwing(patch.swing)
            }
            .computeResult { patch ->
                ChangeSwingResult(patch.swing)
            }
    }

    private fun <T : Action> Flow<T>.modifyPatch(modifier: (T, Patch) -> Patch): Flow<Patch> = this
        .map { action -> modifier(action, patchRepository.unsavedPatch()) }
        .onEach { patchRepository.acceptPatch(it) }

    private fun <T> Flow<T>.sendToPureData(sender: suspend (T) -> Unit): Flow<T> = onEach(sender)

    private fun <T, R : Result> Flow<T>.computeResult(mapper: suspend (T) -> R): Flow<R> = map(mapper)
}
