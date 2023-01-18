package net.simno.dmach.playback

import android.content.Context
import androidx.core.content.ContextCompat
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import java.util.concurrent.atomic.AtomicBoolean
import kotlinx.coroutines.runBlocking
import net.simno.dmach.R
import net.simno.dmach.data.Tempo
import net.simno.kortholt.Kortholt

class PlaybackController(
    private val context: Context,
    private val kortholt: Kortholt.Player,
    private val pureData: PureData,
    private val waveExporter: WaveExporter
) : DefaultLifecycleObserver {

    private val isPlaying = AtomicBoolean(false)
    private var title: String? = null
    private var tempo: String? = null

    suspend fun openPatch() {
        kortholt.openPatch(R.raw.dmach, "dmach.pd", extractZip = true)
    }

    suspend fun startPlayback() {
        if (isPlaying.compareAndSet(false, true)) {
            startService()
            kortholt.startStream()
            pureData.startPlayback()
        }
    }

    fun stopPlayback() {
        isPlaying.set(false)
        pureData.stopPlayback()
    }

    fun updateInfo(title: String, tempo: Tempo) {
        this.title = title
        this.tempo = "${tempo.value} BPM"
        if (isPlaying.get()) {
            // Call startService again if we are playing to update the notification.
            startService()
        }
    }

    override fun onPause(owner: LifecycleOwner) {
        if (isPlaying.compareAndSet(false, false)) {
            stopService()
            if (!waveExporter.isExporting()) {
                runBlocking { kortholt.stopStream() }
            }
        }
    }

    override fun onDestroy(owner: LifecycleOwner) {
        isPlaying.set(false)
        stopService()
        pureData.stopPlayback()
        runBlocking {
            kortholt.stopStream()
            kortholt.closePatch()
        }
    }

    private fun startService() {
        ContextCompat.startForegroundService(context, PlaybackService.intent(context, title, tempo))
    }

    private fun stopService() {
        context.stopService(PlaybackService.intent(context))
    }
}
