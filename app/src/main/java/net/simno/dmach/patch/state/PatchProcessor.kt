package net.simno.dmach.patch.state

import androidx.paging.PagingSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import net.simno.dmach.db.PatchEntity
import net.simno.dmach.db.PatchRepository

class PatchProcessor(
    private val patchRepository: PatchRepository
) : (Flow<Action>) -> Flow<Result> {

    override fun invoke(actions: Flow<Action>): Flow<Result> = merge(
        actions.filterIsInstance<LoadAction>().let(load),
        actions.filterIsInstance<DismissAction>().let(dismiss),
        actions.filterIsInstance<ConfirmOverwriteAction>().let(confirmOverwrite),
        actions.filterIsInstance<ConfirmDeleteAction>().let(confirmDelete),
        actions.filterIsInstance<SavePatchAction>().let(savePatch),
        actions.filterIsInstance<DeletePatchAction>().let(deletePatch),
        actions.filterIsInstance<SelectPatchAction>().let(selectPatch)
    )

    fun patches(): PagingSource<Int, PatchEntity> = patchRepository.getAllPatches()

    private val load: (Flow<LoadAction>) -> Flow<LoadResult> = { actions ->
        actions
            .computeResult {
                val patch = patchRepository.unsavedPatch()
                LoadResult(
                    title = patch.title
                )
            }
    }

    private val dismiss: (Flow<DismissAction>) -> Flow<DismissResult> = { actions ->
        actions
            .computeResult {
                DismissResult
            }
    }

    private val confirmOverwrite: (Flow<ConfirmOverwriteAction>) -> Flow<ConfirmOverwriteResult> = { actions ->
        actions
            .computeResult {
                patchRepository.replacePatch()
                ConfirmOverwriteResult
            }
    }

    private val confirmDelete: (Flow<ConfirmDeleteAction>) -> Flow<ConfirmDeleteResult> = { actions ->
        actions
            .computeResult {
                patchRepository.deletePatch()
                ConfirmDeleteResult
            }
    }

    private val savePatch: (Flow<SavePatchAction>) -> Flow<SavePatchResult> = { actions ->
        actions
            .computeResult { action ->
                val saved = patchRepository.insertPatch(action.title)
                SavePatchResult(!saved, action.title)
            }
    }

    private val deletePatch: (Flow<DeletePatchAction>) -> Flow<DeletePatchResult> = { actions ->
        actions
            .computeResult { action ->
                patchRepository.acceptDeleteTitle(action.title)
                DeletePatchResult(action.title)
            }
    }

    private val selectPatch: (Flow<SelectPatchAction>) -> Flow<SelectPatchResult> = { actions ->
        actions
            .computeResult { action ->
                patchRepository.selectPatch(action.title)
                SelectPatchResult
            }
    }

    private fun <T, R : Result> Flow<T>.computeResult(mapper: suspend (T) -> R): Flow<R> = map(mapper)
}
