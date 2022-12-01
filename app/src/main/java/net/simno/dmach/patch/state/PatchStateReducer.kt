package net.simno.dmach.patch.state

import net.simno.dmach.util.logError

object PatchStateReducer : (ViewState, Result) -> ViewState {
    override fun invoke(previousState: ViewState, result: Result) = when (result) {
        is ErrorResult -> {
            logError("PatchStateReducer", "ErrorResult", result.error)
            previousState
        }
        is LoadResult -> previousState.copy(
            title = result.title
        )
        DismissResult -> previousState.copy(
            showDelete = false,
            showOverwrite = false
        )
        ConfirmOverwriteResult -> previousState.copy(
            finish = true,
            showOverwrite = false
        )
        ConfirmDeleteResult -> previousState.copy(
            showDelete = false
        )
        is SavePatchResult -> previousState.copy(
            finish = !result.showOverwrite,
            showOverwrite = result.showOverwrite,
            title = result.title
        )
        is DeletePatchResult -> previousState.copy(
            showDelete = true,
            deleteTitle = result.deleteTitle
        )
        SelectPatchResult -> previousState.copy(
            finish = true
        )
    }
}
