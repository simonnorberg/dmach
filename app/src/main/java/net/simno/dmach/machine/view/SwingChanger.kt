package net.simno.dmach.machine.view

import android.content.Context
import android.util.AttributeSet

class SwingChanger @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ValueChanger(context, attrs, defStyleAttr) {
    override val maxValue = 50
    override val minValue = 0
}
