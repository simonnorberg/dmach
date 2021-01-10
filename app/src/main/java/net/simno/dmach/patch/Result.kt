package net.simno.dmach.patch

sealed class Result

data class ErrorResult(
    val error: Throwable
) : Result()

data class LoadResult(
    val title: String
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
