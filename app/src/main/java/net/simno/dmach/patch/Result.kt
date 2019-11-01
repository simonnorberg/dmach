package net.simno.dmach.patch

import androidx.paging.PagedList
import net.simno.dmach.data.Patch

sealed class Result

data class ErrorResult(
    val error: Throwable
) : Result()

data class LoadAllResult(
    val title: String,
    val patches: PagedList<Patch>
) : Result()

object DismissResult : Result()

object ConfirmOverwriteResult : Result()

object ConfirmDeleteResult : Result()

data class SavePatchResult(
    val showOverwrite: Boolean,
    val title: String
) : Result()

data class DeletePatchResult(
    val deleteTitle: String
) : Result()

object SelectPatchResult : Result()
