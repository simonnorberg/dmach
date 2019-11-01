package net.simno.dmach.machine.view

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.animation.DecelerateInterpolator
import androidx.core.content.ContextCompat
import net.simno.dmach.R

class PanFader @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : Positioner(context, attrs, defStyleAttr) {

    private val rectHeight = resources.getDimension(R.dimen.rect_height)
    private val offset = shapeStrokeWidth / 2f + rectHeight / 2f

    override val minX = shapeStrokeWidth / 2f
    override val minY = shapeStrokeWidth / 2f + offset

    private var centerAnimator: ValueAnimator? = null
    private var isCentered = true
    private var center = 0f
    private var left = 0f
    private var right = 0f

    init {
        paint.color = ContextCompat.getColor(context, R.color.gamboge)
        paint.alpha = 239
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        if (h > 0) {
            center = height / 2f
            left = center + (offset / 2f)
            right = center - (offset / 2f)
        }
        super.onSizeChanged(w, h, oldw, oldh)
    }

    override fun onDraw(canvas: Canvas) {
        canvas.drawRect(minX, shapeY - offset, getMaxX(), shapeY - offset + rectHeight, paint)
    }

    override fun onActionDown(event: MotionEvent) {
        onActionDownOrMove(event)
    }

    override fun onActionMove(event: MotionEvent, pointerId: Int) {
        onActionDownOrMove(event)
    }

    override fun getMaxX() = width - minX

    override fun getMaxY() = height - minY + shapeStrokeWidth

    private fun isCenter(value: Float) = value < left && value > right

    private fun onActionDownOrMove(event: MotionEvent) {
        if (isCenter(event.y)) {
            if (!isCentered) {
                isCentered = true
                animateToCenter(event, center)
            }
        } else {
            isCentered = false
            updatePosition(event.x, event.y)
        }
    }

    private fun animateToCenter(event: MotionEvent, center: Float) {
        centerAnimator?.cancel()
        centerAnimator = ValueAnimator.ofFloat(event.y, center).apply {
            duration = 70L
            interpolator = DecelerateInterpolator()
            addUpdateListener { animation ->
                (animation.animatedValue as Float).let {
                    updatePosition(event.x, it, notifyCenterY = it == center)
                }
            }
            start()
        }
    }
}
