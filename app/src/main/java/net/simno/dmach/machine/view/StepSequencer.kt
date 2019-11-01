package net.simno.dmach.machine.view

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.annotation.Size
import androidx.core.content.ContextCompat
import androidx.core.graphics.withClip
import net.simno.dmach.R

class StepSequencer @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val margin = resources.getDimension(R.dimen.margin_small)

    var onSequenceChangedListener: OnSequenceChangedListener? = null
    private var steps = (0..31).map { 0 }.toMutableList()
    private var uncheckedLight = 0
    private var uncheckedDark = 0
    private var checked = 0
    private var actionDownIsChecked = false
    private var stepWidth = 0f
    private var stepHeight = 0f
    private var stepWidthMargin = 0f
    private var stepHeightMargin = 0f

    init {
        uncheckedLight = ContextCompat.getColor(context, R.color.khaki)
        uncheckedDark = ContextCompat.getColor(context, R.color.gurkha)
        checked = ContextCompat.getColor(context, R.color.poppy)
    }

    override fun hasOverlappingRendering(): Boolean = false

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        if (w != 0 && h != 0) {
            stepWidth = (w - (STEPS - 1f) * margin) / STEPS
            stepHeight = (h - (CHANNELS - 1f) * margin) / CHANNELS
            stepWidthMargin = stepWidth + margin
            stepHeightMargin = stepHeight + margin
            invalidate()
        }
    }

    override fun onDraw(canvas: Canvas) {
        steps.forEachIndexed { stepIndex, step ->
            MASKS.forEachIndexed { maskIndex, mask ->
                val left = (stepIndex % STEPS) * stepWidthMargin
                val right = left + stepWidth
                val top = (maskIndex + ((stepIndex / STEPS) * MASKS.size)) * stepHeightMargin
                val bottom = top + stepHeight

                canvas.withClip(left, top, right, bottom) {
                    drawColor(
                        when {
                            step and mask > 0 -> checked
                            stepIndex % 8 < 4 -> uncheckedLight
                            else -> uncheckedDark
                        }
                    )
                }
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> onActionDownOrMove(event, true)
            MotionEvent.ACTION_MOVE -> onActionDownOrMove(event, false)
        }
        return true
    }

    fun setSequence(@Size(32) sequence: List<Int>) {
        if (sequence != steps) {
            steps = sequence.toMutableList()
            invalidate()
        }
    }

    private fun onActionDownOrMove(event: MotionEvent, isActionDown: Boolean) {
        if (event.isOutsideView()) {
            return
        }

        val step = (event.x / stepWidthMargin).toInt().coerceIn(0, STEPS - 1)
        val channel = (event.y / stepHeightMargin).toInt().coerceIn(0, CHANNELS - 1)
        val mask = MASKS[channel % MASKS.size]
        val index = ((channel / MASKS.size) * STEPS) + step
        val isChecked = (steps[index] and mask) > 0

        if (isActionDown) {
            actionDownIsChecked = isChecked
        }

        if (isActionDown || (isChecked == actionDownIsChecked)) {
            steps[index] = steps[index] xor mask
            onSequenceChangedListener?.onSequenceChanged(steps)
            invalidate()
        }
    }

    private fun MotionEvent.isOutsideView() = x < 0 || y < 0 || x > width || y > height

    interface OnSequenceChangedListener {
        fun onSequenceChanged(sequence: List<Int>)
    }

    companion object {
        private const val CHANNELS = 6
        private const val STEPS = 16
        private val MASKS = intArrayOf(1, 2, 4)
    }
}
