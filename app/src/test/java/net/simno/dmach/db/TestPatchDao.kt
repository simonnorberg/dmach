package net.simno.dmach.db

import androidx.paging.PagingSource
import androidx.paging.PagingState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import net.simno.dmach.data.Steps
import net.simno.dmach.data.Swing
import net.simno.dmach.data.Tempo
import net.simno.dmach.data.defaultPatch
import net.simno.dmach.db.PatchRepository.Companion.toEntity
import net.simno.dmach.machine.state.Randomizer

class TestPatchDao : PatchDao {
    var deleteTitle = ""

    val patch = defaultPatch().copy(
        title = "test",
        sequence = Randomizer.DEFAULT.nextSequence(),
        selectedChannel = 1,
        tempo = Tempo(123),
        swing = Swing(10),
        steps = Steps(16)
    )

    private val patches = listOf(patch, patch)

    private val dataSource = object : PagingSource<Int, PatchEntity>() {
        override suspend fun load(params: LoadParams<Int>): LoadResult<Int, PatchEntity> =
            LoadResult.Page(data = patches.map { it.toEntity(it.title) }, null, null)

        override fun getRefreshKey(state: PagingState<Int, PatchEntity>): Int? = null
    }

    override suspend fun count(): Int = patches.size

    override fun getActivePatch(): Flow<PatchEntity> = flow { emit(patch.toEntity(patch.title)) }

    override fun getAllPatches(): PagingSource<Int, PatchEntity> = dataSource

    override suspend fun deletePatch(title: String): Int {
        deleteTitle = title
        return 1
    }

    override suspend fun internalResetActive() = Unit

    override suspend fun internalSetActive(title: String): Int = 1

    override suspend fun internalInsertPatch(patch: PatchEntity): Long = 1

    override suspend fun internalReplacePatch(patch: PatchEntity): Long = 1
}
