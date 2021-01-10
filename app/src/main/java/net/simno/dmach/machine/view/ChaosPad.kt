package net.simno.dmach.machine.view

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.VelocityTracker
import androidx.core.content.ContextCompat
import androidx.dynamicanimation.animation.DynamicAnimation
import androidx.dynamicanimation.animation.DynamicAnimation.OnAnimationEndListener
import androidx.dynamicanimation.animation.DynamicAnimation.OnAnimationUpdateListener
import androidx.dynamicanimation.animation.FlingAnimation
import androidx.dynamicanimation.animation.FloatValueHolder
import net.simno.dmach.R
import kotlin.math.absoluteValue

class ChaosPad @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : Positioner(context, attrs, defStyleAttr) {

    private val xAnimationUpdate = OnAnimationUpdateListener { _, newX, _ ->
        updatePosition(newX, shapeY)
    }
    private val yAnimationUpdate = OnAnimationUpdateListener { _, newY, _ ->
        updatePosition(shapeX, newY)
    }
    private val xAnimationEnd = OnAnimationEndListener { _, canceled, _, velocity ->
        if (!canceled && velocity.absoluteValue > 0 && isAttachedToWindow) {
            xVelocity = -velocity
            startXAnimation()
        }
    }
    private val yAnimationEnd = OnAnimationEndListener { _, canceled, _, velocity ->
        if (!canceled && velocity.absoluteValue > 0 && isAttachedToWindow) {
            yVelocity = -velocity
            startYAnimation()
        }
    }

    private val circleRadius = resources.getDimension(R.dimen.circle_radius)

    override val minX = shapeStrokeWidth / 2f + circleRadius
    override val minY = shapeStrokeWidth / 2f + circleRadius

    private var friction = MAX_FRICTION
        set(value) {
            field = value
            if (value == MAX_FRICTION) {
                stopAnimations()
            }
        }

    private var xVelocity = 0f
    private var yVelocity = 0f
    private var velocityTracker: VelocityTracker? = null
    private var xFling: FlingAnimation? = null
    private var yFling: FlingAnimation? = null

    init {
        paint.color = ContextCompat.getColor(context, R.color.colonial_alpha)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        stopAnimations()
    }

    override fun onDraw(canvas: Canvas) {
        canvas.drawCircle(shapeX, shapeY, circleRadius, paint)
    }

    override fun onActionDown(event: MotionEvent) {
        stopAnimations()
        if (velocityTracker == null) {
            velocityTracker = VelocityTracker.obtain()
        } else {
            velocityTracker?.clear()
        }
        velocityTracker?.addMovement(event)

        updatePosition(event.x, event.y)
    }

    override fun onActionMove(event: MotionEvent, pointerId: Int) {
        if (friction < MAX_FRICTION) {
            velocityTracker?.let {
                it.addMovement(event)
                it.computeCurrentVelocity(500, 10000f)
                xVelocity = it.getXVelocity(pointerId)
                yVelocity = it.getYVelocity(pointerId)
            }
        }

        updatePosition(event.x, event.y)
    }

    override fun onActionUp() {
        velocityTracker?.recycle()
        velocityTracker = null
        if (friction < MAX_FRICTION) {
            startXAnimation()
            startYAnimation()
        }
    }

    override fun onActionCancel() {
        velocityTracker?.recycle()
        velocityTracker = null
    }

    override fun getMaxX() = width - minX

    override fun getMaxY() = height - minY

    fun setGravity(gravity: Float) {
        friction = if (gravity > 0.99f) {
            MAX_FRICTION
        } else {
            (gravity * 3).coerceIn(0.01f, 3f)
        }
    }

    private fun stopAnimations() {
        xFling?.cancel()
        xFling = null
        yFling?.cancel()
        yFling = null
    }

    private fun startXAnimation() {
        xFling = createAnimation(shapeX, xVelocity, getMaxX(), minX).apply {
            addUpdateListener(xAnimationUpdate)
            addEndListener(xAnimationEnd)
            start()
        }
    }

    private fun startYAnimation() {
        yFling = createAnimation(shapeY, yVelocity, getMaxY(), minY).apply {
            addUpdateListener(yAnimationUpdate)
            addEndListener(yAnimationEnd)
            start()
        }
    }

    private fun createAnimation(
        startValue: Float,
        startVelocity: Float,
        maxValue: Float,
        minValue: Float
    ): FlingAnimation {
        return FlingAnimation(FloatValueHolder(startValue))
            .setStartVelocity(startVelocity)
            .setMaxValue(maxValue)
            .setMinValue(minValue)
            .setMinimumVisibleChange(DynamicAnimation.MIN_VISIBLE_CHANGE_PIXELS)
            .setFriction(friction)
    }

    companion object {
        const val MAX_FRICTION = Float.MAX_VALUE
    }
}
