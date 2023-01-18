package net.simno.dmach.playback

import android.content.Context
import android.media.AudioManager
import androidx.core.content.getSystemService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import net.simno.dmach.settings.SettingsRepository

@Module
@InstallIn(SingletonComponent::class)
object PlaybackModule {

    @Provides
    fun provideAudioManager(
        @ApplicationContext context: Context
    ): AudioManager {
        return context.getSystemService()!!
    }

    @Provides
    @Singleton
    fun provideAudioFocus(
        audioManager: AudioManager,
        settingsRepository: SettingsRepository
    ): AudioFocus {
        return AudioFocus(audioManager, settingsRepository)
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
