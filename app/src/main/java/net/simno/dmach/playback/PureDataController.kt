package net.simno.dmach.playback

import android.content.Context
import androidx.annotation.Size
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import net.simno.dmach.R
import net.simno.dmach.data.Pan
import net.simno.dmach.data.Patch
import net.simno.dmach.data.Setting
import net.simno.dmach.data.Steps
import net.simno.dmach.data.Swing
import net.simno.dmach.data.Tempo
import net.simno.dmach.util.logSequence
import net.simno.kortholt.PdBaseHelper
import org.puredata.core.PdBase

class PureDataController(
    context: Context
) : PureData, PlaybackObserver, DefaultLifecycleObserver {

    init {
        PdBaseHelper.openPatch(context, R.raw.dmach, "dmach.pd", extractZip = true)
    }

    override fun onPlaybackStart() {
        PdBase.sendBang("play")
    }

    override fun onPlaybackStop() {
        PdBase.sendBang("stop")
    }

    override fun updateInfo(title: String, tempo: Tempo) {
    }

    override fun changeSequence(@Size(32) sequence: List<Int>) {
        logSequence("PureDataController", sequence)
        for (step in 0 until Patch.STEPS) {
            PdBase.sendList("step", 0, step, sequence[step])
            PdBase.sendList("step", 1, step, sequence[step + Patch.STEPS])
        }
    }

    override fun changeSetting(channel: String, setting: Setting) {
        PdBase.sendList(channel, setting.hIndex, setting.x)
        PdBase.sendList(channel, setting.vIndex, setting.y)
    }

    override fun changePan(channel: String, pan: Pan) {
        PdBase.sendFloat(channel + "p", pan.value)
    }

    override fun changeTempo(tempo: Tempo) {
        PdBase.sendFloat("tempo", tempo.value.toFloat())
    }

    override fun changeSwing(swing: Swing) {
        PdBase.sendFloat("swing", swing.value / 100f)
    }

    override fun changeSteps(steps: Steps) {
        PdBase.sendFloat("steps", steps.value.coerceIn(8, 16).toFloat())
    }

    override fun onDestroy(owner: LifecycleOwner) {
        PdBaseHelper.closePatch()
    }
}
