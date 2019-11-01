package net.simno.dmach.machine

import android.media.AudioManager
import com.google.common.truth.Truth.assertThat
import io.reactivex.Flowable
import net.simno.dmach.data.Position
import net.simno.dmach.data.withPan
import net.simno.dmach.data.withPosition
import net.simno.dmach.data.withSelectedSetting
import net.simno.dmach.db.TestDb
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
import java.util.concurrent.TimeUnit

class MachineProcessorTests {

    @Mock
    private lateinit var pureData: PureData
    @Mock
    private lateinit var playbackObserver: PlaybackObserver
    @Mock
    private lateinit var audioFocus: AudioFocus
    private lateinit var db: TestDb
    private lateinit var machineProcessor: MachineProcessor

    private fun processAction(action: Action): Result = processActions(action).first()

    private fun processActions(vararg actions: Action): List<Result> {
        val test = Flowable.intervalRange(0, actions.size.toLong(), 0, 50, TimeUnit.MILLISECONDS)
            .map { actions[it.toInt()] }
            .compose(machineProcessor)
            .test()
        test.awaitTerminalEvent()
        test.assertNoErrors()
        return test.values()
    }

    @Before
    fun setup() {
        MockitoAnnotations.initMocks(this)
        db = TestDb()
        machineProcessor = MachineProcessor(pureData, audioFocus, setOf(playbackObserver), db)
    }

    @Test
    fun load() {
        val actual = processAction(LoadAction)
        val expected = LoadResult(
            ignoreAudioFocus = false,
            sequence = db.patch.sequence,
            tempo = db.patch.tempo,
            swing = db.patch.swing,
            selectedChannel = db.patch.selectedChannel,
            selectedSetting = 0,
            settingsSize = 4,
            hText = "1",
            vText = "2",
            position = Position(0.1f, .2f),
            pan = 0.5f
        )
        assertThat(actual).isEqualTo(expected)

        verify(pureData, times(1)).changeSequence(db.patch.sequence)
        verify(pureData, times(1)).changeTempo(db.patch.tempo)
        verify(pureData, times(1)).changeSwing(db.patch.swing)
        db.patch.channels.forEach { channel ->
            verify(pureData, times(1)).changePan(channel.name, channel.pan)
            channel.settings.forEach { setting ->
                verify(pureData, times(1)).changeSetting(channel.name, setting)
            }
        }
        verify(playbackObserver, times(1)).updateInfo(db.patch.title, db.patch.tempo)
    }

    @Test
    fun playback() {
        `when`(audioFocus.audioFocus())
            .thenReturn(Flowable.just(AudioManager.AUDIOFOCUS_LOSS, AudioManager.AUDIOFOCUS_GAIN))

        val actual = processActions(PlaybackAction)
        val expected = listOf(PlaybackResult(false), PlaybackResult(true))
        assertThat(actual).isEqualTo(expected)

        verify(playbackObserver, times(1)).onPlaybackStop()
        verify(playbackObserver, times(1)).onPlaybackStart()
    }

    @Test
    fun playPause() {
        val actual = processActions(PlayPauseAction, PlayPauseAction, PlayPauseAction)
        val expected = listOf(PlayPauseResult, PlayPauseResult, PlayPauseResult)
        assertThat(actual).isEqualTo(expected)

        verify(audioFocus, times(3)).toggleFocus()
    }

    @Test
    fun audioFocus() {
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
    fun config() {
        val actual = processActions(ConfigAction(true), ConfigAction(false), ConfigAction(true))
        val expected = listOf(ConfigResult(true), ConfigResult(false), ConfigResult(true))
        assertThat(actual).isEqualTo(expected)
        verify(playbackObserver, times(1)).updateInfo(db.patch.title, db.patch.tempo)
    }

    @Test
    fun changeSequence() {
        val sequence = listOf(1337)

        val actual = processAction(ChangeSeqenceAction(sequence))
        val expected = ChangeSequenceResult(sequence)
        assertThat(actual).isEqualTo(expected)

        verify(pureData, times(1)).changeSequence(sequence)
        assertThat(db.acceptedPatch).isEqualTo(db.patch.copy(sequence = sequence))
    }

    @Test
    fun selectChannel() {
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

        val expectedPatch = db.patch.copy(selectedChannel = selectedChannel)
        assertThat(db.acceptedPatch).isEqualTo(expectedPatch)
    }

    @Test
    fun selectSetting() {
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

        val expectedPatch = db.patch.withSelectedSetting(selectedSetting)
        assertThat(db.acceptedPatch).isEqualTo(expectedPatch)
    }

    @Test
    fun changePosition() {
        val position = Position(.13f, .37f)

        val actual = processAction(ChangePositionAction(position))
        val expected = ChangePositionResult
        assertThat(actual).isEqualTo(expected)

        val expectedPatch = db.patch.withPosition(position)
        verify(pureData, times(1)).changeSetting(expectedPatch.channel.name, expectedPatch.channel.setting)
        assertThat(db.acceptedPatch).isEqualTo(expectedPatch)
    }

    @Test
    fun changePan() {
        val pan = .1337f

        val actual = processAction(ChangePanAction(pan))
        val expected = ChangePanResult
        assertThat(actual).isEqualTo(expected)

        val expectedPatch = db.patch.withPan(pan)
        verify(pureData, times(1)).changePan(expectedPatch.channel.name, expectedPatch.channel.pan)
        assertThat(db.acceptedPatch).isEqualTo(expectedPatch)
    }

    @Test
    fun changeTempo() {
        val tempo = 1337

        val actual = processAction(ChangeTempoAction(tempo))
        val expected = ChangeTempoResult(tempo)
        assertThat(actual).isEqualTo(expected)

        val expectedPatch = db.patch.copy(tempo = tempo)
        verify(pureData, times(1)).changeTempo(tempo)
        assertThat(db.acceptedPatch).isEqualTo(expectedPatch)
    }

    @Test
    fun changeSwing() {
        val swing = 1337

        val actual = processAction(ChangeSwingAction(swing))
        val expected = ChangeSwingResult(swing)
        assertThat(actual).isEqualTo(expected)

        val expectedPatch = db.patch.copy(swing = swing)
        verify(pureData, times(1)).changeSwing(swing)
        assertThat(db.acceptedPatch).isEqualTo(expectedPatch)
    }
}
