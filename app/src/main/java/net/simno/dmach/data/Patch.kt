package net.simno.dmach.data

data class Patch(
    val title: String,
    val sequence: List<Int>,
    val mutedChannels: Set<Int>,
    val channels: List<Channel>,
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
        val EMPTY_SEQUENCE: List<Int> = (0..31).map { 0 }
    }
}

fun Patch.mutedSequence(): List<Int> {
    return sequence.mapIndexed { index, step ->
        val offset = if (index < Patch.STEPS) 0 else Patch.MUTED_MASKS.size
        Patch.MUTED_MASKS.foldIndexed(step) { maskIndex, maskedStep, mask ->
            when {
                mutedChannels.contains(maskIndex + offset) -> maskedStep and mask
                else -> maskedStep
            }
        }
    }
}

fun Patch.withPan(pan: Pan): Patch {
    return copy(
        channels = channels.map { ch ->
            if (ch == channel) ch.copy(pan = pan) else ch
        }
    )
}

fun Patch.withSelectedSetting(selectedSetting: Int): Patch {
    return copy(
        channels = channels.map { ch ->
            if (ch == channel) ch.copy(selectedSetting = selectedSetting) else ch
        }
    )
}

fun Patch.withPosition(position: Position): Patch {
    val setting = channel.setting
    return copy(
        channels = channels.map { ch ->
            if (ch == channel) {
                ch.copy(
                    settings = ch.settings.map { s ->
                        if (s == setting) s.copy(x = position.x, y = position.y) else s
                    }
                )
            } else {
                ch
            }
        }
    )
}
