package net.simno.dmach.machine

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.scan

class MachineViewModel @ViewModelInject constructor(
    private val processor: MachineProcessor
) : ViewModel(), (Flow<Action>) -> Flow<ViewState> {
    override fun invoke(actions: Flow<Action>): Flow<ViewState> = actions
        .let(processor)
        .catch { emit(ErrorResult(it)) }
        .scan(ViewState(), { previousState, result -> MachineStateReducer(previousState, result) })
        .drop(1) // skip idle state
        .distinctUntilChanged()
}
