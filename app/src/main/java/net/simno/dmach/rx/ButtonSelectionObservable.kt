package net.simno.dmach.rx

import android.view.View
import android.widget.Button
import androidx.annotation.CheckResult
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.android.MainThreadDisposable

@CheckResult
fun Button.selections(): Observable<Pair<Int, Boolean>> {
    return ButtonSelectionObservable(this)
}

private class ButtonSelectionObservable(
    private val button: Button
) : Observable<Pair<Int, Boolean>>() {
    override fun subscribeActual(observer: Observer<in Pair<Int, Boolean>>) {
        if (!verifyMainThread(observer)) {
            return
        }
        val listener = Listener(button, observer)
        button.setOnClickListener(listener)
        observer.onSubscribe(listener)
    }

    private class Listener(
        private val button: Button,
        private val observer: Observer<in Pair<Int, Boolean>>
    ) : MainThreadDisposable(), View.OnClickListener {
        override fun onClick(button: View) {
            if (!isDisposed) {
                observer.onNext(button.tag.toString().toInt() to button.isSelected)
            }
        }

        override fun onDispose() {
            button.setOnClickListener(null)
        }
    }
}
