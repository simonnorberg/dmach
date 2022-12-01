package net.simno.dmach.machine.state

import net.simno.dmach.data.Patch
import net.simno.dmach.data.Position
import kotlin.random.Random

sealed class Action

object LoadAction : Action()

object PlaybackAction : Action()

object PlayPauseAction : Action()

data class AudioFocusAction(
    val ignoreAudioFocus: Boolean
) : Action()

object ConfigAction : Action()

object DismissAction : Action()

sealed class ChangeSequenceAction(
    open val sequenceId: Int,
    open val sequence: List<Int>
) : Action() {
    data class Edit(
        override val sequenceId: Int,
        override val sequence: List<Int>
    ) : ChangeSequenceAction(sequenceId, sequence)

    class Randomize : ChangeSequenceAction(Random.Default.nextInt(), Patch.RANDOM_SEQUENCE)
    object Empty : ChangeSequenceAction(Random.Default.nextInt(), Patch.EMPTY_SEQUENCE)
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
    val pan: Float
) : Action()

data class ChangeTempoAction(
    val tempo: Int
) : Action()

data class ChangeSwingAction(
    val swing: Int
) : Action()