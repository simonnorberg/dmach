package net.simno.dmach.machine

import net.simno.dmach.data.Channel
import net.simno.dmach.data.Patch
import net.simno.dmach.data.Position

data class ViewState(
    val title: String = "",
    val isPlaying: Boolean = false,
    val showConfig: Boolean = false,
    val configId: Int = 0,
    val ignoreAudioFocus: Boolean = false,
    val sequenceId: Int = 0,
    val sequence: List<Int> = Patch.EMPTY_SEQUENCE,
    val selectedChannel: Int = Channel.NONE_ID,
    val selectedSetting: Int = Channel.NONE.selectedSetting,
    val settingId: Int = 0,
    val settingsSize: Int = Channel.NONE.settings.size,
    val hText: String = Channel.NONE.setting.hText,
    val vText: String = Channel.NONE.setting.vText,
    val position: Position? = Channel.NONE.setting.position,
    val panId: Int = 0,
    val pan: Float? = Channel.NONE.pan,
    val tempo: Int = 120,
    val swing: Int = 0
)
