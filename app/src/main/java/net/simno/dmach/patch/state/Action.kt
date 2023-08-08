package net.simno.dmach.patch.state

sealed class Action

data object LoadAction : Action()

data object DismissAction : Action()

data object ConfirmOverwriteAction : Action()

data object ConfirmDeleteAction : Action()

data class SavePatchAction(
    val title: String
) : Action()

data class DeletePatchAction(
    val title: String
) : Action()

data class SelectPatchAction(
    val title: String
) : Action()
