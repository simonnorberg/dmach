package net.simno.dmach.db

import android.database.sqlite.SQLiteConstraintException
import androidx.paging.PagingSource
import kotlinx.collections.immutable.toPersistentList
import kotlinx.collections.immutable.toPersistentSet
import kotlinx.coroutines.Dispatchers.Default
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.withContext
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.SetSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import net.simno.dmach.data.Channel
import net.simno.dmach.data.Patch
import net.simno.dmach.data.Steps
import net.simno.dmach.data.Swing
import net.simno.dmach.data.Tempo

class PatchRepository(
    private val patchDao: PatchDao
) {
    private val unsavedPatch = MutableSharedFlow<Patch>(replay = 1)
    private val deleteTitle = MutableStateFlow("")
    private val saveTitle = MutableStateFlow("")

    fun patches(): PagingSource<Int, PatchEntity> = patchDao.getAllPatches()

    suspend fun acceptPatch(patch: Patch) = withContext(IO) {
        unsavedPatch.tryEmit(patch)
    }

    suspend fun acceptDeleteTitle(title: String) = withContext(IO) {
        deleteTitle.emit(title)
    }

    suspend fun unsavedPatch(): Patch = withContext(IO) {
        unsavedPatch.replayCache.first()
    }

    fun activePatch(): Flow<Patch> = patchDao.getActivePatch()
        .filterNotNull()
        .map { it.toPatch() }
        .onEach { acceptPatch(it) }

    suspend fun selectPatch(title: String): Int = withContext(IO) {
        patchDao.selectPatch(title)
    }

    suspend fun deletePatch(): Int = withContext(IO) {
        patchDao.deletePatch(deleteTitle.value)
    }

    suspend fun insertPatch(title: String): Boolean = withContext(IO) {
        val patch = unsavedPatch().toEntity(title)
        val inserted = try {
            patchDao.insertPatch(patch) != 0L
        } catch (ignored: SQLiteConstraintException) {
            false
        }
        if (!inserted) {
            saveTitle.emit(title)
        }
        inserted
    }

    suspend fun replacePatch(): Long = withContext(IO) {
        val patch = unsavedPatch().toEntity(saveTitle.value)
        patchDao.replacePatch(patch)
    }

    companion object {
        suspend fun PatchEntity.toPatch(): Patch = withContext(Default) {
            Patch(
                title = title,
                sequence = Json.decodeFromString(ListSerializer(Int.serializer()), sequence).toPersistentList(),
                mutedChannels = Json.decodeFromString(SetSerializer(Int.serializer()), muted).toPersistentSet(),
                channels = Json.decodeFromString(ListSerializer(Channel.serializer()), channels).toPersistentList(),
                selectedChannel = selected,
                tempo = Tempo(tempo),
                swing = Swing(swing),
                steps = Steps(steps)
            )
        }

        suspend fun Patch.toEntity(title: String): PatchEntity = withContext(Default) {
            PatchEntity(
                id = null,
                title = title,
                sequence = Json.encodeToString(ListSerializer(Int.serializer()), sequence),
                muted = Json.encodeToString(SetSerializer(Int.serializer()), mutedChannels),
                channels = Json.encodeToString(ListSerializer(Channel.serializer()), channels),
                selected = selectedChannel,
                tempo = tempo.value,
                swing = swing.value,
                steps = steps.value,
                active = true
            )
        }
    }
}
