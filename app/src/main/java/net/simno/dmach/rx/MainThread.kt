package net.simno.dmach.rx

import io.reactivex.Observer
import io.reactivex.android.MainThreadDisposable
import io.reactivex.disposables.Disposables

fun verifyMainThread(observer: Observer<*>): Boolean {
    return try {
        MainThreadDisposable.verifyMainThread()
        true
    } catch (error: IllegalStateException) {
        observer.onSubscribe(Disposables.empty())
        observer.onError(error)
        false
    }
}
