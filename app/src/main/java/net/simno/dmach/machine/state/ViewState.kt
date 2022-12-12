package net.simno.dmach.machine.state

import net.simno.dmach.data.Channel
import net.simno.dmach.data.Pan
import net.simno.dmach.data.Patch
import net.simno.dmach.data.Position
import net.simno.dmach.data.Steps
import net.simno.dmach.data.Swing
import net.simno.dmach.data.Tempo
import java.io.File

data class ViewState(
    val title: String = "",
    val isPlaying: Boolean = false,
    val showConfig: Boolean = false,
    val configId: Int = 0,
    val showExport: Boolean = false,
    val startExport: Boolean = false,
    val waveFile: File? = null,
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
    val pan: Pan? = Channel.NONE.pan,
    val tempo: Tempo = Tempo(120),
    val swing: Swing = Swing(0),
    val steps: Steps = Steps(16)
)
