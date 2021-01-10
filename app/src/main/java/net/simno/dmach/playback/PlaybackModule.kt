package net.simno.dmach.playback

import android.content.Context
import android.content.SharedPreferences
import android.media.AudioManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import dagger.hilt.android.qualifiers.ApplicationContext

@Module
@InstallIn(ActivityComponent::class)
object PlaybackModule {
    @Provides
    fun provideAudioFocus(
        audioManager: AudioManager,
        sharedPreferences: SharedPreferences
    ): AudioFocus {
        return AudioFocus(audioManager, sharedPreferences)
    }

    @Provides
    fun providePlaybackServiceController(@ApplicationContext context: Context): PlaybackServiceController {
        return PlaybackServiceController(context)
    }

    @Provides
    fun providePureDataController(@ApplicationContext context: Context): PureDataController {
        return PureDataController(context)
    }
}
