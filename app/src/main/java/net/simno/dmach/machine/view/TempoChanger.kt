package net.simno.dmach.machine.view

import android.content.Context
import android.util.AttributeSet

class TempoChanger @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ValueChanger(context, attrs, defStyleAttr) {
    override val maxValue = 1000
    override val minValue = 1

    init {
        setValue(120)
    }
}
