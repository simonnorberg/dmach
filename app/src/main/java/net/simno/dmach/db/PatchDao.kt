package net.simno.dmach.db

import androidx.paging.DataSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import io.reactivex.Flowable

@Dao
interface PatchDao {

    @Query("SELECT * FROM patch WHERE active = 1 LIMIT 1")
    fun getActivePatch(): Flowable<PatchEntity>

    @Query("SELECT * FROM patch ORDER BY title")
    fun getAllPatches(): DataSource.Factory<Int, PatchEntity>

    @Query("DELETE FROM patch WHERE title = :title")
    fun deletePatch(title: String): Int

    @Query("UPDATE patch SET active = 0 WHERE active = 1")
    fun internalResetActive()

    @Query("UPDATE patch SET active = 1 WHERE title = :title")
    fun internalSetActive(title: String): Int

    @Insert(onConflict = OnConflictStrategy.ABORT)
    fun internalInsertPatch(patch: PatchEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun internalReplacePatch(patch: PatchEntity): Long

    @Transaction
    fun selectPatch(title: String): Int {
        internalResetActive()
        return internalSetActive(title)
    }

    @Transaction
    fun insertPatch(patch: PatchEntity): Long {
        internalResetActive()
        return internalInsertPatch(patch)
    }

    @Transaction
    fun replacePatch(patch: PatchEntity): Long {
        internalResetActive()
        return internalReplacePatch(patch)
    }
}
