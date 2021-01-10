package net.simno.dmach.patch

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.scan
import net.simno.dmach.data.Patch
import net.simno.dmach.db.PatchRepository.Companion.toPatch

class PatchViewModel @ViewModelInject constructor(
    private val processor: PatchProcessor
) : ViewModel(), (Flow<Action>) -> Flow<ViewState> {

    val patches: Flow<PagingData<Patch>> = Pager(PagingConfig(pageSize = 50)) { processor.patches() }
        .flow
        .map { pagingData -> pagingData.map { entity -> entity.toPatch() } }
        .cachedIn(viewModelScope)

    override fun invoke(actions: Flow<Action>): Flow<ViewState> = actions
        .let(processor)
        .catch { emit(ErrorResult(it)) }
        .scan(ViewState(), { previousState, result -> PatchStateReducer(previousState, result) })
        .drop(1) // skip idle state
        .distinctUntilChanged()
}
