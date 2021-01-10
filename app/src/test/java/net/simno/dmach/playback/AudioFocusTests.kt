package net.simno.dmach.playback

import android.app.Application
import android.content.Context
import android.media.AudioManager
import android.os.Build
import androidx.core.content.getSystemService
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Shadows
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [Build.VERSION_CODES.O])
class AudioFocusTests {
    private val audioManager = ApplicationProvider.getApplicationContext<Application>()
        .getSystemService<AudioManager>()!!

    private val prefs = ApplicationProvider.getApplicationContext<Application>()
        .getSharedPreferences("dmach.test", Context.MODE_PRIVATE)

    @Test
    fun requestAudioFocusGranted() = runBlocking {
        val shadowAudioManager = Shadows.shadowOf(audioManager)
        shadowAudioManager.setNextFocusRequestResponse(AudioManager.AUDIOFOCUS_REQUEST_GRANTED)

        val audioFocus = AudioFocus(audioManager, prefs)

        audioFocus.toggleFocus()
        val expected = listOf(AudioManager.AUDIOFOCUS_GAIN)
        val actual = audioFocus.audioFocus().take(expected.size).toList()
        assertThat(actual).isEqualTo(expected)
    }

    @Test
    fun requestAudioFocusDelayed() = runBlocking {
        val shadowAudioManager = Shadows.shadowOf(audioManager)
        shadowAudioManager.setNextFocusRequestResponse(AudioManager.AUDIOFOCUS_REQUEST_DELAYED)

        val audioFocus = AudioFocus(audioManager, prefs)

        audioFocus.toggleFocus()
        val expected = listOf(AudioManager.AUDIOFOCUS_LOSS)
        val actual = audioFocus.audioFocus().take(expected.size).toList()
        assertThat(actual).isEqualTo(expected)
    }

    @Test
    fun requestAudioFocusFailed() = runBlocking {
        val shadowAudioManager = Shadows.shadowOf(audioManager)
        shadowAudioManager.setNextFocusRequestResponse(AudioManager.AUDIOFOCUS_REQUEST_FAILED)

        val audioFocus = AudioFocus(audioManager, prefs)

        audioFocus.toggleFocus()
        val expected = listOf(AudioManager.AUDIOFOCUS_LOSS)
        val actual = audioFocus.audioFocus().take(expected.size).toList()
        assertThat(actual).isEqualTo(expected)
    }

    @Suppress("UNCHECKED_CAST")
    @Test
    fun abandonAudioFocus() = runBlocking {
        val shadowAudioManager = Shadows.shadowOf(audioManager)
        shadowAudioManager.setNextFocusRequestResponse(AudioManager.AUDIOFOCUS_REQUEST_GRANTED)

        val audioFocus = AudioFocus(audioManager, prefs)

        val expected = listOf(
            AudioManager.AUDIOFOCUS_NONE,
            AudioManager.AUDIOFOCUS_GAIN,
            AudioManager.AUDIOFOCUS_LOSS
        )
        val actual = listOf(
            async {
                audioFocus.audioFocus().take(expected.size).toList()
            },
            async {
                audioFocus.toggleFocus()
                delay(10L)
                audioFocus.toggleFocus()
            }
        ).awaitAll().first() as List<Int>

        assertThat(actual).isEqualTo(expected)
    }

    @Test
    fun ignoreAudioFocus() = runBlocking {
        val shadowAudioManager = Shadows.shadowOf(audioManager)
        shadowAudioManager.setNextFocusRequestResponse(AudioManager.AUDIOFOCUS_REQUEST_FAILED)

        val audioFocus = AudioFocus(audioManager, prefs)

        audioFocus.setIgnoreAudioFocus(true)
        audioFocus.toggleFocus()
        val expected = listOf(AudioManager.AUDIOFOCUS_GAIN)
        val actual = audioFocus.audioFocus().take(expected.size).toList()
        assertThat(actual).isEqualTo(expected)

        assertThat(shadowAudioManager.lastAudioFocusRequest).isNull()
        assertThat(audioFocus.isIgnoreAudioFocus()).isTrue()
    }
}
