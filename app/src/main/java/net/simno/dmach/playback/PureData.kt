package net.simno.dmach.playback

import androidx.annotation.Size
import net.simno.dmach.data.Setting

interface PureData {
    fun changeSequence(@Size(32) sequence: List<Int>)
    fun changeSetting(channel: String, setting: Setting)
    fun changePan(channel: String, pan: Float)
    fun changeTempo(tempo: Int)
    fun changeSwing(swing: Int)
}
