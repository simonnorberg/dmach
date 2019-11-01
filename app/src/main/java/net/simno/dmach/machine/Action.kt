package net.simno.dmach.machine

import net.simno.dmach.data.Position

sealed class Action

object LoadAction : Action()

object PlaybackAction : Action()

object PlayPauseAction : Action()

data class AudioFocusAction(
    val ignoreAudioFocus: Boolean
) : Action()

data class ConfigAction(
    val showConfig: Boolean
) : Action()

data class ChangeSeqenceAction(
    val sequence: List<Int>
) : Action()

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
