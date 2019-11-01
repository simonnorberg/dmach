package net.simno.dmach.db

import kotlinx.serialization.UnstableDefault
import kotlinx.serialization.json.Json
import kotlinx.serialization.list
import kotlinx.serialization.serializer
import net.simno.dmach.data.Channel
import net.simno.dmach.data.Patch

@UseExperimental(UnstableDefault::class)
object EntityToPatch :
    androidx.arch.core.util.Function<PatchEntity, Patch>,
    io.reactivex.functions.Function<PatchEntity, Patch> {
    override fun apply(entity: PatchEntity): Patch = Patch(
        title = entity.title,
        sequence = Json.parse(Int.serializer().list, entity.sequence),
        channels = Json.parse(Channel.serializer().list, entity.channels),
        selectedChannel = entity.selected,
        tempo = entity.tempo,
        swing = entity.swing
    )
}
