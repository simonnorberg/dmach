package net.simno.dmach.patch.state

sealed class Result

data class ErrorResult(
    val error: Throwable
) : Result()

data class LoadResult(
    val title: String
) : Result()

data object DismissResult : Result()

data object ConfirmOverwriteResult : Result()

data object ConfirmDeleteResult : Result()

data class SavePatchResult(
    val showOverwrite: Boolean,
    val title: String
) : Result()

data class DeletePatchResult(
    val deleteTitle: String
) : Result()

data object SelectPatchResult : Result()
