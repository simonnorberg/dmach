package net.simno.dmach.machine

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.scopes.ViewModelScoped
import net.simno.dmach.db.PatchRepository
import net.simno.dmach.machine.state.MachineProcessor
import net.simno.dmach.playback.AudioFocus
import net.simno.dmach.playback.PlaybackController
import net.simno.dmach.playback.PureData
import net.simno.dmach.playback.WaveExporter
import net.simno.dmach.settings.SettingsRepository

@Module
@InstallIn(ViewModelComponent::class)
object MachineModule {
    @Provides
    @ViewModelScoped
    fun provideMachineProcesssor(
        playbackController: PlaybackController,
        pureData: PureData,
        waveExporter: WaveExporter,
        audioFocus: AudioFocus,
        patchRepository: PatchRepository,
        settingsRepository: SettingsRepository
    ): MachineProcessor {
        return MachineProcessor(
            playbackController,
            pureData,
            waveExporter,
            audioFocus,
            patchRepository,
            settingsRepository
        )
    }
}
