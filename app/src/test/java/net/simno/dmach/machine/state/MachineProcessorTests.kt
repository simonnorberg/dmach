package net.simno.dmach.machine.state

import com.google.common.truth.Truth.assertThat
import java.io.File
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.Channel.Factory.RENDEZVOUS
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
import net.simno.dmach.data.Pan
import net.simno.dmach.data.Patch
import net.simno.dmach.data.Position
import net.simno.dmach.data.Steps
import net.simno.dmach.data.Swing
import net.simno.dmach.data.Tempo
import net.simno.dmach.data.mutedSequence
import net.simno.dmach.data.withPan
import net.simno.dmach.data.withPosition
import net.simno.dmach.data.withSelectedSetting
import net.simno.dmach.db.PatchRepository
import net.simno.dmach.db.TestPatchDao
import net.simno.dmach.playback.AudioFocus
import net.simno.dmach.playback.PlaybackController
import net.simno.dmach.playback.PureData
import net.simno.dmach.playback.WaveExporter
import net.simno.dmach.settings.Settings
import net.simno.dmach.settings.SettingsRepository
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.anyList
import org.mockito.Mockito.mock
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.whenever

@DelicateCoroutinesApi
class MachineProcessorTests {

    @Mock
    private lateinit var pureData: PureData

    @Mock
    private lateinit var playbackController: PlaybackController

    @Mock
    private lateinit var audioFocus: AudioFocus

    @Mock
    private lateinit var waveExporter: WaveExporter

    @Mock
    private lateinit var settingsRepository: SettingsRepository

    private lateinit var repository: PatchRepository
    private lateinit var testDao: TestPatchDao
    private lateinit var machineProcessor: MachineProcessor

    private suspend fun processAction(action: Action): Result = processActions(action).first()

    private val testUid = 1337

