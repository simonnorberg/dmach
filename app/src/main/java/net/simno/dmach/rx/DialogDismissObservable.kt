package net.simno.dmach.rx

import android.content.DialogInterface
import androidx.annotation.CheckResult
import androidx.appcompat.app.AlertDialog
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.android.MainThreadDisposable

@CheckResult
fun AlertDialog.dismisses(): Observable<Unit> {
    return DialogDismissObservable(this)
}

private class DialogDismissObservable(
    private val dialog: AlertDialog
) : Observable<Unit>() {

    override fun subscribeActual(observer: Observer<in Unit>) {
        if (!verifyMainThread(observer)) {
            return
        }
        val listener = Listener(dialog, observer)
        dialog.setOnDismissListener(listener)
        observer.onSubscribe(listener)
    }

    private class Listener(
        private val dialog: AlertDialog,
        private val observer: Observer<in Unit>
    ) : MainThreadDisposable(), DialogInterface.OnDismissListener {
        override fun onDismiss(dialog: DialogInterface) {
            if (!isDisposed) {
                observer.onNext(Unit)
            }
        }

        override fun onDispose() {
            dialog.setOnDismissListener(null)
        }
    }
}
