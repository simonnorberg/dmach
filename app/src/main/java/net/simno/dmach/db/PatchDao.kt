package net.simno.dmach.db

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface PatchDao {

    @Query("SELECT COUNT(*) FROM patch")
    suspend fun count(): Int

    @Query("SELECT * FROM patch WHERE active = 1 LIMIT 1")
    fun getActivePatch(): Flow<PatchEntity?>

    @Query("SELECT * FROM patch ORDER BY title")
    fun getAllPatches(): PagingSource<Int, PatchEntity>

    @Query("DELETE FROM patch WHERE title = :title")
    suspend fun deletePatch(title: String): Int

    @Query("UPDATE patch SET active = 0 WHERE active = 1")
    suspend fun internalResetActive()

    @Query("UPDATE patch SET active = 1 WHERE title = :title")
    suspend fun internalSetActive(title: String): Int

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun internalInsertPatch(patch: PatchEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun internalReplacePatch(patch: PatchEntity): Long

    @Transaction
    suspend fun selectPatch(title: String): Int {
        internalResetActive()
        return internalSetActive(title)
    }

    @Transaction
    suspend fun insertPatch(patch: PatchEntity): Long {
        internalResetActive()
        return internalInsertPatch(patch)
    }

    @Transaction
    suspend fun replacePatch(patch: PatchEntity): Long {
        internalResetActive()
        return internalReplacePatch(patch)
    }
}
