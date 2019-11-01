package net.simno.dmach.patch

import io.reactivex.functions.BiFunction
import net.simno.dmach.logError

object PatchStateReducer : BiFunction<ViewState, Result, ViewState> {
    override fun apply(previousState: ViewState, result: Result) = when (result) {
        is ErrorResult -> {
            logError("PatchStateReducer", "ErrorResult", result.error)
            previousState
        }
        is LoadAllResult -> previousState.copy(
            title = result.title,
            patches = result.patches
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
