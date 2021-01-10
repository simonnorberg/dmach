package net.simno.dmach.playback

import android.content.Context
import androidx.annotation.Size
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import net.simno.dmach.R
import net.simno.dmach.data.Patch
import net.simno.dmach.data.Setting
import net.simno.kortholt.PdBaseHelper
import org.puredata.core.PdBase

class PureDataController(
    context: Context
) : PureData, PlaybackObserver, LifecycleObserver {

    init {
        PdBaseHelper.openPatch(context, R.raw.dmach, "dmach.pd", extractZip = true)
    }

    override fun onPlaybackStart() {
        PdBase.sendBang("play")
    }

    override fun onPlaybackStop() {
        PdBase.sendBang("stop")
    }

    override fun updateInfo(title: String, tempo: Int) {
    }

    override fun changeSequence(@Size(32) sequence: List<Int>) {
        for (step in 0 until Patch.STEPS) {
            PdBase.sendList("step", 0, step, sequence[step])
            PdBase.sendList("step", 1, step, sequence[step + Patch.STEPS])
        }
    }

    override fun changeSetting(channel: String, setting: Setting) {
        PdBase.sendList(channel, setting.hIndex, setting.x)
        PdBase.sendList(channel, setting.vIndex, setting.y)
    }

    override fun changePan(channel: String, pan: Float) {
        PdBase.sendFloat(channel + "p", pan)
    }

    override fun changeTempo(tempo: Int) {
        PdBase.sendFloat("tempo", tempo.toFloat())
    }

    override fun changeSwing(swing: Int) {
        PdBase.sendFloat("swing", swing / 100f)
    }

    @Suppress("unused")
    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun closePatch() {
        PdBaseHelper.closePatch()
    }
}
