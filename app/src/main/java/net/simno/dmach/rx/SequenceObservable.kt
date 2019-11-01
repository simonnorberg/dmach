package net.simno.dmach.rx

import androidx.annotation.CheckResult
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.android.MainThreadDisposable
import net.simno.dmach.machine.view.StepSequencer

@CheckResult
fun StepSequencer.sequences(): Observable<List<Int>> {
    return SequenceObservable(this)
}

private class SequenceObservable(
    private val sequencer: StepSequencer
) : Observable<List<Int>>() {

    override fun subscribeActual(observer: Observer<in List<Int>>) {
        if (!verifyMainThread(observer)) {
            return
        }
        val listener = Listener(sequencer, observer)
        observer.onSubscribe(listener)
        sequencer.onSequenceChangedListener = listener
    }

    private class Listener(
        private val sequencer: StepSequencer,
        private val observer: Observer<in List<Int>>
    ) : MainThreadDisposable(), StepSequencer.OnSequenceChangedListener {
        override fun onSequenceChanged(sequence: List<Int>) {
            if (!isDisposed) {
                observer.onNext(sequence)
            }
        }

        override fun onDispose() {
            sequencer.onSequenceChangedListener = null
        }
    }
}
