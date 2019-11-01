package net.simno.dmach.rx

import androidx.annotation.CheckResult
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.android.MainThreadDisposable
import net.simno.dmach.data.Patch
import net.simno.dmach.patch.PatchAdapter

@CheckResult
fun PatchAdapter.longClicks(): Observable<Patch> {
    return PatchLongClickObservable(this)
}

private class PatchLongClickObservable(
    private val adapter: PatchAdapter
) : Observable<Patch>() {
    override fun subscribeActual(observer: Observer<in Patch>) {
        if (!verifyMainThread(observer)) {
            return
        }
        val listener = Listener(adapter, observer)
        adapter.onLongClick = listener.onPatchLongClick
        observer.onSubscribe(listener)
    }

    private class Listener(
        private val adapter: PatchAdapter,
        private val observer: Observer<in Patch>
    ) : MainThreadDisposable() {
        val onPatchLongClick: (Patch) -> Unit = { patch ->
            if (!isDisposed) {
                observer.onNext(patch)
            }
        }

        override fun onDispose() {
            adapter.onLongClick = null
        }
    }
}
