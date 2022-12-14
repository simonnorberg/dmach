package net.simno.dmach.playback

import android.app.Application
import android.media.AudioManager
import android.os.Build
import androidx.core.content.getSystemService
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import net.simno.dmach.settings.Settings
import net.simno.dmach.settings.SettingsRepository
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.whenever
import org.robolectric.Shadows
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [Build.VERSION_CODES.P])
class AudioFocusTests {

    private val audioManager = ApplicationProvider.getApplicationContext<Application>()
        .getSystemService<AudioManager>()!!

    @Mock
    private lateinit var settingsRepository: SettingsRepository

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
    }

    @Test
    fun requestAudioFocusGranted() = runBlocking {
        val shadowAudioManager = Shadows.shadowOf(audioManager)
        shadowAudioManager.setNextFocusRequestResponse(AudioManager.AUDIOFOCUS_REQUEST_GRANTED)

        whenever(settingsRepository.settings)
            .doReturn(flowOf(Settings(ignoreAudioFocus = false)))

        val audioFocus = AudioFocus(audioManager, settingsRepository)

        audioFocus.requestAudioFocus()
        val expected = listOf(true)
        val actual = audioFocus.audioFocus.take(expected.size).toList()
        assertThat(actual).isEqualTo(expected)
    }

    @Test
    fun requestAudioFocusDelayed() = runBlocking {
        val shadowAudioManager = Shadows.shadowOf(audioManager)
        shadowAudioManager.setNextFocusRequestResponse(AudioManager.AUDIOFOCUS_REQUEST_DELAYED)

        whenever(settingsRepository.settings)
            .doReturn(flowOf(Settings(ignoreAudioFocus = false)))

        val audioFocus = AudioFocus(audioManager, settingsRepository)

        audioFocus.requestAudioFocus()
        val expected = listOf(false)
        val actual = audioFocus.audioFocus.take(expected.size).toList()
        assertThat(actual).isEqualTo(expected)
    }

    @Test
    fun requestAudioFocusFailed() = runBlocking {
        val shadowAudioManager = Shadows.shadowOf(audioManager)
        shadowAudioManager.setNextFocusRequestResponse(AudioManager.AUDIOFOCUS_REQUEST_FAILED)

        whenever(settingsRepository.settings)
            .doReturn(flowOf(Settings(ignoreAudioFocus = false)))

        val audioFocus = AudioFocus(audioManager, settingsRepository)

        audioFocus.requestAudioFocus()
        val expected = listOf(false)
        val actual = audioFocus.audioFocus.take(expected.size).toList()
        assertThat(actual).isEqualTo(expected)
    }

    @Suppress("UNCHECKED_CAST")
    @Test
    fun abandonAudioFocus() = runBlocking {
        val shadowAudioManager = Shadows.shadowOf(audioManager)
        shadowAudioManager.setNextFocusRequestResponse(AudioManager.AUDIOFOCUS_REQUEST_GRANTED)

        whenever(settingsRepository.settings)
            .doReturn(flowOf(Settings(ignoreAudioFocus = false)))

        val audioFocus = AudioFocus(audioManager, settingsRepository)

        val expected = listOf(true, false)
        val actual = listOf(
            async {
                audioFocus.audioFocus.take(expected.size).toList()
            },
            async {
                audioFocus.requestAudioFocus()
                delay(10L)
                audioFocus.abandonAudioFocus()
            }
        ).awaitAll().first() as List<Boolean>

        assertThat(actual).isEqualTo(expected)
    }

    @Test
    fun ignoreAudioFocus() = runBlocking {
        val shadowAudioManager = Shadows.shadowOf(audioManager)
        shadowAudioManager.setNextFocusRequestResponse(AudioManager.AUDIOFOCUS_REQUEST_FAILED)

        whenever(settingsRepository.settings)
            .doReturn(flowOf(Settings(ignoreAudioFocus = true)))

        val audioFocus = AudioFocus(audioManager, settingsRepository)

        audioFocus.requestAudioFocus()
        val expected = listOf(true)
        val actual = audioFocus.audioFocus.take(expected.size).toList()
        assertThat(actual).isEqualTo(expected)

        assertThat(shadowAudioManager.lastAudioFocusRequest).isNull()
    }
}
