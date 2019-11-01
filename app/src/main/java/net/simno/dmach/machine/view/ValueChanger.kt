package net.simno.dmach.machine.view

import android.annotation.SuppressLint
import android.content.Context
import android.os.Looper
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.core.os.HandlerCompat

abstract class ValueChanger @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    protected abstract val maxValue: Int
    protected abstract val minValue: Int

    private val mainHandler = HandlerCompat.createAsync(Looper.getMainLooper())

    var onValueChangedListener: ValueChangedListener? = null
    private var value = 0
    private var delay = 500L
    private var change = 1

    override fun hasOverlappingRendering(): Boolean = false

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> onActionDown(event)
            MotionEvent.ACTION_MOVE -> onActionMove(event)
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

    private fun onActionDown(event: MotionEvent) {
        HandlerCompat.postDelayed(mainHandler, {
            calculateChangeAndDelay(event.x, width.toFloat())
        }, null, 0)
        HandlerCompat.postDelayed(mainHandler, delayedAction {
            val newValue = value + change
            if (newValue in minValue..maxValue) {
                value = newValue
                onValueChangedListener?.onValueChanged(newValue)
            }
        }, null, 0)
    }

    private fun onActionMove(event: MotionEvent) {
        calculateChangeAndDelay(event.x, width.toFloat())
    }

    private fun onActionUpOrCancel() {
        mainHandler.removeCallbacksAndMessages(null)
    }

    private fun calculateChangeAndDelay(x: Float, width: Float) {
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

    interface ValueChangedListener {
        fun onValueChanged(value: Int)
    }

    companion object {
        private const val MAX_DELAY = 400f
        private const val MIN_DELAY = 32f
    }
}