    private suspend fun processActions(
        vararg actions: Action,
        resultSize: Int? = null
    ): List<Result> = actions.asFlow()
        .onEach { delay(10L) }
        .buffer(RENDEZVOUS)
        .shareIn(GlobalScope, SharingStarted.Lazily)
        .let(machineProcessor)
        .take(resultSize ?: actions.size)
        .toList()

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        testDao = TestPatchDao()
        repository = PatchRepository(testDao)
        machineProcessor = MachineProcessor(
            playbackController = playbackController,
            pureData = pureData,
            waveExporter = waveExporter,
            audioFocus = audioFocus,
            patchRepository = repository,
            settingsRepository = settingsRepository,
            randomSequence = { listOf(1337) },
            randomInt = { testUid },
            randomFloat = { 0.1337f }
        )
    }

    @Test
    fun load() = runBlocking {
        val actual = processAction(LoadAction)
        val expected = LoadResult(
            title = testDao.patch.title,
            sequenceId = testUid,
            sequence = testDao.patch.sequence,
            mutedChannels = testDao.patch.mutedChannels,
            selectedChannel = testDao.patch.selectedChannel,
            selectedSetting = 0,
            settingId = testUid,
            settingsSize = 5,
            hText = "Pitch",
            vText = "Gain",
            position = Position(0.49f, .45f),
            panId = testUid,
            pan = Pan(0.5f),
            tempo = testDao.patch.tempo,
            swing = testDao.patch.swing,
            steps = testDao.patch.steps
        )
        assertThat(actual).isEqualTo(expected)

        verify(pureData, times(1)).changeSequence(testDao.patch.sequence)
        verify(pureData, times(1)).changeTempo(testDao.patch.tempo)
        verify(pureData, times(1)).changeSwing(testDao.patch.swing)
        verify(pureData, times(1)).changeSteps(testDao.patch.steps)
        testDao.patch.channels.forEach { channel ->
            verify(pureData, times(1)).changePan(channel.name, channel.pan)
            channel.settings.forEach { setting ->
                verify(pureData, times(1)).changeSetting(channel.name, setting)
            }
        }
        verify(playbackController, times(1)).updateInfo(testDao.patch.title, testDao.patch.tempo)
    }

    @Test
    fun playback() = runBlocking {
        whenever(audioFocus.audioFocus)
            .doReturn(flowOf(false, true))

        val expected = listOf(PlaybackResult(false), PlaybackResult(true))
        val actual = processActions(PlaybackAction, resultSize = expected.size)
        assertThat(actual).isEqualTo(expected)

        verify(playbackController, times(1)).stopPlayback()
        verify(playbackController, times(1)).startPlayback()
    }

    @Test
    fun playPause() = runBlocking {
        val actual = processActions(PlayPauseAction(true), PlayPauseAction(false), PlayPauseAction(true))
        val expected = listOf(PlayPauseResult, PlayPauseResult, PlayPauseResult)
        assertThat(actual).isEqualTo(expected)

        verify(audioFocus, times(2)).requestAudioFocus()
        verify(audioFocus, times(1)).abandonAudioFocus()
    }

    @Test
    fun settings() = runBlocking {
        val settings = listOf(
            Settings(ignoreAudioFocus = true),
            Settings(ignoreAudioFocus = false)
        )
        whenever(settingsRepository.settings)
            .doReturn(settings.asFlow())

        val actual = processActions(ChangeSettingsAction(settings[0]), ChangeSettingsAction(settings[1]))
        val expected = listOf(ChangeSettingsResult, ChangeSettingsResult)
        assertThat(actual).isEqualTo(expected)

        verify(settingsRepository, times(1)).updateSettings(settings[0])
        verify(settingsRepository, times(1)).updateSettings(settings[1])
    }

    @Test
    fun configDismiss() = runBlocking {
        processAction(LoadAction)
        verify(playbackController, times(1)).updateInfo(testDao.patch.title, testDao.patch.tempo)

        val actual = processActions(ConfigAction, DismissAction, ConfigAction)
        val expected = listOf(ConfigResult(testUid), DismissResult, ConfigResult(testUid))
        assertThat(actual).isEqualTo(expected)
        verify(playbackController, times(2)).updateInfo(testDao.patch.title, testDao.patch.tempo)
    }

    @Test
    fun startExport() = runBlocking {
        processAction(LoadAction)

        val actual = processAction(ExportAction)
        val expected = ExportResult

        verify(audioFocus, times(1)).abandonAudioFocus()
        assertThat(actual).isEqualTo(expected)
    }

    @Test
    fun exportFile() = runBlocking {
        val mockFile = mock(File::class.java)
        val title = "untitled"
        val tempo = Tempo(120)
        val steps = Steps(16)

        whenever(waveExporter.saveWaveFile(title, tempo, steps))
            .doReturn(mockFile)

        processAction(LoadAction)

        val actual = processAction(ExportFileAction(title, tempo, steps))
        val expected = ExportFileResult(mockFile)

        verify(waveExporter, times(1)).saveWaveFile(title, tempo, steps)
        assertThat(actual).isEqualTo(expected)
    }

    @Test
    fun changeSequence() = runBlocking {
        processAction(LoadAction)
        val sequence = listOf(1337)

        val actual = processAction(ChangeSequenceAction(0, sequence))
        val expected = ChangeSequenceResult(0, sequence)
        assertThat(actual).isEqualTo(expected)

        verify(pureData, times(1)).changeSequence(sequence)
        assertThat(repository.unsavedPatch()).isEqualTo(testDao.patch.copy(sequence = sequence))
    }

    @Test
    fun muteChannel() = runBlocking {
        processAction(LoadAction)

        val actual = processActions(
            MuteChannelAction(0, true),
            MuteChannelAction(4, true),
            MuteChannelAction(3, true),
            MuteChannelAction(1, true),
            MuteChannelAction(2, true),
            MuteChannelAction(3, false),
            MuteChannelAction(4, false)
        )
        val expected = listOf(
            MuteChannelResult(setOf(0)),
            MuteChannelResult(setOf(0, 4)),
            MuteChannelResult(setOf(0, 3, 4)),
            MuteChannelResult(setOf(0, 1, 3, 4)),
            MuteChannelResult(setOf(0, 1, 2, 3, 4)),
            MuteChannelResult(setOf(0, 1, 2, 4)),
            MuteChannelResult(setOf(0, 1, 2))
        )
        assertThat(actual).isEqualTo(expected)

        verify(pureData, times(8)).changeSequence(anyList())
        assertThat(repository.unsavedPatch())
            .isEqualTo(testDao.patch.copy(mutedChannels = expected.last().mutedChannels))

        val mutedSequence = testDao.patch.sequence.take(Patch.STEPS).map { 0 } +
            testDao.patch.sequence.takeLast(Patch.STEPS)
        assertThat(repository.unsavedPatch().mutedSequence()).isEqualTo(mutedSequence)
    }

    @Test
    fun selectChannel() = runBlocking {
        processAction(LoadAction)
        val selectedChannel = 2

        val actual = processAction(SelectChannelAction(selectedChannel, false))
        val expected = SelectChannelResult(
            selectedChannel = selectedChannel,
            selectedSetting = 0,
            settingId = testUid,
            settingsSize = 4,
            hText = "Pitch",
            vText = "Gain",
            panId = testUid,
            pan = Pan(0.5f),
            position = Position(.55f, .3f)
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
            settingId = testUid,
            hText = "Low-pass",
            vText = "Noise",
            position = Position(.6f, .8f)
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
        val pan = Pan(.1337f)

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
        val tempo = Tempo(1337)

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
        val swing = Swing(1337)

        val actual = processAction(ChangeSwingAction(swing))
        val expected = ChangeSwingResult(swing)
        assertThat(actual).isEqualTo(expected)

        val expectedPatch = testDao.patch.copy(swing = swing)
        verify(pureData, times(1)).changeSwing(swing)
        assertThat(repository.unsavedPatch()).isEqualTo(expectedPatch)
    }

    @Test
    fun changeSteps() = runBlocking {
        processAction(LoadAction)
        val steps = Steps(13)

        val actual = processAction(ChangeStepsAction(steps))
        val expected = ChangeStepsResult(steps, testUid)
        assertThat(actual).isEqualTo(expected)

        val expectedPatch = testDao.patch.copy(steps = steps)
        verify(pureData, times(1)).changeSteps(steps)
        assertThat(repository.unsavedPatch()).isEqualTo(expectedPatch)
    }

    @Test
    fun resetSequence() = runBlocking {
        processAction(LoadAction)

        val sequence = listOf(1337)
        processAction(ChangeSequenceAction(0, sequence))
        verify(pureData, times(1)).changeSequence(sequence)
        assertThat(repository.unsavedPatch()).isEqualTo(testDao.patch.copy(sequence = sequence))

        val actual = processAction(ChangePatchAction.Reset(Settings(sequenceEnabled = true)))
        val expected = ChangePatchResult(
            sequenceId = testUid,
            sequence = Patch.EMPTY_SEQUENCE,
            panId = testUid,
            pan = testDao.patch.channel.pan,
            settingId = testUid,
            position = testDao.patch.channel.setting.position
        )
        assertThat(actual).isEqualTo(expected)
        assertThat(repository.unsavedPatch()).isEqualTo(testDao.patch.copy(sequence = expected.sequence))
        verify(pureData, times(1)).changeSequence(expected.sequence)
    }

    @Test
    fun resetSound() = runBlocking {
        processAction(LoadAction)

        val position = Position(.13f, .37f)
        processAction(ChangePositionAction(position))
        assertThat(repository.unsavedPatch()).isEqualTo(testDao.patch.withPosition(position))

        val actual = processAction(ChangePatchAction.Reset(Settings(soundEnabled = true)))
        val expected = ChangePatchResult(
            sequenceId = testUid,
            sequence = testDao.patch.sequence,
            panId = testUid,
            pan = testDao.patch.channel.pan,
            settingId = testUid,
            position = testDao.patch.channel.setting.position
        )
        assertThat(actual).isEqualTo(expected)
        assertThat(repository.unsavedPatch()).isEqualTo(testDao.patch)
    }

    @Test
    fun resetPan() = runBlocking {
        processAction(LoadAction)

        val pan = Pan(.1337f)
        processAction(ChangePanAction(pan))
        assertThat(repository.unsavedPatch()).isEqualTo(testDao.patch.withPan(pan))

        val actual = processAction(ChangePatchAction.Reset(Settings(panEnabled = true)))
        val expected = ChangePatchResult(
            sequenceId = testUid,
            sequence = testDao.patch.sequence,
            panId = testUid,
            pan = testDao.patch.channel.pan,
            settingId = testUid,
            position = testDao.patch.channel.setting.position
        )
        assertThat(actual).isEqualTo(expected)
        assertThat(repository.unsavedPatch()).isEqualTo(testDao.patch)
    }

    @Test
    fun randomizeSequence() = runBlocking {
        processAction(LoadAction)

        assertThat(repository.unsavedPatch()).isEqualTo(testDao.patch)

        val actual = processAction(ChangePatchAction.Randomize(Settings(sequenceEnabled = true)))
        val expected = ChangePatchResult(
            sequenceId = testUid,
            sequence = listOf(1337),
            panId = testUid,
            pan = testDao.patch.channel.pan,
            settingId = testUid,
            position = testDao.patch.channel.setting.position
        )
        assertThat(actual).isEqualTo(expected)
        assertThat(repository.unsavedPatch()).isEqualTo(testDao.patch.copy(sequence = expected.sequence))
        verify(pureData, times(1)).changeSequence(expected.sequence)
    }

    @Test
    fun randomizeSound() = runBlocking {
        processAction(LoadAction)

        assertThat(repository.unsavedPatch()).isEqualTo(testDao.patch)

        val actual = processAction(ChangePatchAction.Randomize(Settings(soundEnabled = true)))
        val expected = ChangePatchResult(
            sequenceId = testUid,
            sequence = testDao.patch.sequence,
            panId = testUid,
            pan = testDao.patch.channel.pan,
            settingId = testUid,
            position = Position(0.1337f, 0.1337f)
        )
        assertThat(actual).isEqualTo(expected)
    }

    @Test
    fun randomizePan() = runBlocking {
        processAction(LoadAction)

        assertThat(repository.unsavedPatch()).isEqualTo(testDao.patch)

        val actual = processAction(ChangePatchAction.Randomize(Settings(panEnabled = true)))
        val expected = ChangePatchResult(
            sequenceId = testUid,
            sequence = testDao.patch.sequence,
            panId = testUid,
            pan = Pan(0.1337f),
            settingId = testUid,
            position = testDao.patch.channel.setting.position
        )
        assertThat(actual).isEqualTo(expected)
    }
}
