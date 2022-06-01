package net.simno.dmach.playback

import android.content.SharedPreferences
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.AudioManager.AUDIOFOCUS_GAIN
import android.media.AudioManager.AUDIOFOCUS_LOSS
import android.media.AudioManager.AUDIOFOCUS_LOSS_TRANSIENT
import android.media.AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK
import android.media.AudioManager.AUDIOFOCUS_NONE
import androidx.core.content.edit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class AudioFocus(
    private val audioManager: AudioManager,
    private val preferences: SharedPreferences
) : AudioManager.OnAudioFocusChangeListener {

    private val audioFocus = MutableStateFlow(AUDIOFOCUS_NONE)
    private val focusLock = Any()
    private var focusDelegate = getAudioFocusDelegate()
    private var playbackDelayed = false
    private var resumeOnFocusGain = false

    override fun onAudioFocusChange(focusChange: Int) {
        when (focusChange) {
            AUDIOFOCUS_GAIN -> {
                if (playbackDelayed || resumeOnFocusGain) {
                    synchronized(focusLock) {
                        playbackDelayed = false
                        resumeOnFocusGain = false
                    }
                    onFocusGain()
                }
            }
            AUDIOFOCUS_LOSS -> {
                synchronized(focusLock) {
                    playbackDelayed = false
                    resumeOnFocusGain = false
                }
                onFocusLoss()
            }
            AUDIOFOCUS_LOSS_TRANSIENT,
            AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> {
                synchronized(focusLock) {
                    playbackDelayed = false
                    resumeOnFocusGain = true
                }
                onFocusLoss()
            }
        }
    }

    fun audioFocus(): Flow<Int> = audioFocus.asStateFlow()

    fun toggleFocus() {
        if (audioFocus.value == AUDIOFOCUS_GAIN) {
            abandonAudioFocus()
        } else {
            requestAudioFocus()
        }
    }

    fun setIgnoreAudioFocus(ignoreAudioFocus: Boolean) {
        preferences.edit { putBoolean(IGNORE_AUDIO_FOCUS, ignoreAudioFocus) }
        focusDelegate = getAudioFocusDelegate()
    }

    fun isIgnoreAudioFocus(): Boolean = focusDelegate == IgnoreFocusDelegate

    private fun requestAudioFocus() {
        val result = focusDelegate.requestFocus()
        val isGranted: Boolean
        synchronized(focusLock) {
            isGranted = focusDelegate.isGranted(result)
        }
        when {
            isGranted -> onFocusGain()
            else -> onFocusLoss()
        }
    }

    private fun abandonAudioFocus() {
        focusDelegate.abandonFocus()
        onFocusLoss()
    }

    private fun getAudioFocusDelegate(): AudioFocusDelegate = when {
        preferences.getBoolean(IGNORE_AUDIO_FOCUS, false) -> IgnoreFocusDelegate
        else -> FocusDelegate()
    }

    private fun onFocusGain() {
        audioFocus.tryEmit(AUDIOFOCUS_GAIN)
    }

    private fun onFocusLoss() {
        audioFocus.tryEmit(AUDIOFOCUS_LOSS)
    }

    companion object {
        private const val IGNORE_AUDIO_FOCUS = "ignoreAudioFocus"
    }

    private interface AudioFocusDelegate {
        fun requestFocus(): Int
        fun abandonFocus(): Int
        fun isGranted(result: Int): Boolean
    }

    private inner class FocusDelegate : AudioFocusDelegate {
        private val audioFocusRequest = AudioFocusRequest.Builder(AUDIOFOCUS_GAIN)
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .setUsage(AudioAttributes.USAGE_GAME)
                    .build()
            )
            .setWillPauseWhenDucked(true)
            .setAcceptsDelayedFocusGain(true)
            .setOnAudioFocusChangeListener(this@AudioFocus)
            .build()

        override fun requestFocus() = audioManager.requestAudioFocus(audioFocusRequest)

        override fun abandonFocus() = audioManager.abandonAudioFocusRequest(audioFocusRequest)

        override fun isGranted(result: Int): Boolean {
            return when (result) {
                AudioManager.AUDIOFOCUS_REQUEST_GRANTED -> true
                AudioManager.AUDIOFOCUS_REQUEST_DELAYED -> {
                    playbackDelayed = true
                    false
                }
                else -> false
            }
        }
    }

    private object IgnoreFocusDelegate : AudioFocusDelegate {
        override fun requestFocus() = AudioManager.AUDIOFOCUS_REQUEST_GRANTED

        override fun abandonFocus() = AudioManager.AUDIOFOCUS_REQUEST_GRANTED

        override fun isGranted(result: Int) = true
    }
}
