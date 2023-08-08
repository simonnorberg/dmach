package net.simno.dmach.machine.state

import kotlin.random.Random
import net.simno.dmach.data.Patch

interface Randomizer {
    fun nextSequence(): List<Int>
    fun nextFloat(): Float
    fun nextInt(): Int

    companion object {
        val DEFAULT = object : Randomizer {
            override fun nextSequence(): List<Int> = Patch.EMPTY_SEQUENCE
                .map { Random.nextInt(12) }
                .map { if (it < 8) it else 0 }

            override fun nextFloat(): Float = Random.nextDouble(0.0, 1.0).toFloat()

            override fun nextInt(): Int = Random.nextInt()
        }
    }
}
