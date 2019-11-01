package net.simno.dmach.rx

import android.graphics.PointF
import androidx.annotation.CheckResult
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.android.MainThreadDisposable
import net.simno.dmach.data.Position
import net.simno.dmach.machine.view.Positioner

@CheckResult
fun Positioner.positions(): Observable<Position> {
    return PositionObservable(this)
}

private class PositionObservable(
    private val positioner: Positioner
) : Observable<Position>() {

    override fun subscribeActual(observer: Observer<in Position>) {
        if (!verifyMainThread(observer)) {
            return
        }
        val listener = Listener(positioner, observer)
        observer.onSubscribe(listener)
        positioner.onPositionChangedListener = listener
    }

    private class Listener(
        private val positioner: Positioner,
        private val observer: Observer<in Position>
    ) : MainThreadDisposable(), Positioner.OnPositionChangedListener {

        override fun onPositionChanged(point: PointF) {
            if (!isDisposed) {
                observer.onNext(Position(point.x, point.y))
            }
        }

        override fun onDispose() {
            positioner.onPositionChangedListener = null
        }
    }
}
