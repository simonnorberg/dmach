package net.simno.dmach.data

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
data class Channel(
    val name: String,
    val settings: List<Setting>,
    val selectedSetting: Int,
    val pan: Pan
) {
    @Transient
    val setting: Setting = settings.getOrElse(selectedSetting) { Setting.EMPTY }

    companion object {
        const val NONE_ID = -1

        val NONE: Channel = Channel("none", emptyList(), 0, Pan(0.5f))
    }
}
