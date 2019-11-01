package net.simno.dmach.machine

import android.media.AudioManager
import io.reactivex.Flowable
import io.reactivex.FlowableTransformer
import io.reactivex.schedulers.Schedulers
import net.simno.dmach.data.Channel
import net.simno.dmach.data.Patch
import net.simno.dmach.data.withPan
import net.simno.dmach.data.withPosition
import net.simno.dmach.data.withSelectedSetting
import net.simno.dmach.db.Db
import net.simno.dmach.playback.AudioFocus
import net.simno.dmach.playback.PlaybackObserver
import net.simno.dmach.playback.PureData
import org.reactivestreams.Publisher

class MachineProcessor(
    private val pureData: PureData,
    private val audioFocus: AudioFocus,
    private val playbackObservers: Set<PlaybackObserver>,
    private val db: Db
) : FlowableTransformer<Action, Result> {

    override fun apply(actions: Flowable<Action>): Publisher<Result> = actions.publish { shared ->
        Flowable.mergeArray<Result>(
            shared.ofType(LoadAction::class.java).compose(load),
            shared.ofType(PlaybackAction::class.java).compose(playback),
            shared.ofType(PlayPauseAction::class.java).compose(playPause),
            shared.ofType(AudioFocusAction::class.java).compose(audioFocusProcessor),
            shared.ofType(ConfigAction::class.java).compose(config),
            shared.ofType(ChangeSeqenceAction::class.java).compose(changeSequence),
            shared.ofType(SelectChannelAction::class.java).compose(selectChannel),
            shared.ofType(SelectSettingAction::class.java).compose(selectSetting),
            shared.ofType(ChangePositionAction::class.java).compose(changePosition),
            shared.ofType(ChangePanAction::class.java).compose(changePan),
            shared.ofType(ChangeTempoAction::class.java).compose(changeTempo),
            shared.ofType(ChangeSwingAction::class.java).compose(changeSwing)
        )
    }

    private val load = FlowableTransformer<LoadAction, LoadResult> { actions ->
        actions
            .flatMap {
                db.activePatch()
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

    private val playback = FlowableTransformer<PlaybackAction, PlaybackResult> { actions ->
        actions
            .flatMap {
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

    private val playPause = FlowableTransformer<PlayPauseAction, PlayPauseResult> { actions ->
        actions
            .doOnNext {
                audioFocus.toggleFocus()
            }
            .computeResult {
                PlayPauseResult
            }
    }

    private val audioFocusProcessor = FlowableTransformer<AudioFocusAction, AudioFocusResult> { actions ->
        actions
            .doOnNext { action ->
                audioFocus.setIgnoreAudioFocus(action.ignoreAudioFocus)
            }
            .computeResult {
                AudioFocusResult(audioFocus.isIgnoreAudioFocus())
            }
    }

    private val config = FlowableTransformer<ConfigAction, ConfigResult> { actions ->
        actions
            .flatMap { action ->
                db.unsavedPatch()
                    .computeResult { patch ->
                        if (!action.showConfig) {
                            playbackObservers.forEach { it.updateInfo(patch.title, patch.tempo) }
                        }
                        ConfigResult(action.showConfig)
                    }
            }
    }

    private val changeSequence = FlowableTransformer<ChangeSeqenceAction, ChangeSequenceResult> { actions ->
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

    private val selectChannel = FlowableTransformer<SelectChannelAction, SelectChannelResult> { actions ->
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

    private val selectSetting = FlowableTransformer<SelectSettingAction, SelectSettingResult> { actions ->
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

    private val changePosition = FlowableTransformer<ChangePositionAction, ChangePositionResult> { actions ->
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

    private val changePan = FlowableTransformer<ChangePanAction, ChangePanResult> { actions ->
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

    private val changeTempo = FlowableTransformer<ChangeTempoAction, ChangeTempoResult> { actions ->
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

    private val changeSwing = FlowableTransformer<ChangeSwingAction, ChangeSwingResult> { actions ->
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

    private fun <T : Action> Flowable<T>.modifyPatch(modifier: (T, Patch) -> Patch): Flowable<Patch> = this
        .flatMap { action ->
            // Runs on db scheduler
            db.unsavedPatch()
                .map { patch ->
                    modifier(action, patch)
                }
                .doOnNext(db.acceptPatch())
        }

    private fun <T> Flowable<T>.sendToPureData(sender: (T) -> Unit): Flowable<T> = this
        .observeOn(Schedulers.computation())
        .doOnNext(sender)

    private fun <T, R : Result> Flowable<T>.computeResult(mapper: (T) -> R): Flowable<R> = this
        .observeOn(Schedulers.computation())
        .map(mapper)
}
