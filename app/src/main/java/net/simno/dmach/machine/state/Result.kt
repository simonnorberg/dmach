package net.simno.dmach.machine.state

import java.io.File
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.PersistentSet
import net.simno.dmach.data.Pan
import net.simno.dmach.data.Position
import net.simno.dmach.data.Steps
import net.simno.dmach.data.Swing
import net.simno.dmach.data.Tempo
import net.simno.dmach.settings.Settings

sealed class Result

data class ErrorResult(
    val error: Throwable
) : Result()

data class DebugResult(
    val debug: Boolean
) : Result()

data class LoadResult(
    val title: String,
    val sequenceId: Int,
    val sequence: PersistentList<Int>,
    val mutedChannels: PersistentSet<Int>,
    val selectedChannel: Int,
    val selectedSetting: Int,
    val settingId: Int,
    val settingsSize: Int,
    val hText: String,
    val vText: String,
    val position: Position,
    val panId: Int,
    val pan: Pan,
    val tempo: Tempo,
    val swing: Swing,
    val steps: Steps
) : Result()

data class ResumeResult(
    val settingId: Int,
    val position: Position,
    val panId: Int,
    val pan: Pan
) : Result()

data class PlaybackResult(
    val isPlaying: Boolean
) : Result()

data object PlayPauseResult : Result()

data class SettingsResult(
    val settings: Settings
) : Result()

data object ChangeSettingsResult : Result()

data class ConfigResult(
    val configId: Int
) : Result()

data object ExportResult : Result()

data class ExportFileResult(
    val waveFile: File?
) : Result()

data object DismissResult : Result()

data class ChangeSequenceResult(
    val sequenceId: Int,
    val sequence: PersistentList<Int>
) : Result()

data class MuteChannelResult(
    val mutedChannels: PersistentSet<Int>
) : Result()

data class SelectChannelResult(
    val selectedChannel: Int,
    val selectedSetting: Int,
    val settingId: Int,
    val settingsSize: Int,
    val hText: String,
    val vText: String,
    val position: Position,
    val panId: Int,
    val pan: Pan
) : Result()

data class SelectSettingResult(
    val selectedSetting: Int,
    val settingId: Int,
    val hText: String,
    val vText: String,
    val position: Position
) : Result()

data object ChangePositionResult : Result()

data object ChangePanResult : Result()

data class ChangeTempoResult(
    val tempo: Tempo
) : Result()

data class ChangeSwingResult(
    val swing: Swing
) : Result()

data class ChangeStepsResult(
    val steps: Steps,
    val sequenceId: Int
) : Result()

data class ChangePatchResult(
    val sequenceId: Int,
    val sequence: PersistentList<Int>,
    val panId: Int,
    val pan: Pan,
    val settingId: Int,
    val position: Position
) : Result()
