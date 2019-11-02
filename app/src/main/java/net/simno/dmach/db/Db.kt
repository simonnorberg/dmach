package net.simno.dmach.db

import android.app.Application
import android.database.sqlite.SQLiteConstraintException
import androidx.paging.PagedList
import androidx.paging.toFlowable
import androidx.room.Room
import com.jakewharton.rxrelay2.BehaviorRelay
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.Scheduler
import io.reactivex.functions.Consumer
import io.reactivex.schedulers.Schedulers
import net.simno.dmach.data.Patch
import java.util.concurrent.Executors

interface Db {
    fun acceptPatch(): Consumer<Patch>
    fun acceptDeleteTitle(): Consumer<String>
    fun unsavedPatch(): Flowable<Patch>
    fun activePatch(): Flowable<Patch>
    fun allPatches(): Flowable<PagedList<Patch>>
    fun selectPatch(title: String): Flowable<Int>
    fun deletePatch(): Flowable<Int>
    fun insertPatch(title: String): Flowable<Boolean>
    fun replacePatch(): Flowable<Long>

    companion object {
        fun create(application: Application): Db {
            val singleThreadExecutor = Executors.newSingleThreadExecutor()

            val patchDatabase = Room
                .databaseBuilder(application, PatchDatabase::class.java, PatchDatabase.NAME)
                .setTransactionExecutor(singleThreadExecutor)
                .createFromAsset("dmach.db")
                .addMigrations(PatchDatabase.MIGRATION_1_2, PatchDatabase.MIGRATION_2_3)
                .build()

            val dbScheduler = Schedulers.from(singleThreadExecutor)

            return RoomDb(patchDatabase.patchDao(), dbScheduler)
        }
    }
}

private class RoomDb(
    private val patchDao: PatchDao,
    private val dbScheduler: Scheduler
) : Db {
    private val unsavedPatch = BehaviorRelay.create<Patch>()
    private val deleteTitle = BehaviorRelay.createDefault("")
    private val saveTitle = BehaviorRelay.createDefault("")

    override fun acceptPatch(): Consumer<Patch> = Consumer { patch ->
        unsavedPatch.accept(patch)
    }

    override fun acceptDeleteTitle(): Consumer<String> = Consumer { title ->
        deleteTitle.accept(title)
    }

    override fun unsavedPatch(): Flowable<Patch> = Flowable
        .defer {
            unsavedPatch
                .take(1)
                .toFlowable(BackpressureStrategy.LATEST)
        }
        .subscribeOn(dbScheduler)

    override fun activePatch(): Flowable<Patch> = patchDao
        .getActivePatch()
        .map(EntityToPatch)
        .doOnNext(acceptPatch())

    override fun allPatches(): Flowable<PagedList<Patch>> = patchDao
        .getAllPatches()
        .map(EntityToPatch)
        .toFlowable(pageSize = 50, notifyScheduler = dbScheduler, fetchScheduler = dbScheduler)

    override fun selectPatch(title: String): Flowable<Int> = Flowable
        .fromCallable { patchDao.selectPatch(title) }
        .subscribeOn(dbScheduler)

    override fun deletePatch(): Flowable<Int> = Flowable
        .fromCallable { patchDao.deletePatch(deleteTitle.value.orEmpty()) }
        .subscribeOn(dbScheduler)

    override fun insertPatch(title: String): Flowable<Boolean> = unsavedPatch()
        .map(PatchToEntity(title))
        .map { patch ->
            try {
                patchDao.insertPatch(patch) != 0L
            } catch (ignored: SQLiteConstraintException) {
                false
            }
        }
        .doOnNext { inserted ->
            if (!inserted) {
                saveTitle.accept(title)
            }
        }

    override fun replacePatch(): Flowable<Long> = unsavedPatch()
        .map(PatchToEntity(saveTitle.value.orEmpty()))
        .map { patch ->
            patchDao.replacePatch(patch)
        }
}
