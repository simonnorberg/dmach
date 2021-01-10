package net.simno.dmach.machine

import net.simno.dmach.data.Position

sealed class Result

data class ErrorResult(
    val error: Throwable
) : Result()

data class LoadResult(
    val ignoreAudioFocus: Boolean,
    val sequence: List<Int>,
    val selectedChannel: Int,
    val selectedSetting: Int,
    val settingsSize: Int,
    val hText: String,
    val vText: String,
    val position: Position,
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

object ConfigResult : Result()

object DismissResult : Result()

data class ChangeSequenceResult(
    val sequence: List<Int>
) : Result()

data class SelectChannelResult(
    val selectedChannel: Int,
    val selectedSetting: Int,
    val settingsSize: Int,
    val hText: String,
    val vText: String,
    val position: Position,
    val pan: Float
) : Result()

data class SelectSettingResult(
    val selectedSetting: Int,
    val hText: String,
    val vText: String,
    val position: Position,
    val pan: Float
) : Result()

object ChangePositionResult : Result()

object ChangePanResult : Result()

data class ChangeTempoResult(
    val tempo: Int
) : Result()

data class ChangeSwingResult(
    val swing: Int
) : Result()
