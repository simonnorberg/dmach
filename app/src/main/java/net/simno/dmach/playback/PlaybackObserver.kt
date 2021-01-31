package net.simno.dmach.playback

import androidx.lifecycle.LifecycleObserver

interface PlaybackObserver : LifecycleObserver {
    fun onPlaybackStart()
    fun onPlaybackStop()
    fun updateInfo(title: String, tempo: Int)
}
