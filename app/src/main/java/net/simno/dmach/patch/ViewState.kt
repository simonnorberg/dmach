package net.simno.dmach.patch

import androidx.paging.PagedList
import net.simno.dmach.data.Patch

data class ViewState(
    val finish: Boolean = false,
    val showDelete: Boolean = false,
    val showOverwrite: Boolean = false,
    val deleteTitle: String = "",
    val title: String = "",
    val patches: PagedList<Patch>? = null
)
