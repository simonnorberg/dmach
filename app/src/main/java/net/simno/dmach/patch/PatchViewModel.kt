package net.simno.dmach.patch

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.scan
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.stateIn
import net.simno.dmach.data.Patch
import net.simno.dmach.db.PatchRepository.Companion.toPatch
import javax.inject.Inject

@HiltViewModel
class PatchViewModel @Inject constructor(
    private val processor: PatchProcessor
) : ViewModel() {

    val patches: Flow<PagingData<Patch>> = Pager(PagingConfig(pageSize = 50)) { processor.patches() }
        .flow
        .map { pagingData -> pagingData.map { entity -> entity.toPatch() } }
        .cachedIn(viewModelScope)

    private val _actions = Channel<Action>()

    private val actions: Flow<Action> = flow {
        emit(LoadAction)
        emitAll(_actions.receiveAsFlow())
    }

    val viewState: StateFlow<ViewState> = actions
        .buffer(0)
        .shareIn(viewModelScope, SharingStarted.Lazily)
        .let(processor)
        .catch { emit(ErrorResult(it)) }
        .scan(ViewState()) { previousState, result -> PatchStateReducer(previousState, result) }
        .flowOn(Dispatchers.Default)
        .stateIn(viewModelScope, SharingStarted.Lazily, ViewState())

    fun onAction(action: Action) {
        _actions.trySend(action)
    }
}
