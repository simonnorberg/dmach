package net.simno.dmach.machine.view

import android.annotation.SuppressLint
import android.content.Context
import android.os.Looper
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.core.os.HandlerCompat
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

abstract class ValueChanger @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    protected abstract val maxValue: Int
    protected abstract val minValue: Int

    private val mainHandler = HandlerCompat.createAsync(Looper.getMainLooper())

    private val _values = MutableSharedFlow<Int>(
        replay = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val values: SharedFlow<Int> = _values.asSharedFlow()

    private var value = 0
    private var delay = 500L
    private var change = 1

    override fun hasOverlappingRendering(): Boolean = false

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        val x = event.x
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> onActionDown(x)
            MotionEvent.ACTION_MOVE -> onActionMove(x)
            MotionEvent.ACTION_UP -> onActionUpOrCancel()
            MotionEvent.ACTION_CANCEL -> onActionUpOrCancel()
        }
        return true
    }

    fun setValue(newValue: Int) {
        if (newValue in minValue..maxValue) {
            value = newValue
        }
    }

    private fun onActionDown(x: Float) {
        HandlerCompat.postDelayed(
            mainHandler,
            { calculateChangeAndDelay(x) },
            null,
            0
        )
        HandlerCompat.postDelayed(
            mainHandler,
            delayedAction {
                val newValue = value + change
                if (newValue in minValue..maxValue) {
                    value = newValue
                    _values.tryEmit(newValue)
                }
            },
            null,
            0
        )
    }

    private fun onActionMove(x: Float) {
        calculateChangeAndDelay(x)
    }

    private fun onActionUpOrCancel() {
        mainHandler.removeCallbacksAndMessages(null)
    }

    private fun calculateChangeAndDelay(x: Float) {
        val width = width.toFloat()
        if (width <= 0) {
            return
        }
        val center = width / 2f
        val validX = x.coerceIn(0f, width)

        change = if (validX > center) 1 else -1

        val delayMultiplier = if (validX <= center) {
            validX / center
        } else {
            1 - ((validX - center) / (width - center))
        }

        delay = (((MAX_DELAY - MIN_DELAY) * delayMultiplier) + MIN_DELAY).toLong()
    }

    private fun delayedAction(action: () -> Unit): () -> Unit = {
        action.invoke()
        HandlerCompat.postDelayed(mainHandler, delayedAction(action), null, delay)
    }

    companion object {
        private const val MAX_DELAY = 400f
        private const val MIN_DELAY = 32f
    }
}
