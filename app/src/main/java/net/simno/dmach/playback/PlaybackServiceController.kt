package net.simno.dmach.playback

import android.content.Context
import androidx.core.content.ContextCompat
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import java.util.concurrent.atomic.AtomicBoolean

class PlaybackServiceController(
    private val context: Context,
    private val kortholtController: KortholtController
) : PlaybackObserver, DefaultLifecycleObserver {

    private val isPlaying = AtomicBoolean(false)
    private var title: String? = null
    private var tempo: String? = null

    override fun onPlaybackStart() {
        if (isPlaying.compareAndSet(false, true)) {
            startService()
            kortholtController.create()
        }
    }

    override fun onPlaybackStop() {
        isPlaying.set(false)
    }

    override fun updateInfo(title: String, tempo: Int) {
        this.title = title
        this.tempo = "$tempo BPM"
        if (isPlaying.get()) {
            // Call startService again if we are playing to update the notification.
            startService()
        }
    }

    override fun onPause(owner: LifecycleOwner) {
        if (isPlaying.compareAndSet(false, false)) {
            stopService()
            if (!kortholtController.isExporting()) {
                kortholtController.destroy()
            }
        }
    }

    override fun onDestroy(owner: LifecycleOwner) {
        isPlaying.set(false)
        stopService()
        kortholtController.destroy()
    }

    private fun startService() {
        ContextCompat.startForegroundService(context, PlaybackService.intent(context, title, tempo))
    }

    private fun stopService() {
        context.stopService(PlaybackService.intent(context))
    }
}
