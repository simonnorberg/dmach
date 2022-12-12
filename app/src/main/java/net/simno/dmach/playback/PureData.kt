package net.simno.dmach.playback

import androidx.annotation.Size
import net.simno.dmach.data.Pan
import net.simno.dmach.data.Setting
import net.simno.dmach.data.Steps
import net.simno.dmach.data.Swing
import net.simno.dmach.data.Tempo

interface PureData {
    fun changeSequence(@Size(32) sequence: List<Int>)
    fun changeSetting(channel: String, setting: Setting)
    fun changePan(channel: String, pan: Pan)
    fun changeTempo(tempo: Tempo)
    fun changeSwing(swing: Swing)
    fun changeSteps(steps: Steps)
}
