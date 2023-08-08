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
import net.simno.kortholt.Kortholt

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
    fun provideKortholtPlayer(
        @ApplicationContext context: Context
    ): Kortholt.Player {
        return Kortholt.Player.Builder(context)
            .build()
            .also { Kortholt.setPlayer(it) }
    }

    @Provides
    @Singleton
    fun provideWaveExporter(
        @ApplicationContext context: Context,
        kortholt: Kortholt.Player
    ): WaveExporter {
        return WaveExporter(context, kortholt)
    }

    @Provides
    @Singleton
    fun providePureData(
        kortholt: Kortholt.Player
    ): PureData {
        return PureData(kortholt)
    }

    @Provides
    @Singleton
    fun providePlaybackController(
        @ApplicationContext context: Context,
        kortholt: Kortholt.Player,
        pureData: PureData,
        waveExporter: WaveExporter
    ): PlaybackController {
        return PlaybackController(context, kortholt, pureData, waveExporter)
    }
}
