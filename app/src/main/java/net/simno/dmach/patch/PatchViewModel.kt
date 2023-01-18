package net.simno.dmach.patch

import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import net.simno.dmach.StateViewModel
import net.simno.dmach.data.Patch
import net.simno.dmach.db.PatchRepository.Companion.toPatch
import net.simno.dmach.patch.state.Action
import net.simno.dmach.patch.state.ErrorResult
import net.simno.dmach.patch.state.LoadAction
import net.simno.dmach.patch.state.PatchProcessor
import net.simno.dmach.patch.state.PatchStateReducer
import net.simno.dmach.patch.state.Result
import net.simno.dmach.patch.state.ViewState

@HiltViewModel
class PatchViewModel @Inject constructor(
    private val processor: PatchProcessor
) : StateViewModel<Action, Result, ViewState>(
    processor = processor,
    reducer = PatchStateReducer,
    onError = { ErrorResult(it) },
    startViewState = ViewState(),
    LoadAction
) {
    val patches: Flow<PagingData<Patch>> = Pager(PagingConfig(pageSize = 50)) { processor.patches() }
        .flow
        .map { pagingData -> pagingData.map { entity -> entity.toPatch() } }
        .cachedIn(viewModelScope)
}
