package net.simno.dmach.patch

import io.reactivex.Flowable
import io.reactivex.FlowableTransformer
import io.reactivex.schedulers.Schedulers
import net.simno.dmach.db.Db
import org.reactivestreams.Publisher

class PatchProcessor(
    private val db: Db
) : FlowableTransformer<Action, Result> {

    override fun apply(actions: Flowable<Action>): Publisher<Result> = actions.publish { shared ->
        Flowable.mergeArray<Result>(
            shared.ofType(LoadAllAction::class.java).compose(loadAll),
            shared.ofType(DismissAction::class.java).compose(dismiss),
            shared.ofType(ConfirmOverwriteAction::class.java).compose(confirmOverwrite),
            shared.ofType(ConfirmDeleteAction::class.java).compose(confirmDelete),
            shared.ofType(SavePatchAction::class.java).compose(savePatch),
            shared.ofType(DeletePatchAction::class.java).compose(deletePatch),
            shared.ofType(SelectPatchAction::class.java).compose(selectPatch)
        )
    }

    private val loadAll = FlowableTransformer<LoadAllAction, LoadAllResult> { actions ->
        actions
            .flatMap {
                db.allPatches()
            }
            .flatMap { patches ->
                db.unsavedPatch()
                    .computeResult { patch ->
                        LoadAllResult(
                            title = patch.title,
                            patches = patches
                        )
                    }
            }
    }

    private val dismiss = FlowableTransformer<DismissAction, DismissResult> { actions ->
        actions
            .computeResult {
                DismissResult
            }
    }

    private val confirmOverwrite = FlowableTransformer<ConfirmOverwriteAction, ConfirmOverwriteResult> { actions ->
        actions
            .flatMap {
                db.replacePatch()
            }
            .computeResult {
                ConfirmOverwriteResult
            }
    }

    private val confirmDelete = FlowableTransformer<ConfirmDeleteAction, ConfirmDeleteResult> { actions ->
        actions
            .flatMap {
                db.deletePatch()
            }
            .computeResult {
                ConfirmDeleteResult
            }
    }

    private val savePatch = FlowableTransformer<SavePatchAction, SavePatchResult> { actions ->
        actions
            .flatMap { action ->
                db.insertPatch(action.title)
                    .computeResult { saved ->
                        SavePatchResult(!saved, action.title)
                    }
            }
    }

    private val deletePatch = FlowableTransformer<DeletePatchAction, DeletePatchResult> { actions ->
        actions
            .map { action ->
                action.title
            }
            .doOnNext(db.acceptDeleteTitle())
            .computeResult { title ->
                DeletePatchResult(title)
            }
    }

    private val selectPatch = FlowableTransformer<SelectPatchAction, SelectPatchResult> { actions ->
        actions
            .flatMap {
                db.selectPatch(it.title)
            }
            .computeResult {
                SelectPatchResult
            }
    }

    private fun <T, R : Result> Flowable<T>.computeResult(mapper: (T) -> R): Flowable<R> = this
        .observeOn(Schedulers.computation())
        .map(mapper)
}
