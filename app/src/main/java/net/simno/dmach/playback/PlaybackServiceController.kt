package net.simno.dmach.playback

import android.content.Context
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.OnLifecycleEvent
import java.util.concurrent.atomic.AtomicBoolean

class PlaybackServiceController(
    private val context: Context
) : PlaybackObserver {

    private val isPlaying = AtomicBoolean(false)
    private var title: String? = null
    private var tempo: String? = null

    override fun onPlaybackStart() {
        if (isPlaying.compareAndSet(false, true)) {
            startService()
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

    @Suppress("unused")
    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    private fun onPause() {
        if (isPlaying.compareAndSet(false, false)) {
            stopService()
        }
    }

    @Suppress("unused")
    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    private fun onDestroy() {
        isPlaying.set(false)
        stopService()
    }

    private fun startService() {
        ContextCompat.startForegroundService(context, PlaybackService.intent(context, title, tempo))
    }

    private fun stopService() {
        context.stopService(PlaybackService.intent(context))
    }
}
