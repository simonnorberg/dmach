package net.simno.dmach.data

import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.PersistentSet
import kotlinx.collections.immutable.toPersistentList

data class Patch(
    val title: String,
    val sequence: PersistentList<Int>,
    val mutedChannels: PersistentSet<Int>,
    val channels: PersistentList<Channel>,
    val selectedChannel: Int,
    val tempo: Tempo,
    val swing: Swing,
    val steps: Steps
) {
    val channel: Channel = channels.getOrElse(selectedChannel) { Channel.NONE }

    companion object {
        const val STEPS = 16
        const val CHANNELS = 6
        val MASKS = intArrayOf(1, 2, 4)
        val MUTED_MASKS = MASKS.map { 7 - it }.toIntArray()
        val EMPTY_SEQUENCE = (0..31).map { 0 }.toPersistentList()
    }
}

fun Patch.mutedSequence(): List<Int> = sequence.mapIndexed { index, step ->
    val offset = if (index < Patch.STEPS) 0 else Patch.MUTED_MASKS.size
    Patch.MUTED_MASKS.foldIndexed(step) { maskIndex, maskedStep, mask ->
        when {
            mutedChannels.contains(maskIndex + offset) -> maskedStep and mask
            else -> maskedStep
        }
    }
}

fun Patch.withPan(pan: Pan): Patch = copy(
    channels = channels.map { ch ->
        if (ch == channel) ch.copy(pan = pan) else ch
    }.toPersistentList()
)

fun Patch.withSelectedSetting(selectedSetting: Int): Patch = copy(
    channels = channels.map { ch ->
        if (ch == channel) ch.copy(selectedSetting = selectedSetting) else ch
    }.toPersistentList()
)

fun Patch.withPosition(position: Position): Patch {
    val setting = channel.setting
    return copy(
        channels = channels.map { ch ->
            if (ch == channel) {
                ch.copy(
                    settings = ch.settings.map { s ->
                        if (s == setting) s.copy(x = position.x, y = position.y) else s
                    }.toPersistentList()
                )
            } else {
                ch
            }
        }.toPersistentList()
    )
}
