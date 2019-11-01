package net.simno.dmach.db

import io.reactivex.functions.BiFunction
import kotlinx.serialization.UnstableDefault
import kotlinx.serialization.json.Json
import kotlinx.serialization.list
import kotlinx.serialization.serializer
import net.simno.dmach.data.Channel
import net.simno.dmach.data.Patch

@UseExperimental(UnstableDefault::class)
object PatchToEntity : BiFunction<Patch, String, PatchEntity> {
    override fun apply(patch: Patch, title: String): PatchEntity = PatchEntity(
        _id = null,
        title = title,
        sequence = Json.stringify(Int.serializer().list, patch.sequence),
        channels = Json.stringify(Channel.serializer().list, patch.channels),
        selected = patch.selectedChannel,
        tempo = patch.tempo,
        swing = patch.swing,
        active = true
    )
}
