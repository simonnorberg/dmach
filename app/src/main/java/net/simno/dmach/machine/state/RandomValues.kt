package net.simno.dmach.machine.state

import kotlin.random.Random
import net.simno.dmach.data.Patch

fun interface RandomSequence {
    fun next(): List<Int>

    companion object {
        val DEFAULT = RandomSequence {
            Patch.EMPTY_SEQUENCE
                .map { Random.nextInt(12) }
                .map { if (it < 8) it else 0 }
        }
    }
}

fun interface RandomFloat {
    fun next(): Float

    companion object {
        val DEFAULT = RandomFloat { Random.nextDouble(0.0, 1.0).toFloat() }
    }
}

fun interface RandomInt {
    fun next(): Int

    companion object {
        val DEFAULT = RandomInt { Random.nextInt() }
    }
}
