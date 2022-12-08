package net.simno.dmach.machine

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.scopes.ViewModelScoped
import net.simno.dmach.db.PatchRepository
import net.simno.dmach.machine.state.MachineProcessor
import net.simno.dmach.playback.AudioFocus
import net.simno.dmach.playback.KortholtController
import net.simno.dmach.playback.PlaybackServiceController
import net.simno.dmach.playback.PureDataController

@Module
@InstallIn(ViewModelComponent::class)
object MachineModule {
    @Provides
    @ViewModelScoped
    fun provideMachineProcesssor(
        playbackServiceController: PlaybackServiceController,
        pureDataController: PureDataController,
        kortholtController: KortholtController,
        audioFocus: AudioFocus,
        patchRepository: PatchRepository
    ): MachineProcessor {
        return MachineProcessor(
            setOf(playbackServiceController, pureDataController),
            pureDataController,
            kortholtController,
            audioFocus,
            patchRepository
        )
    }
}
