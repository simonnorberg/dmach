package net.simno.dmach.playback

import android.app.Application
import android.content.Context
import android.media.AudioManager
import android.os.Build
import androidx.core.content.getSystemService
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Shadows
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [Build.VERSION_CODES.LOLLIPOP, Build.VERSION_CODES.P])
class AudioFocusTests {
    private val audioManager = ApplicationProvider.getApplicationContext<Application>()
        .getSystemService<AudioManager>()!!

    private val prefs = ApplicationProvider.getApplicationContext<Application>()
        .getSharedPreferences("dmach.test", Context.MODE_PRIVATE)

    @Test
    fun requestAudioFocusGranted() {
        val shadowAudioManager = Shadows.shadowOf(audioManager)
        shadowAudioManager.setNextFocusRequestResponse(AudioManager.AUDIOFOCUS_REQUEST_GRANTED)

        val audioFocus = AudioFocus.create(audioManager, prefs)
        val test = audioFocus.audioFocus().test()

        audioFocus.toggleFocus()

        test.awaitCount(2)
        test.assertNoErrors()
        test.assertValue(AudioManager.AUDIOFOCUS_GAIN)
    }

    @Test
    fun requestAudioFocusDelayed() {
        val shadowAudioManager = Shadows.shadowOf(audioManager)
        shadowAudioManager.setNextFocusRequestResponse(AudioManager.AUDIOFOCUS_REQUEST_DELAYED)

        val audioFocus = AudioFocus.create(audioManager, prefs)
        val test = audioFocus.audioFocus().test()

        audioFocus.toggleFocus()

        test.awaitCount(1)
        test.assertNoErrors()
        test.assertValues(AudioManager.AUDIOFOCUS_LOSS)
    }

    @Test
    fun requestAudioFocusFailed() {
        val shadowAudioManager = Shadows.shadowOf(audioManager)
        shadowAudioManager.setNextFocusRequestResponse(AudioManager.AUDIOFOCUS_REQUEST_FAILED)

        val audioFocus = AudioFocus.create(audioManager, prefs)
        val test = audioFocus.audioFocus().test()

        audioFocus.toggleFocus()

        test.awaitCount(1)
        test.assertNoErrors()
        test.assertValues(AudioManager.AUDIOFOCUS_LOSS)
    }

    @Test
    fun abandonAudioFocus() {
        val shadowAudioManager = Shadows.shadowOf(audioManager)
        shadowAudioManager.setNextFocusRequestResponse(AudioManager.AUDIOFOCUS_REQUEST_GRANTED)

        val audioFocus = AudioFocus.create(audioManager, prefs)
        val test = audioFocus.audioFocus().test()

        audioFocus.toggleFocus()
        audioFocus.toggleFocus()

        test.awaitCount(3)
        test.assertNoErrors()
        test.assertValues(
            AudioManager.AUDIOFOCUS_GAIN,
            AudioManager.AUDIOFOCUS_LOSS
        )
    }

    @Test
    fun ignoreAudioFocus() {
        val shadowAudioManager = Shadows.shadowOf(audioManager)
        shadowAudioManager.setNextFocusRequestResponse(AudioManager.AUDIOFOCUS_REQUEST_FAILED)

        val audioFocus = AudioFocus.create(audioManager, prefs)
        val test = audioFocus.audioFocus().test()

        audioFocus.setIgnoreAudioFocus(true)
        audioFocus.toggleFocus()

        test.awaitCount(2)
        test.assertNoErrors()
        test.assertValue(AudioManager.AUDIOFOCUS_GAIN)

        assertThat(shadowAudioManager.lastAudioFocusRequest).isNull()
        assertThat(audioFocus.isIgnoreAudioFocus()).isTrue()
    }
}
