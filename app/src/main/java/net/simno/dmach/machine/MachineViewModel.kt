package net.simno.dmach.machine

import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.scan
import javax.inject.Inject

@HiltViewModel
class MachineViewModel @Inject constructor(
    private val processor: MachineProcessor
) : ViewModel(), (Flow<Action>) -> Flow<ViewState> {

    val lifecycleObservers: Set<LifecycleObserver> = processor.playbackObservers

    override fun invoke(actions: Flow<Action>): Flow<ViewState> = actions
        .let(processor)
        .catch { emit(ErrorResult(it)) }
        .scan(ViewState(), { previousState, result -> MachineStateReducer(previousState, result) })
        .drop(1) // skip idle state
        .distinctUntilChanged()
}
