package net.simno.dmach.machine.state

import java.io.File
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.PersistentSet
import kotlinx.collections.immutable.persistentSetOf
import net.simno.dmach.data.Channel
import net.simno.dmach.data.Pan
import net.simno.dmach.data.Patch
import net.simno.dmach.data.Position
import net.simno.dmach.data.Steps
import net.simno.dmach.data.Swing
import net.simno.dmach.data.Tempo
import net.simno.dmach.settings.Settings

data class ViewState(
    val title: String = "",
    val isPlaying: Boolean = false,
    val showConfig: Boolean = false,
    val configId: Int = 0,
    val showExport: Boolean = false,
    val startExport: Boolean = false,
    val waveFile: File? = null,
    val settings: Settings = Settings(),
    val sequenceId: Int = 0,
    val sequence: PersistentList<Int> = Patch.EMPTY_SEQUENCE,
    val mutedChannels: PersistentSet<Int> = persistentSetOf(),
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
