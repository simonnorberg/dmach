package net.simno.dmach.machine.view

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Paint
import android.graphics.PointF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import net.simno.dmach.R

abstract class Positioner @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    var onPositionChangedListener: OnPositionChangedListener? = null

    protected abstract val minX: Float
    protected abstract val minY: Float
    protected val shapeStrokeWidth = resources.getDimension(R.dimen.shape_stroke_width)
    protected val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = shapeStrokeWidth
    }

    protected var shapeX = 0f
    protected var shapeY = 0f
    private var lastSetPosition: PointF? = null

    override fun hasOverlappingRendering(): Boolean = true

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        if (w != oldw || h != oldh) {
            lastSetPosition?.let { setPosition(it) }
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
        setPosition(PointF(x, y))
    }

    private fun setPosition(position: PointF) {
        // Save the position if we call setPosition before onSizeChanged has been called.
        lastSetPosition = position

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
        onPositionChangedListener?.onPositionChanged(PointF(posX, posY))
    }

    interface OnPositionChangedListener {
        fun onPositionChanged(point: PointF)
    }
}
