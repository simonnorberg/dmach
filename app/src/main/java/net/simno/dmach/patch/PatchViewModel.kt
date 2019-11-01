package net.simno.dmach.patch

import androidx.lifecycle.ViewModel
import io.reactivex.Flowable
import io.reactivex.FlowableTransformer
import org.reactivestreams.Publisher

class PatchViewModel(
    private val processor: PatchProcessor
) : ViewModel(), FlowableTransformer<Action, ViewState> {
    override fun apply(actions: Flowable<Action>): Publisher<ViewState> = actions
        .compose(processor)
        .onErrorReturn(::ErrorResult)
        .scan(ViewState(), PatchStateReducer)
        .skip(1) // skip idle state
        .distinctUntilChanged()
}
