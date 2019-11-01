package net.simno.dmach.data

import kotlin.random.Random

data class Patch(
    val title: String,
    val sequence: List<Int>,
    val channels: List<Channel>,
    val selectedChannel: Int,
    val tempo: Int,
    val swing: Int
) {
    val channel: Channel = channels.getOrElse(selectedChannel) { Channel.NONE }

    companion object {
        const val STEPS = 16
        val EMPTY_SEQUENCE: List<Int> = (0..31).map { 0 }
        val RANDOM_SEQUENCE: List<Int>
            get() = (0..31)
                .map { Random.nextInt(12) }
                .map { if (it < 8) it else 0 }
    }
}

fun Patch.withPan(pan: Float): Patch {
    return copy(channels = channels.map {
        if (it == channel) it.copy(pan = pan) else it
    })
}

fun Patch.withSelectedSetting(selectedSetting: Int): Patch {
    return copy(channels = channels.map {
        if (it == channel) it.copy(selectedSetting = selectedSetting) else it
    })
}

fun Patch.withPosition(position: Position): Patch {
    val setting = channel.setting
    return copy(channels = channels.map { ch ->
        if (ch == channel) {
            ch.copy(settings = ch.settings.map { s ->
                if (s == setting) s.copy(x = position.x, y = position.y) else s
            })
        } else {
            ch
        }
    })
}
