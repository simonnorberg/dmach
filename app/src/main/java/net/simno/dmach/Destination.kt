package net.simno.dmach

import kotlinx.serialization.Serializable

@Serializable
sealed class Destination {
    @Serializable
    data object Machine : Destination()

    @Serializable
    data object Patch : Destination()
}
