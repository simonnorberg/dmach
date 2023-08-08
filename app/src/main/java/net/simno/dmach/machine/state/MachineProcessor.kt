package net.simno.dmach.machine.state

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.flatMapMerge
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onEach
import net.simno.dmach.data.Channel
import net.simno.dmach.data.Pan
import net.simno.dmach.data.Patch
import net.simno.dmach.data.defaultPatch
import net.simno.dmach.data.mutedSequence
import net.simno.dmach.data.withPan
import net.simno.dmach.data.withPosition
import net.simno.dmach.data.withSelectedSetting
import net.simno.dmach.db.PatchRepository
import net.simno.dmach.playback.AudioFocus
import net.simno.dmach.playback.PlaybackController
import net.simno.dmach.playback.PureData
import net.simno.dmach.playback.WaveExporter
import net.simno.dmach.settings.SettingsRepository

class MachineProcessor(
    private val playbackController: PlaybackController,
    private val pureData: PureData,
    private val waveExporter: WaveExporter,
    private val audioFocus: AudioFocus,
    private val patchRepository: PatchRepository,
    private val settingsRepository: SettingsRepository,
    private val random: Randomizer = Randomizer.DEFAULT
) : (Flow<Action>) -> Flow<Result> {

    override fun invoke(actions: Flow<Action>): Flow<Result> = merge(
        actions.filterIsInstance<LoadAction>().let(load),
        actions.filterIsInstance<PlaybackAction>().let(playback),
        actions.filterIsInstance<PlayPauseAction>().let(playPause),
        actions.filterIsInstance<SettingsAction>().let(settings),
        actions.filterIsInstance<ChangeSettingsAction>().let(changeSettings),
        actions.filterIsInstance<ConfigAction>().let(config),
        actions.filterIsInstance<ExportAction>().let(export),
        actions.filterIsInstance<ExportFileAction>().let(exportFile),
        actions.filterIsInstance<DismissAction>().let(dismiss),
        actions.filterIsInstance<ChangeSequenceAction>().let(changeSequence),
        actions.filterIsInstance<MuteChannelAction>().let(muteChannel),
        actions.filterIsInstance<SelectChannelAction>().let(selectChannel),
        actions.filterIsInstance<SelectSettingAction>().let(selectSetting),
        actions.filterIsInstance<ChangePositionAction>().let(changePosition),
        actions.filterIsInstance<ChangePanAction>().let(changePan),
        actions.filterIsInstance<ChangeTempoAction>().let(changeTempo),
        actions.filterIsInstance<ChangeSwingAction>().let(changeSwing),
        actions.filterIsInstance<ChangeStepsAction>().let(changeSteps),
        actions.filterIsInstance<ChangePatchAction>().let(changePatch)
    )

    private val load: (Flow<LoadAction>) -> Flow<LoadResult> = { actions ->
        actions
            .onEach {
                playbackController.openPatch()
            }
            .flatMapMerge {
                patchRepository.activePatch()
            }
            .sendToPureData { patch ->
                pureData.changeSequence(patch.mutedSequence())
                pureData.changeTempo(patch.tempo)
                pureData.changeSwing(patch.swing)
                pureData.changeSteps(patch.steps)
                patch.channels.forEach { channel ->
                    pureData.changePan(channel.name, channel.pan)
                    channel.settings.forEach { setting ->
                        pureData.changeSetting(channel.name, setting)
                    }
                }
                playbackController.updateInfo(patch.title, patch.tempo)
            }
            .computeResult { patch ->
                val channel = patch.channel
                LoadResult(
                    title = patch.title,
                    sequenceId = random.nextInt(),
                    sequence = patch.sequence,
                    mutedChannels = patch.mutedChannels,
                    selectedChannel = patch.selectedChannel,
                    selectedSetting = channel.selectedSetting,
                    settingId = random.nextInt(),
                    settingsSize = channel.settings.size,
                    hText = channel.setting.hText,
                    vText = channel.setting.vText,
                    position = channel.setting.position,
                    panId = random.nextInt(),
                    pan = channel.pan,
                    tempo = patch.tempo,
                    swing = patch.swing,
                    steps = patch.steps
                )
            }
    }

    private val playback: (Flow<PlaybackAction>) -> Flow<PlaybackResult> = { actions ->
        actions
            .flatMapMerge {
                audioFocus.audioFocus
            }
            .sendToPureData { hasFocus ->
                if (hasFocus) {
                    playbackController.startPlayback()
                } else {
                    playbackController.stopPlayback()
                }
            }
            .computeResult { isPlaying ->
                PlaybackResult(isPlaying)
            }
    }

    private val playPause: (Flow<PlayPauseAction>) -> Flow<PlayPauseResult> = { actions ->
        actions
            .onEach { action ->
                if (action.play) {
                    audioFocus.requestAudioFocus()
                } else {
                    audioFocus.abandonAudioFocus()
                }
            }
            .computeResult {
                PlayPauseResult
            }
    }

    private val settings: (Flow<SettingsAction>) -> Flow<SettingsResult> = { actions ->
        actions
            .flatMapMerge {
                settingsRepository.settings
            }
            .computeResult { settings ->
                SettingsResult(settings)
            }
    }

    private val changeSettings: (Flow<ChangeSettingsAction>) -> Flow<ChangeSettingsResult> = { actions ->
        actions
            .onEach { action ->
                settingsRepository.updateSettings(action.settings)
            }
            .computeResult {
                ChangeSettingsResult
            }
    }

    private val config: (Flow<ConfigAction>) -> Flow<ConfigResult> = { actions ->
        actions
            .computeResult {
                ConfigResult(random.nextInt())
            }
    }

    private val export: (Flow<ExportAction>) -> Flow<ExportResult> = { actions ->
        actions
            .onEach {
                audioFocus.abandonAudioFocus()
            }
            .computeResult {
                ExportResult
            }
    }

    private val exportFile: (Flow<ExportFileAction>) -> Flow<ExportFileResult> = { actions ->
        actions
            .computeResult { action ->
                val waveFile = waveExporter.saveWaveFile(
                    title = action.title,
                    tempo = action.tempo,
                    steps = action.steps
                )
                ExportFileResult(waveFile)
            }
    }

    private val dismiss: (Flow<DismissAction>) -> Flow<DismissResult> = { actions ->
        actions
            .computeResult {
                val patch = patchRepository.unsavedPatch()
                playbackController.updateInfo(patch.title, patch.tempo)
                DismissResult
            }
    }

    private val changeSequence: (Flow<ChangeSequenceAction>) -> Flow<ChangeSequenceResult> = { actions ->
        actions
            .modifyPatch { action, patch ->
                patch.copy(sequence = action.sequence)
            }
            .sendToPureData { pwa ->
                pureData.changeSequence(pwa.patch.mutedSequence())
            }
            .computeResult { pwa ->
                ChangeSequenceResult(pwa.action.sequenceId, pwa.patch.sequence)
            }
    }

    private val muteChannel: (Flow<MuteChannelAction>) -> Flow<MuteChannelResult> = { actions ->
        actions
            .modifyPatch { action, patch ->
                val mutedChannels = if (action.isMuted) {
                    patch.mutedChannels + action.channel
                } else {
                    patch.mutedChannels - action.channel
                }
                patch.copy(mutedChannels = mutedChannels)
            }
            .sendToPureData { pwa ->
                pureData.changeSequence(pwa.patch.mutedSequence())
            }
            .computeResult { pwa ->
                MuteChannelResult(
                    mutedChannels = pwa.patch.mutedChannels
                )
            }
    }

    private val selectChannel: (Flow<SelectChannelAction>) -> Flow<SelectChannelResult> = { actions ->
        actions
            .modifyPatch { action, patch ->
                val selectedChannel = if (action.isSelected) Channel.NONE_ID else action.channel
                patch.copy(selectedChannel = selectedChannel)
            }
            .computeResult { pwa ->
                val channel = pwa.patch.channel
                SelectChannelResult(
                    selectedChannel = pwa.patch.selectedChannel,
                    selectedSetting = channel.selectedSetting,
                    settingId = random.nextInt(),
                    settingsSize = channel.settings.size,
                    hText = channel.setting.hText,
                    vText = channel.setting.vText,
                    position = channel.setting.position,
                    panId = random.nextInt(),
                    pan = channel.pan
                )
            }
    }

    private val selectSetting: (Flow<SelectSettingAction>) -> Flow<SelectSettingResult> = { actions ->
        actions
            .modifyPatch { action, patch ->
                patch.withSelectedSetting(action.setting)
            }
            .computeResult { pwa ->
                val channel = pwa.patch.channel
                SelectSettingResult(
                    selectedSetting = channel.selectedSetting,
                    settingId = random.nextInt(),
                    hText = channel.setting.hText,
                    vText = channel.setting.vText,
                    position = channel.setting.position
                )
            }
    }

    private val changePosition: (Flow<ChangePositionAction>) -> Flow<ChangePositionResult> = { actions ->
        actions
            .modifyPatch { action, patch ->
                patch.withPosition(action.position)
            }
            .sendToPureData { pwa ->
                val channel = pwa.patch.channel
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
            .sendToPureData { pwa ->
                val channel = pwa.patch.channel
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
            .sendToPureData { pwa ->
                pureData.changeTempo(pwa.patch.tempo)
            }
            .computeResult { pwa ->
                ChangeTempoResult(pwa.patch.tempo)
            }
    }

    private val changeSwing: (Flow<ChangeSwingAction>) -> Flow<ChangeSwingResult> = { actions ->
        actions
            .modifyPatch { action, patch ->
                patch.copy(swing = action.swing)
            }
            .sendToPureData { pwa ->
                pureData.changeSwing(pwa.patch.swing)
            }
            .computeResult { pwa ->
                ChangeSwingResult(pwa.action.swing)
            }
    }

    private val changeSteps: (Flow<ChangeStepsAction>) -> Flow<ChangeStepsResult> = { actions ->
        actions
            .modifyPatch { action, patch ->
                patch.copy(steps = action.steps)
            }
            .sendToPureData { pwa ->
                pureData.changeSteps(pwa.patch.steps)
            }
            .computeResult { pwa ->
                ChangeStepsResult(
                    steps = pwa.action.steps,
                    sequenceId = random.nextInt()
                )
            }
    }

    private val changePatch: (Flow<ChangePatchAction>) -> Flow<ChangePatchResult> = { actions ->
        actions
            .modifyPatch { action, patch ->
                val s = action.settings
                val reset = action is ChangePatchAction.Reset
                val defaultPatch = defaultPatch()
                patch.copy(
                    sequence = when {
                        s.sequenceEnabled && reset -> Patch.EMPTY_SEQUENCE
                        s.sequenceEnabled -> random.nextSequence()
                        else -> patch.sequence
                    },
                    channels = patch.channels.mapIndexed { chIndex, channel ->
                        channel.copy(
                            pan = when {
                                s.panEnabled && reset -> defaultPatch.channels[chIndex].pan
                                s.panEnabled -> Pan(random.nextFloat())
                                else -> channel.pan
                            },
                            settings = channel.settings.mapIndexed { sIndex, setting ->
                                when {
                                    s.soundEnabled && reset -> defaultPatch.channels[chIndex].settings[sIndex]
                                    s.soundEnabled -> setting.copy(x = random.nextFloat(), y = random.nextFloat())
                                    else -> setting
                                }
                            }
                        )
                    }
                )
            }
            .sendToPureData { pwa ->
                pureData.changeSequence(pwa.patch.mutedSequence())
                pwa.patch.channels.forEach { channel ->
                    pureData.changePan(channel.name, channel.pan)
                    channel.settings.forEach { setting ->
                        pureData.changeSetting(channel.name, setting)
                    }
                }
            }
            .computeResult { pwa ->
                val channel = pwa.patch.channel
                ChangePatchResult(
                    sequenceId = random.nextInt(),
                    sequence = pwa.patch.sequence,
                    panId = random.nextInt(),
                    pan = channel.pan,
                    settingId = random.nextInt(),
                    position = channel.setting.position
                )
            }
    }

    private fun <T : Action> Flow<T>.modifyPatch(modifier: (T, Patch) -> (Patch)): Flow<PatchWithAction<T>> = this
        .map { action -> PatchWithAction(modifier(action, patchRepository.unsavedPatch()), action) }
        .onEach { patchRepository.acceptPatch(it.patch) }

    private fun <T> Flow<T>.sendToPureData(sender: suspend (T) -> Unit): Flow<T> = onEach(sender)

    private fun <T, R : Result> Flow<T>.computeResult(mapper: suspend (T) -> R): Flow<R> = map(mapper)

    private data class PatchWithAction<T : Action>(
        val patch: Patch,
        val action: T
    )
}
