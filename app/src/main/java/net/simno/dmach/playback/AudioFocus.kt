package net.simno.dmach.playback

import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import net.simno.dmach.settings.SettingsRepository

class AudioFocus(
    private val audioManager: AudioManager,
    settingsRepository: SettingsRepository
) : AudioManager.OnAudioFocusChangeListener {

    private enum class Focus {
        USER_GAIN,
        USER_LOSS,
        SYSTEM_GAIN,
        SYSTEM_LOSS,
        NONE
    }

    private val audioFocusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .setUsage(AudioAttributes.USAGE_GAME)
                .build()
        )
        .setWillPauseWhenDucked(true)
        .setAcceptsDelayedFocusGain(true)
        .setOnAudioFocusChangeListener(this)
        .build()

    private var playbackDelayed = false
    private var resumeOnFocusGain = false

    private val focusLock = Any()
    private val focus = MutableStateFlow(Focus.NONE)

    val audioFocus: Flow<Boolean> = combine(
        focus,
        settingsRepository.settings
    ) { focus, settings ->
        if (settings.ignoreAudioFocus) {
            when (focus) {
                Focus.USER_GAIN, Focus.SYSTEM_GAIN -> true
                else -> false
            }
        } else {
            when (focus) {
                Focus.USER_GAIN -> {
                    val result = audioManager.requestAudioFocus(audioFocusRequest)
                    synchronized(focusLock) {
                        when (result) {
                            AudioManager.AUDIOFOCUS_REQUEST_GRANTED -> true
                            AudioManager.AUDIOFOCUS_REQUEST_DELAYED -> {
                                playbackDelayed = true
                                false
                            }
                            else -> false
                        }
                    }
                }
                Focus.USER_LOSS -> {
                    audioManager.abandonAudioFocusRequest(audioFocusRequest)
                    false
                }
                Focus.SYSTEM_GAIN -> true
                Focus.SYSTEM_LOSS -> false
                Focus.NONE -> false
            }
        }
    }.distinctUntilChanged()

    override fun onAudioFocusChange(focusChange: Int) {
        when (focusChange) {
            AudioManager.AUDIOFOCUS_GAIN -> {
                if (playbackDelayed || resumeOnFocusGain) {
                    synchronized(focusLock) {
                        playbackDelayed = false
                        resumeOnFocusGain = false
                    }
                    setFocus(Focus.SYSTEM_GAIN)
                }
            }
            AudioManager.AUDIOFOCUS_LOSS -> {
                synchronized(focusLock) {
                    playbackDelayed = false
                    resumeOnFocusGain = false
                }
                setFocus(Focus.SYSTEM_LOSS)
            }
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT,
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> {
                synchronized(focusLock) {
                    playbackDelayed = false
                    resumeOnFocusGain = true
                }
                setFocus(Focus.SYSTEM_LOSS)
            }
        }
    }

    fun requestAudioFocus() {
        setFocus(Focus.USER_GAIN)
    }

    fun abandonAudioFocus() {
        setFocus(Focus.USER_LOSS)
    }

    private fun setFocus(request: Focus) {
        focus.tryEmit(request)
    }
}
