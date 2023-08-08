package net.simno.dmach.machine.state

import net.simno.dmach.data.Pan
import net.simno.dmach.data.Position
import net.simno.dmach.data.Steps
import net.simno.dmach.data.Swing
import net.simno.dmach.data.Tempo
import net.simno.dmach.settings.Settings

sealed class Action

data object LoadAction : Action()

data object PlaybackAction : Action()

data class PlayPauseAction(
    val play: Boolean
) : Action()

data object SettingsAction : Action()

data class ChangeSettingsAction(
    val settings: Settings
) : Action()

data object ConfigAction : Action()

data object ExportAction : Action()

data class ExportFileAction(
    val title: String,
    val tempo: Tempo,
    val steps: Steps
) : Action()

data object DismissAction : Action()

data class ChangeSequenceAction(
    val sequenceId: Int,
    val sequence: List<Int>
) : Action()

data class MuteChannelAction(
    val channel: Int,
    val isMuted: Boolean
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

sealed class ChangePatchAction(
    open val settings: Settings
) : Action() {
    data class Reset(override val settings: Settings) : ChangePatchAction(settings)
    data class Randomize(override val settings: Settings) : ChangePatchAction(settings)
}
