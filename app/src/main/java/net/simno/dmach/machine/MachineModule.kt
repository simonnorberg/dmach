package net.simno.dmach.machine

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import net.simno.dmach.db.PatchRepository
import net.simno.dmach.playback.AudioFocus
import net.simno.dmach.playback.PlaybackServiceController
import net.simno.dmach.playback.PureDataController

@Module
@InstallIn(ViewModelComponent::class)
object MachineModule {
    @Provides
    fun provideMachineProcesssor(
        playbackServiceController: PlaybackServiceController,
        pureDataController: PureDataController,
        audioFocus: AudioFocus,
        patchRepository: PatchRepository
    ): MachineProcessor {
        return MachineProcessor(
            setOf(playbackServiceController, pureDataController),
            pureDataController,
            audioFocus,
            patchRepository
        )
    }
}
