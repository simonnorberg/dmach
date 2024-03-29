package net.simno.dmach.machine

import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
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
)
