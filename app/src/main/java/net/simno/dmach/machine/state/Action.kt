package net.simno.dmach.machine.state

import net.simno.dmach.data.Pan
import net.simno.dmach.data.Patch
import net.simno.dmach.data.Position
import net.simno.dmach.data.Steps
import net.simno.dmach.data.Swing
import net.simno.dmach.data.Tempo
import kotlin.random.Random

sealed class Action

object LoadAction : Action()

object PlaybackAction : Action()

object PlayPauseAction : Action()

data class AudioFocusAction(
    val ignoreAudioFocus: Boolean
) : Action()

object ConfigAction : Action()

object ExportAction : Action()

data class ExportFileAction(
    val title: String,
    val tempo: Tempo,
    val steps: Steps
) : Action()

object DismissAction : Action()

sealed class ChangeSequenceAction(
    open val sequenceId: Int,
    open val sequence: List<Int>
) : Action() {
    data class Edit(
        override val sequenceId: Int,
        override val sequence: List<Int>
    ) : ChangeSequenceAction(sequenceId, sequence)

    data class Randomize(val id: Int = Random.nextInt()) : ChangeSequenceAction(id, Patch.RANDOM_SEQUENCE)
    data class Empty(val id: Int = Random.nextInt()) : ChangeSequenceAction(id, Patch.EMPTY_SEQUENCE)
}

data class SelectChannelAction(
    val channel: Int,
    val isSelected: Boolean
) : Action()

data class SelectSettingAction(
    val setting: Int
) : Action()

data class ChangePositionAction(
    val position: Position
) : Action()

data class ChangePanAction(
    val pan: Pan
) : Action()

data class ChangeTempoAction(
    val tempo: Tempo
) : Action()

data class ChangeSwingAction(
    val swing: Swing
) : Action()

data class ChangeStepsAction(
    val steps: Steps
) : Action()
