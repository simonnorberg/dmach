package net.simno.dmach.db

import androidx.paging.PagedList
import androidx.paging.PositionalDataSource
import io.reactivex.Flowable
import io.reactivex.functions.Consumer
import net.simno.dmach.data.Channel
import net.simno.dmach.data.Patch
import net.simno.dmach.data.Setting
import java.util.concurrent.Executors

class TestDb : Db {
    private val settings = listOf(
        Setting("1", "2", 1, 2, .1f, .2f),
        Setting("3", "4", 3, 4, .3f, .4f),
        Setting("5", "6", 5, 6, .5f, .6f),
        Setting("7", "8", 7, 8, .7f, .8f)
    )
    val patch = Patch(
        title = "test",
        sequence = listOf(1, 2, 3, 4, 5),
        channels = listOf("bd", "sd", "cp", "tt", "cb", "hh").map { Channel(it, settings, 0, 0.5f) },
        selectedChannel = 1,
        tempo = 123,
        swing = 10
    )
    lateinit var acceptedPatch: Patch
    lateinit var acceptedDeleteTitle: String

    private val patches = listOf(patch, patch)
    private val dataSource = object : PositionalDataSource<Patch>() {
        override fun loadRange(params: LoadRangeParams, callback: LoadRangeCallback<Patch>) {
            callback.onResult(patches)
        }

        override fun loadInitial(params: LoadInitialParams, callback: LoadInitialCallback<Patch>) {
            callback.onResult(patches, 0, 2)
        }
    }
    private val executor = Executors.newSingleThreadExecutor()
    private val pagedList = PagedList.Builder(dataSource, PagedList.Config.Builder().setPageSize(50).build())
        .setNotifyExecutor(executor)
        .setFetchExecutor(executor)
        .build()

    override fun acceptPatch(): Consumer<Patch> = Consumer { patch ->
        acceptedPatch = patch
    }

    override fun acceptDeleteTitle(): Consumer<String> = Consumer { deleteTitle ->
        acceptedDeleteTitle = deleteTitle
    }

    override fun unsavedPatch(): Flowable<Patch> = Flowable.just(patch)

    override fun activePatch(): Flowable<Patch> = Flowable.just(patch)

    override fun allPatches(): Flowable<PagedList<Patch>> = Flowable.just(pagedList)

    override fun selectPatch(title: String): Flowable<Int> = Flowable.just(1)

    override fun deletePatch(): Flowable<Int> = Flowable.just(1)

    override fun insertPatch(title: String): Flowable<Boolean> = Flowable.just(true)

    override fun replacePatch(): Flowable<Long> = Flowable.just(1)
}
