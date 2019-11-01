package net.simno.dmach.rx

import android.content.DialogInterface
import androidx.annotation.CheckResult
import androidx.appcompat.app.AlertDialog
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.android.MainThreadDisposable

@CheckResult
fun AlertDialog.positives(text: CharSequence): Observable<Unit> {
    return DialogButtonObservable(this, DialogInterface.BUTTON_POSITIVE, text)
}

@CheckResult
fun AlertDialog.negatives(text: CharSequence): Observable<Unit> {
    return DialogButtonObservable(this, DialogInterface.BUTTON_NEGATIVE, text)
}

private class DialogButtonObservable(
    private val dialog: AlertDialog,
    private val whichButton: Int,
    private val text: CharSequence
) : Observable<Unit>() {

    override fun subscribeActual(observer: Observer<in Unit>) {
        if (!verifyMainThread(observer)) {
            return
        }
        val listener = Listener(dialog, observer)
        dialog.setButton(whichButton, text, listener)
        observer.onSubscribe(listener)
    }

    private class Listener(
        private val dialog: AlertDialog,
        private val observer: Observer<in Unit>
    ) : MainThreadDisposable(), DialogInterface.OnClickListener {
        override fun onClick(dialog: DialogInterface, which: Int) {
            if (!isDisposed) {
                observer.onNext(Unit)
            }
        }

        override fun onDispose() {
            dialog.setOnDismissListener(null)
        }
    }
}
