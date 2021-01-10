package net.simno.dmach.machine.view

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Paint
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import net.simno.dmach.R
import net.simno.dmach.data.Position

abstract class Positioner @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    protected abstract val minX: Float
    protected abstract val minY: Float
    protected val shapeStrokeWidth = resources.getDimension(R.dimen.margin_small)
    protected val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = shapeStrokeWidth
    }

    private val _positions = MutableSharedFlow<Position>(
        replay = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val positions: SharedFlow<Position> = _positions.asSharedFlow()

    protected var shapeX = 0f
    protected var shapeY = 0f

    override fun hasOverlappingRendering(): Boolean = true

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        if (w != oldw || h != oldh) {
            _positions.replayCache.firstOrNull()?.let { setPosition(it) }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        val index = event.actionIndex
        val action = event.actionMasked
        val pointerId = event.getPointerId(index)

        when (action) {
            MotionEvent.ACTION_DOWN -> onActionDown(event)
            MotionEvent.ACTION_MOVE -> onActionMove(event, pointerId)
            MotionEvent.ACTION_UP -> onActionUp()
            MotionEvent.ACTION_CANCEL -> onActionCancel()
        }

        return true
    }

    fun setPosition(x: Float = 0f, y: Float = 0f) {
        setPosition(Position(x, y))
    }

    private fun setPosition(position: Position) {
        // Save the position if we call setPosition before onSizeChanged has been called.
        _positions.tryEmit(position)

        // Don't set shape position if layout is not finished.
        if (width == 0 && height == 0) {
            return
        }

        // Convert position value [0.0-1.0] to pixels.
        val newX = if (position.x == 0.5f) {
            width / 2f
        } else {
            position.x * (getMaxX() - minX) + minX
        }
        val newY = if (position.y == 0.5f) {
            height / 2f
        } else {
            (1 - position.y) * (getMaxY() - minY) + minY
        }
        updatePosition(newX, newY, false)
    }

    protected open fun onActionDown(event: MotionEvent) {
    }

    protected open fun onActionMove(event: MotionEvent, pointerId: Int) {
    }

    protected open fun onActionUp() {
    }

    protected open fun onActionCancel() {
    }

    protected abstract fun getMaxX(): Float

    protected abstract fun getMaxY(): Float

    protected fun updatePosition(
        newX: Float,
        newY: Float,
        notify: Boolean = true,
        notifyCenterY: Boolean = false,
        beforeInvalidate: () -> Unit = {}
    ) {
        // Adjust the new coordinates so that the Shape is always drawn inside the view.
        val validX = newX.coerceIn(minX, getMaxX())
        val validY = newY.coerceIn(minY, getMaxY())

        if (validX == shapeX && validY == shapeY) {
            return
        }

        shapeX = validX
        shapeY = validY

        if (notify) {
            notifyPositionChanged(notifyCenterY)
        }

        beforeInvalidate()

        invalidate()
    }

    private fun notifyPositionChanged(notifyCenterY: Boolean) {
        // Convert pixels to a position value [0.0-1.0]
        val posX = ((shapeX - minX) / (getMaxX() - minX)).coerceIn(0f, 1f)
        val posY = if (notifyCenterY) {
            // Pixel conversion is not exact. Set Y to 0.5 if we know it is centered.
            0.5f
        } else {
            1 - ((shapeY - minY) / (getMaxY() - minY)).coerceIn(0f, 1f)
        }
        _positions.tryEmit(Position(posX, posY))
    }
}
