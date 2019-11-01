package net.simno.dmach.rx

import androidx.annotation.CheckResult
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.android.MainThreadDisposable
import net.simno.dmach.machine.view.ValueChanger

@CheckResult
fun ValueChanger.values(): Observable<Int> {
    return ValueObservable(this)
}

private class ValueObservable(
    private val valueChanger: ValueChanger
) : Observable<Int>() {

    override fun subscribeActual(observer: Observer<in Int>) {
        if (!verifyMainThread(observer)) {
            return
        }
        val listener = Listener(valueChanger, observer)
        observer.onSubscribe(listener)
        valueChanger.onValueChangedListener = listener
    }

    private class Listener(
        private val valueChanger: ValueChanger,
        private val observer: Observer<in Int>
    ) : MainThreadDisposable(), ValueChanger.ValueChangedListener {
        override fun onValueChanged(value: Int) {
            if (!isDisposed) {
                observer.onNext(value)
            }
        }

        override fun onDispose() {
            valueChanger.onValueChangedListener = null
        }
    }
}
