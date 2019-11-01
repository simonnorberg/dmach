package net.simno.dmach.data

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
data class Setting(
    val hText: String,
    val vText: String,
    val hIndex: Int,
    val vIndex: Int,
    val x: Float,
    val y: Float
) {
    @Transient
    val position: Position = Position(x, y)

    companion object {
        val EMPTY: Setting = Setting("", "", 0, 0, 0.5f, 0.5f)
    }
}
