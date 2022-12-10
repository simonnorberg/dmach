package net.simno.dmach.playback

import androidx.lifecycle.LifecycleObserver
import net.simno.dmach.data.Tempo

interface PlaybackObserver : LifecycleObserver {
    fun onPlaybackStart()
    fun onPlaybackStop()
    fun updateInfo(title: String, tempo: Tempo)
}
