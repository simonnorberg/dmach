package net.simno.dmach.patch

import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.DelicateCoroutinesApi
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
import net.simno.dmach.db.PatchRepository
import net.simno.dmach.db.TestPatchDao
import net.simno.dmach.machine.MachineProcessor
import net.simno.dmach.playback.AudioFocus
import net.simno.dmach.playback.PureData
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock

@DelicateCoroutinesApi
class PatchProcessorTests {

    private lateinit var repository: PatchRepository
    private lateinit var testDao: TestPatchDao
    private lateinit var patchProcessor: PatchProcessor

    private suspend fun processAction(action: Action): Result = processActions(action).first()

    private suspend fun processActions(
        vararg actions: Action
    ): List<Result> = actions.asFlow()
        .onEach { delay(10L) }
        .buffer(0)
        .shareIn(GlobalScope, SharingStarted.Lazily)
        .let(patchProcessor)
        .take(actions.size)
        .toList()

    @Before
    fun setup() {
        testDao = TestPatchDao()
        repository = PatchRepository(testDao)
        patchProcessor = PatchProcessor(repository)
        setupRepository()
    }

    private fun setupRepository() = runBlocking {
        flowOf(net.simno.dmach.machine.LoadAction)
            .onEach { delay(10L) }
            .buffer(0)
            .shareIn(GlobalScope, SharingStarted.Lazily)
            .let(MachineProcessor(emptySet(), mock(PureData::class.java), mock(AudioFocus::class.java), repository))
            .take(1)
            .toList()
    }

    @Test
    fun load() = runBlocking {
        val actual = processAction(LoadAction) as LoadResult
        assertThat(actual.title).isEqualTo(testDao.patch.title)
    }

    @Test
    fun dismiss() = runBlocking {
        val actual = processAction(DismissAction)
        val expected = DismissResult
        assertThat(actual).isEqualTo(expected)
    }

    @Test
    fun confirmOverwrite() = runBlocking {
        val actual = processAction(ConfirmOverwriteAction)
        val expected = ConfirmOverwriteResult
        assertThat(actual).isEqualTo(expected)
    }

    @Test
    fun confirmDelete() = runBlocking {
        val actual = processAction(ConfirmDeleteAction)
        val expected = ConfirmDeleteResult
        assertThat(actual).isEqualTo(expected)
    }

    @Test
    fun savePatch() = runBlocking {
        val title = "1337"
        val actual = processAction(SavePatchAction(title))
        val expected = SavePatchResult(false, title)
        assertThat(actual).isEqualTo(expected)
    }

    @Test
    fun deletePatch() = runBlocking {
        val title = "1337"
        val actual = processAction(DeletePatchAction(title))
        val expected = DeletePatchResult(title)
        processAction(ConfirmDeleteAction)
        assertThat(actual).isEqualTo(expected)
        assertThat(testDao.deleteTitle).isEqualTo(title)
    }

    @Test
    fun selectPatch() = runBlocking {
        val title = "1337"
        val actual = processAction(SelectPatchAction(title))
        val expected = SelectPatchResult
        assertThat(actual).isEqualTo(expected)
    }
}
