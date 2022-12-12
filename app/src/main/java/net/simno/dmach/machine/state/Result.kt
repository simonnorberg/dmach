package net.simno.dmach.machine.state

import net.simno.dmach.data.Pan
import net.simno.dmach.data.Position
import net.simno.dmach.data.Steps
import net.simno.dmach.data.Swing
import net.simno.dmach.data.Tempo
import java.io.File

sealed class Result

data class ErrorResult(
    val error: Throwable
) : Result()

data class LoadResult(
    val title: String,
    val ignoreAudioFocus: Boolean,
    val sequenceId: Int,
    val sequence: List<Int>,
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

data class PlaybackResult(
    val isPlaying: Boolean
) : Result()

object PlayPauseResult : Result()

data class AudioFocusResult(
    val ignoreAudioFocus: Boolean
) : Result()

data class ConfigResult(
    val configId: Int
) : Result()

object ExportResult : Result()

data class ExportFileResult(
    val waveFile: File?
) : Result()

object DismissResult : Result()

data class ChangeSequenceResult(
    val sequenceId: Int,
    val sequence: List<Int>
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

object ChangePositionResult : Result()

object ChangePanResult : Result()

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
