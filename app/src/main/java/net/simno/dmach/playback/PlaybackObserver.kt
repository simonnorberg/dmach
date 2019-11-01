package net.simno.dmach.playback

interface PlaybackObserver {
    fun onPlaybackStart()
    fun onPlaybackStop()
    fun updateInfo(title: String, tempo: Int)
}
