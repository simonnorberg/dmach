package net.simno.dmach.machine

import androidx.lifecycle.ViewModel
import io.reactivex.Flowable
import io.reactivex.FlowableTransformer
import org.reactivestreams.Publisher

class MachineViewModel(
    private val processor: MachineProcessor
) : ViewModel(), FlowableTransformer<Action, ViewState> {
    override fun apply(actions: Flowable<Action>): Publisher<ViewState> = actions
        .compose(processor)
        .onErrorReturn(::ErrorResult)
        .scan(ViewState(), MachineStateReducer)
        .skip(1) // skip idle state
        .distinctUntilChanged()
}
