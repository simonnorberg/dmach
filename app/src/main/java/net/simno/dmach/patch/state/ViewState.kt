package net.simno.dmach.patch.state

data class ViewState(
    val finish: Boolean = false,
    val showDelete: Boolean = false,
    val showOverwrite: Boolean = false,
    val deleteTitle: String = "",
    val title: String = ""
)
