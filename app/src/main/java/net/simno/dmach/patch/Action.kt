package net.simno.dmach.patch

sealed class Action

object LoadAllAction : Action()

object DismissAction : Action()

object ConfirmOverwriteAction : Action()

object ConfirmDeleteAction : Action()

data class SavePatchAction(
    val title: String
) : Action()

data class DeletePatchAction(
    val title: String
) : Action()

data class SelectPatchAction(
    val title: String
) : Action()
