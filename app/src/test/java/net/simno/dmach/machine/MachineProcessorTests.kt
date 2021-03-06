package net.simno.dmach.machine

import android.media.AudioManager
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import net.simno.dmach.data.Position
import net.simno.dmach.data.withPan
import net.simno.dmach.data.withPosition
import net.simno.dmach.data.withSelectedSetting
import net.simno.dmach.db.PatchRepository
import net.simno.dmach.db.TestPatchDao
import net.simno.dmach.playback.AudioFocus
import net.simno.dmach.playback.PlaybackObserver
import net.simno.dmach.playback.PureData
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.MockitoAnnotations

class MachineProcessorTests {

    @Mock
    private lateinit var pureData: PureData

    @Mock
    private lateinit var playbackObserver: PlaybackObserver

    @Mock
    private lateinit var audioFocus: AudioFocus
    private lateinit var repository: PatchRepository
    private lateinit var testDao: TestPatchDao
    private lateinit var machineProcessor: MachineProcessor

    private suspend fun processAction(action: Action): Result = processActions(action).first()

    private suspend fun processActions(
        vararg actions: Action,
        resultSize: Int? = null
    ): List<Result> = actions.asFlow()
        .onEach { delay(10L) }
        .buffer(0)
        .shareIn(GlobalScope, SharingStarted.Lazily)
        .let(machineProcessor)
        .take(resultSize ?: actions.size)
        .toList()

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        testDao = TestPatchDao()
        repository = PatchRepository(testDao)
        machineProcessor = MachineProcessor(setOf(playbackObserver), pureData, audioFocus, repository)
    }

    @Test
    fun load() = runBlocking {
        val actual = processAction(LoadAction)
        val expected = LoadResult(
            ignoreAudioFocus = false,
            sequence = testDao.patch.sequence,
            tempo = testDao.patch.tempo,
            swing = testDao.patch.swing,
            selectedChannel = testDao.patch.selectedChannel,
            selectedSetting = 0,
            settingsSize = 4,
            hText = "1",
            vText = "2",
            position = Position(0.1f, .2f),
            pan = 0.5f
        )
        assertThat(actual).isEqualTo(expected)

        verify(pureData, times(1)).changeSequence(testDao.patch.sequence)
        verify(pureData, times(1)).changeTempo(testDao.patch.tempo)
        verify(pureData, times(1)).changeSwing(testDao.patch.swing)
        testDao.patch.channels.forEach { channel ->
            verify(pureData, times(1)).changePan(channel.name, channel.pan)
            channel.settings.forEach { setting ->
                verify(pureData, times(1)).changeSetting(channel.name, setting)
            }
        }
        verify(playbackObserver, times(1)).updateInfo(testDao.patch.title, testDao.patch.tempo)
    }

    @Test
    fun playback() = runBlocking {
        `when`(audioFocus.audioFocus())
            .thenReturn(flowOf(AudioManager.AUDIOFOCUS_LOSS, AudioManager.AUDIOFOCUS_GAIN))

        val expected = listOf(PlaybackResult(false), PlaybackResult(true))
        val actual = processActions(PlaybackAction, resultSize = expected.size)
        assertThat(actual).isEqualTo(expected)

        verify(playbackObserver, times(1)).onPlaybackStop()
        verify(playbackObserver, times(1)).onPlaybackStart()
    }

    @Test
    fun playPause() = runBlocking {
        val actual = processActions(PlayPauseAction, PlayPauseAction, PlayPauseAction)
        val expected = listOf(PlayPauseResult, PlayPauseResult, PlayPauseResult)
        assertThat(actual).isEqualTo(expected)

        verify(audioFocus, times(3)).toggleFocus()
    }

    @Test
    fun audioFocus() = runBlocking {
        `when`(audioFocus.isIgnoreAudioFocus())
            .thenReturn(true)
            .thenReturn(false)
        val actual = processActions(AudioFocusAction(true), AudioFocusAction(false))
        val expected = listOf(AudioFocusResult(true), AudioFocusResult(false))
        assertThat(actual).isEqualTo(expected)

        verify(audioFocus, times(1)).setIgnoreAudioFocus(true)
        verify(audioFocus, times(1)).setIgnoreAudioFocus(false)
    }

    @Test
    fun configDismiss() = runBlocking {
        processAction(LoadAction)
        verify(playbackObserver, times(1)).updateInfo(testDao.patch.title, testDao.patch.tempo)

        val actual = processActions(ConfigAction, DismissAction, ConfigAction)
        val expected = listOf(ConfigResult, DismissResult, ConfigResult)
        assertThat(actual).isEqualTo(expected)
        verify(playbackObserver, times(2)).updateInfo(testDao.patch.title, testDao.patch.tempo)
    }

    @Test
    fun changeSequence() = runBlocking {
        processAction(LoadAction)
        val sequence = listOf(1337)

        val actual = processAction(ChangeSeqenceAction(sequence))
        val expected = ChangeSequenceResult(sequence)
        assertThat(actual).isEqualTo(expected)

        verify(pureData, times(1)).changeSequence(sequence)
        assertThat(repository.unsavedPatch()).isEqualTo(testDao.patch.copy(sequence = sequence))
    }

    @Test
    fun selectChannel() = runBlocking {
        processAction(LoadAction)
        val selectedChannel = 2

        val actual = processAction(SelectChannelAction(selectedChannel, false))
        val expected = SelectChannelResult(
            selectedChannel = selectedChannel,
            selectedSetting = 0,
            settingsSize = 4,
            hText = "1",
            vText = "2",
            pan = 0.5f,
            position = Position(.1f, .2f)
        )
        assertThat(actual).isEqualTo(expected)

        val expectedPatch = testDao.patch.copy(selectedChannel = selectedChannel)
        assertThat(repository.unsavedPatch()).isEqualTo(expectedPatch)
    }

    @Test
    fun selectSetting() = runBlocking {
        processAction(LoadAction)
        val selectedSetting = 1

        val actual = processAction(SelectSettingAction(selectedSetting))
        val expected = SelectSettingResult(
            selectedSetting = selectedSetting,
            hText = "3",
            vText = "4",
            pan = 0.5f,
            position = Position(.3f, .4f)
        )
        assertThat(actual).isEqualTo(expected)

        val expectedPatch = testDao.patch.withSelectedSetting(selectedSetting)
        assertThat(repository.unsavedPatch()).isEqualTo(expectedPatch)
    }

    @Test
    fun changePosition() = runBlocking {
        processAction(LoadAction)
        val position = Position(.13f, .37f)

        val actual = processAction(ChangePositionAction(position))
        val expected = ChangePositionResult
        assertThat(actual).isEqualTo(expected)

        val expectedPatch = testDao.patch.withPosition(position)
        verify(pureData, times(1)).changeSetting(expectedPatch.channel.name, expectedPatch.channel.setting)
        assertThat(repository.unsavedPatch()).isEqualTo(expectedPatch)
    }

    @Test
    fun changePan() = runBlocking {
        processAction(LoadAction)
        val pan = .1337f

        val actual = processAction(ChangePanAction(pan))
        val expected = ChangePanResult
        assertThat(actual).isEqualTo(expected)

        val expectedPatch = testDao.patch.withPan(pan)
        verify(pureData, times(1)).changePan(expectedPatch.channel.name, expectedPatch.channel.pan)
        assertThat(repository.unsavedPatch()).isEqualTo(expectedPatch)
    }

    @Test
    fun changeTempo() = runBlocking {
        processAction(LoadAction)
        val tempo = 1337

        val actual = processAction(ChangeTempoAction(tempo))
        val expected = ChangeTempoResult(tempo)
        assertThat(actual).isEqualTo(expected)

        val expectedPatch = testDao.patch.copy(tempo = tempo)
        verify(pureData, times(1)).changeTempo(tempo)
        assertThat(repository.unsavedPatch()).isEqualTo(expectedPatch)
    }

    @Test
    fun changeSwing() = runBlocking {
        processAction(LoadAction)
        val swing = 1337

        val actual = processAction(ChangeSwingAction(swing))
        val expected = ChangeSwingResult(swing)
        assertThat(actual).isEqualTo(expected)

        val expectedPatch = testDao.patch.copy(swing = swing)
        verify(pureData, times(1)).changeSwing(swing)
        assertThat(repository.unsavedPatch()).isEqualTo(expectedPatch)
    }
}
