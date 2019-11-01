package net.simno.dmach.machine.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.core.content.ContextCompat
import net.simno.dmach.R
import kotlin.math.pow

class GravityFader @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : Positioner(context, attrs, defStyleAttr) {

    private val rectHeight = resources.getDimension(R.dimen.rect_height)
    private val offset = shapeStrokeWidth / 2f + rectHeight / 2f

    override val minX = shapeStrokeWidth / 2f
    override val minY = shapeStrokeWidth / 2f + offset

    private val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        strokeWidth = shapeStrokeWidth
        style = Paint.Style.FILL_AND_STROKE
        strokeCap = Paint.Cap.ROUND
    }

    private var lines: FloatArray = floatArrayOf()

    init {
        paint.color = ContextCompat.getColor(context, R.color.gamboge)
        paint.alpha = 239
        linePaint.color = ContextCompat.getColor(context, R.color.gamboge)
        linePaint.alpha = 127
        setPosition(0f, 0f)
    }

    override fun onDraw(canvas: Canvas) {
        canvas.drawLines(lines, linePaint)
        canvas.drawRect(minX, shapeY - offset, getMaxX(), shapeY - offset + rectHeight, paint)
    }

    override fun onActionDown(event: MotionEvent) {
        updatePosition(event.x, event.y, beforeInvalidate = { createLines() })
    }

    override fun onActionMove(event: MotionEvent, pointerId: Int) {
        updatePosition(event.x, event.y, beforeInvalidate = { createLines() })
    }

    override fun getMaxX() = width - minX

    override fun getMaxY() = height - minY + shapeStrokeWidth

    private fun createLines() {
        val startY = shapeY + (offset / 2)
        val maxY = getMaxY() + offset
        val lineSpacing = (offset / 2.5f) * (2 - (startY / maxY))

        val centerX = (getMaxX() - minX + shapeStrokeWidth) / 2f
        val length = getMaxX() - centerX

        var lineCount = 1
        var lineY = startY

        lines = floatArrayOf()
        while (lineY < maxY) {
            lineY = startY + shapeStrokeWidth + lineSpacing * lineCount

            val a = lineY - startY.toDouble()
            val b = 1 - (a / (maxY - startY))
            val lineMultiplier = ((a.pow(b) - 1) / (a - 1)).toFloat()
            val lineLength = (length / 8f) + ((length / 1.1f) * lineMultiplier)

            val lineStart = centerX + lineLength
            val lineEnd = centerX - lineLength

            lines += floatArrayOf(lineStart, lineY, lineEnd, lineY)
            lineCount++
        }
    }
}
