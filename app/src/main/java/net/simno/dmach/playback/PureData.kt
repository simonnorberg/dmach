package net.simno.dmach.playback

import androidx.annotation.Size
import net.simno.dmach.data.Pan
import net.simno.dmach.data.Patch
import net.simno.dmach.data.Setting
import net.simno.dmach.data.Steps
import net.simno.dmach.data.Swing
import net.simno.dmach.data.Tempo
import net.simno.dmach.util.logSequence
import net.simno.kortholt.Kortholt

class PureData(
    private val kortholt: Kortholt.Player
) {

    fun startPlayback() {
        kortholt.sendBang("play")
    }

    fun stopPlayback() {
        kortholt.sendBang("stop")
    }

    fun changeSequence(@Size(32) sequence: List<Int>) {
        logSequence("PureData", sequence)
        for (step in 0 until Patch.STEPS) {
            kortholt.sendList("step", 0, step, sequence[step])
            kortholt.sendList("step", 1, step, sequence[step + Patch.STEPS])
        }
    }

    fun changeSetting(channel: String, setting: Setting) {
        kortholt.sendList(channel, setting.hIndex, setting.x)
        kortholt.sendList(channel, setting.vIndex, setting.y)
    }

    fun changePan(channel: String, pan: Pan) {
        kortholt.sendFloat(channel + "p", pan.value)
    }

    fun changeTempo(tempo: Tempo) {
        kortholt.sendFloat("tempo", tempo.value.toFloat())
    }

    fun changeSwing(swing: Swing) {
        kortholt.sendFloat("swing", swing.value / 100f)
    }

    fun changeSteps(steps: Steps) {
        kortholt.sendFloat("steps", steps.value.coerceIn(8, 16).toFloat())
    }
}
