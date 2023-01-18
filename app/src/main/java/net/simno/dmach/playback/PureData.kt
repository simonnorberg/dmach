package net.simno.dmach.playback

import androidx.annotation.Size
import net.simno.dmach.data.Pan
import net.simno.dmach.data.Patch
import net.simno.dmach.data.Setting
import net.simno.dmach.data.Steps
import net.simno.dmach.data.Swing
import net.simno.dmach.data.Tempo
import net.simno.dmach.util.logSequence
import org.puredata.core.PdBase

class PureData {

    fun startPlayback() {
        PdBase.sendBang("play")
    }

    fun stopPlayback() {
        PdBase.sendBang("stop")
    }

    fun changeSequence(@Size(32) sequence: List<Int>) {
        logSequence("PureDataController", sequence)
        for (step in 0 until Patch.STEPS) {
            PdBase.sendList("step", 0, step, sequence[step])
            PdBase.sendList("step", 1, step, sequence[step + Patch.STEPS])
        }
    }

    fun changeSetting(channel: String, setting: Setting) {
        PdBase.sendList(channel, setting.hIndex, setting.x)
        PdBase.sendList(channel, setting.vIndex, setting.y)
    }

    fun changePan(channel: String, pan: Pan) {
        PdBase.sendFloat(channel + "p", pan.value)
    }

    fun changeTempo(tempo: Tempo) {
        PdBase.sendFloat("tempo", tempo.value.toFloat())
    }

    fun changeSwing(swing: Swing) {
        PdBase.sendFloat("swing", swing.value / 100f)
    }

    fun changeSteps(steps: Steps) {
        PdBase.sendFloat("steps", steps.value.coerceIn(8, 16).toFloat())
    }
}
