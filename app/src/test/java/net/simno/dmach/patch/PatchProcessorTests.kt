package net.simno.dmach.patch

import com.google.common.truth.Truth.assertThat
import io.reactivex.Flowable
import net.simno.dmach.db.TestDb
import org.junit.Before
import org.junit.Test

class PatchProcessorTests {

    private lateinit var db: TestDb

    private lateinit var patchProcessor: PatchProcessor

    private fun processAction(action: Action): Result = processActions(action).first()

    private fun processActions(vararg actions: Action): List<Result> {
        val test = Flowable.fromIterable(actions.toList())
            .compose(patchProcessor)
            .test()

        test.awaitTerminalEvent()
        test.assertNoErrors()
        return test.values()
    }

    @Before
    fun setup() {
        db = TestDb()
        patchProcessor = PatchProcessor(db)
    }

    @Test
    fun loadAll() {
        val actual = processAction(LoadAllAction) as LoadAllResult
        assertThat(actual.title).isEqualTo("test")
        assertThat(actual.patches).isEqualTo(listOf(db.patch, db.patch))
    }

    @Test
    fun dismiss() {
        val actual = processAction(DismissAction)
        val expected = DismissResult
        assertThat(actual).isEqualTo(expected)
    }

    @Test
    fun confirmOverwrite() {
        val actual = processAction(ConfirmOverwriteAction)
        val expected = ConfirmOverwriteResult
        assertThat(actual).isEqualTo(expected)
    }

    @Test
    fun confirmDelete() {
        val actual = processAction(ConfirmDeleteAction)
        val expected = ConfirmDeleteResult
        assertThat(actual).isEqualTo(expected)
    }

    @Test
    fun savePatch() {
        val title = "1337"
        val actual = processAction(SavePatchAction(title))
        val expected = SavePatchResult(false, title)
        assertThat(actual).isEqualTo(expected)
    }

    @Test
    fun deletePatch() {
        val title = "1337"
        val actual = processAction(DeletePatchAction(title))
        val expected = DeletePatchResult(title)
        assertThat(actual).isEqualTo(expected)
        assertThat(db.acceptedDeleteTitle).isEqualTo(title)
    }

    @Test
    fun selectPatch() {
        val title = "1337"
        val actual = processAction(SelectPatchAction(title))
        val expected = SelectPatchResult
        assertThat(actual).isEqualTo(expected)
    }
}
