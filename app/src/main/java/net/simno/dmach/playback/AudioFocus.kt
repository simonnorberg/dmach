package net.simno.dmach.playback

import android.annotation.TargetApi
import android.content.SharedPreferences
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.os.Build
import androidx.core.content.edit
import com.jakewharton.rxrelay2.BehaviorRelay
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable

interface AudioFocus {
    fun audioFocus(): Flowable<Int>
    fun toggleFocus()
    fun setIgnoreAudioFocus(ignoreAudioFocus: Boolean)
    fun isIgnoreAudioFocus(): Boolean

    companion object {
        fun create(
            audioManager: AudioManager,
            preferences: SharedPreferences
        ): AudioFocus = AudioFocusImpl(audioManager, preferences)
    }
}

private class AudioFocusImpl(
    private val audioManager: AudioManager,
    private val preferences: SharedPreferences
) : AudioFocus, AudioManager.OnAudioFocusChangeListener {
    private val audioFocus = BehaviorRelay.create<Int>()
    private val focusLock = Any()
    private var focusDelegate = getAudioFocusDelegate()
    private var playbackDelayed = false
    private var resumeOnFocusGain = false

    override fun onAudioFocusChange(focusChange: Int) {
        when (focusChange) {
            AudioManager.AUDIOFOCUS_GAIN -> {
                if (playbackDelayed || resumeOnFocusGain) {
                    synchronized(focusLock) {
                        playbackDelayed = false
                        resumeOnFocusGain = false
                    }
                    onFocusGain()
                }
            }
            AudioManager.AUDIOFOCUS_LOSS -> {
                synchronized(focusLock) {
                    playbackDelayed = false
                    resumeOnFocusGain = false
                }
                onFocusLoss()
            }
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT,
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> {
                synchronized(focusLock) {
                    playbackDelayed = false
                    resumeOnFocusGain = true
                }
                onFocusLoss()
            }
        }
    }

    override fun audioFocus(): Flowable<Int> = audioFocus
        .toFlowable(BackpressureStrategy.LATEST)
        .distinctUntilChanged()

    override fun toggleFocus() {
        if (audioFocus.value == AudioManager.AUDIOFOCUS_GAIN) {
            abandonAudioFocus()
        } else {
            requestAudioFocus()
        }
    }

    override fun setIgnoreAudioFocus(ignoreAudioFocus: Boolean) {
        preferences.edit { putBoolean(IGNORE_AUDIO_FOCUS, ignoreAudioFocus) }
        focusDelegate = getAudioFocusDelegate()
    }

    override fun isIgnoreAudioFocus(): Boolean = focusDelegate == IgnoreFocusDelegate

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
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.O -> FocusDelegate()
        else -> LegacyFocusDelegate()
    }

    private fun onFocusGain() {
        audioFocus.accept(AudioManager.AUDIOFOCUS_GAIN)
    }

    private fun onFocusLoss() {
        audioFocus.accept(AudioManager.AUDIOFOCUS_LOSS)
    }

    companion object {
        private const val IGNORE_AUDIO_FOCUS = "ignoreAudioFocus"
    }

    private interface AudioFocusDelegate {
        fun requestFocus(): Int
        fun abandonFocus(): Int
        fun isGranted(result: Int): Boolean
    }

    @TargetApi(Build.VERSION_CODES.O)
    private inner class FocusDelegate : AudioFocusDelegate {
        private val audioFocusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .setUsage(AudioAttributes.USAGE_GAME)
                    .build()
            )
            .setWillPauseWhenDucked(true)
            .setAcceptsDelayedFocusGain(true)
            .setOnAudioFocusChangeListener(this@AudioFocusImpl)
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

    @Suppress("DEPRECATION")
    private inner class LegacyFocusDelegate : AudioFocusDelegate {
        override fun requestFocus() =
            audioManager.requestAudioFocus(this@AudioFocusImpl, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN)

        override fun abandonFocus() = audioManager.abandonAudioFocus(this@AudioFocusImpl)

        override fun isGranted(result: Int) = result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
    }

    private object IgnoreFocusDelegate : AudioFocusDelegate {
        override fun requestFocus() = AudioManager.AUDIOFOCUS_REQUEST_GRANTED

        override fun abandonFocus() = AudioManager.AUDIOFOCUS_REQUEST_GRANTED

        override fun isGranted(result: Int) = true
    }
}
