package net.simno.dmach

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.scan
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.stateIn

abstract class StateViewModel<Action, Result, ViewState>(
    processor: (Flow<Action>) -> Flow<Result>,
    reducer: (ViewState, Result) -> ViewState,
    onError: (Throwable) -> Result,
    startViewState: ViewState,
    vararg startActions: Action
) : ViewModel() {

    private val _actions = Channel<Action>()

    private val actions: Flow<Action> = flow {
        emitAll(flowOf(*startActions))
        emitAll(_actions.receiveAsFlow())
    }

    val viewState: StateFlow<ViewState> = actions
        .buffer(0)
        .shareIn(viewModelScope, SharingStarted.Lazily)
        .let(processor)
        .catch { emit(onError(it)) }
        .scan(startViewState) { previousState, result -> reducer(previousState, result) }
        .flowOn(Dispatchers.Default)
        .stateIn(viewModelScope, SharingStarted.Lazily, startViewState)

    fun onAction(action: Action) {
        _actions.trySend(action)
    }
}
