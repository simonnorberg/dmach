package net.simno.dmach.db

import androidx.paging.PagingSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import net.simno.dmach.data.Channel
import net.simno.dmach.data.Patch
import net.simno.dmach.data.Setting
import net.simno.dmach.db.PatchRepository.Companion.toEntity

class TestPatchDao : PatchDao {
    var deleteTitle = ""

    private val settings = listOf(
        Setting("1", "2", 1, 2, .1f, .2f),
        Setting("3", "4", 3, 4, .3f, .4f),
        Setting("5", "6", 5, 6, .5f, .6f),
        Setting("7", "8", 7, 8, .7f, .8f)
    )
    val patch = Patch(
        title = "test",
        sequence = Patch.RANDOM_SEQUENCE,
        channels = listOf("bd", "sd", "cp", "tt", "cb", "hh").map { Channel(it, settings, 0, 0.5f) },
        selectedChannel = 1,
        tempo = 123,
        swing = 10
    )

    private val patches = listOf(patch, patch)

    private val dataSource = object : PagingSource<Int, PatchEntity>() {
        override suspend fun load(params: LoadParams<Int>): LoadResult<Int, PatchEntity> {
            return LoadResult.Page(data = patches.map { it.toEntity(it.title) }, null, null)
        }
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
