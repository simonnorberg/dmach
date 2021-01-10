package net.simno.dmach.machine

import android.content.Context
import androidx.activity.ComponentActivity
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import dagger.hilt.android.qualifiers.ActivityContext
import net.simno.dmach.db.PatchRepository
import net.simno.dmach.playback.AudioFocus
import net.simno.dmach.playback.PlaybackServiceController
import net.simno.dmach.playback.PureDataController

@Module
@InstallIn(ActivityComponent::class)
object MachineModule {
    @Provides
    fun provideMachineProcesssor(
        @ActivityContext context: Context,
        playbackServiceController: PlaybackServiceController,
        pureDataController: PureDataController,
        audioFocus: AudioFocus,
        patchRepository: PatchRepository
    ): MachineProcessor {
        (context as ComponentActivity).lifecycle.apply {
            addObserver(playbackServiceController)
            addObserver(pureDataController)
        }
        return MachineProcessor(
            pureDataController,
            audioFocus,
            setOf(playbackServiceController, pureDataController),
            patchRepository
        )
    }
}
