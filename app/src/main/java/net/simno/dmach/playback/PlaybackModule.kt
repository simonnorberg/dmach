package net.simno.dmach.playback

import android.content.Context
import android.content.SharedPreferences
import android.media.AudioManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object PlaybackModule {

    @Provides
    @Singleton
    fun provideAudioFocus(
        audioManager: AudioManager,
        sharedPreferences: SharedPreferences
    ): AudioFocus {
        return AudioFocus(audioManager, sharedPreferences)
    }

    @Provides
    @Singleton
    fun provideKortholtController(
        @ApplicationContext context: Context
    ): KortholtController {
        return KortholtController(context)
    }

    @Provides
    @Singleton
    fun providePlaybackServiceController(
        @ApplicationContext context: Context,
        kortholtController: KortholtController
    ): PlaybackServiceController {
        return PlaybackServiceController(context, kortholtController)
    }

    @Provides
    @Singleton
    fun providePureDataController(
        @ApplicationContext context: Context
    ): PureDataController {
        return PureDataController(context)
    }
}
