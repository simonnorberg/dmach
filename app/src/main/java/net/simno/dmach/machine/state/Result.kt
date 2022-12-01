package net.simno.dmach.machine.state

import net.simno.dmach.data.Position

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
    val pan: Float,
    val tempo: Int,
    val swing: Int
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
    val pan: Float
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
    val tempo: Int
) : Result()

data class ChangeSwingResult(
    val swing: Int
) : Result()
