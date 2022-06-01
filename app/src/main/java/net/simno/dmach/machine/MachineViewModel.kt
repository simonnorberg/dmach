package net.simno.dmach.machine

import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.scan
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class MachineViewModel @Inject constructor(
    processor: MachineProcessor
) : ViewModel() {

    val lifecycleObservers: Set<LifecycleObserver> = processor.playbackObservers

    private val _actions = Channel<Action>()

    private val actions: Flow<Action> = flow {
        emitAll(flowOf(LoadAction, PlaybackAction))
        emitAll(_actions.receiveAsFlow())
    }

    val viewState: StateFlow<ViewState> = actions
        .buffer(0)
        .shareIn(viewModelScope, SharingStarted.Lazily)
        .let(processor)
        .catch { emit(ErrorResult(it)) }
        .scan(ViewState()) { previousState, result -> MachineStateReducer(previousState, result) }
        .flowOn(Dispatchers.Default)
        .stateIn(viewModelScope, SharingStarted.Lazily, ViewState())

    fun onAction(action: Action) {
        _actions.trySend(action)
    }
}
