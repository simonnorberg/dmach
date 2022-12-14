package net.simno.dmach.machine

import androidx.lifecycle.LifecycleObserver
import dagger.hilt.android.lifecycle.HiltViewModel
import net.simno.dmach.StateViewModel
import net.simno.dmach.machine.state.Action
import net.simno.dmach.machine.state.ErrorResult
import net.simno.dmach.machine.state.LoadAction
import net.simno.dmach.machine.state.MachineProcessor
import net.simno.dmach.machine.state.MachineStateReducer
import net.simno.dmach.machine.state.PlaybackAction
import net.simno.dmach.machine.state.Result
import net.simno.dmach.machine.state.SettingsAction
import net.simno.dmach.machine.state.ViewState
import javax.inject.Inject

@HiltViewModel
class MachineViewModel @Inject constructor(
    processor: MachineProcessor
) : StateViewModel<Action, Result, ViewState>(
    processor = processor,
    reducer = MachineStateReducer,
    onError = { ErrorResult(it) },
    startViewState = ViewState(),
    LoadAction,
    PlaybackAction,
    SettingsAction
) {
    val lifecycleObservers: Set<LifecycleObserver> = processor.playbackObservers
}
