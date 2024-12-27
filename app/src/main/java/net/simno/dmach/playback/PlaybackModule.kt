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
    ): AudioManager = context.getSystemService()!!

    @Provides
    @Singleton
    fun provideAudioFocus(
        audioManager: AudioManager,
        settingsRepository: SettingsRepository
    ): AudioFocus = AudioFocus(audioManager, settingsRepository)

    @Provides
    @Singleton
    fun provideKortholtPlayer(
        @ApplicationContext context: Context
    ): Kortholt.Player = Kortholt.Player.Builder(context)
        .build()
        .also { Kortholt.setPlayer(it) }

    @Provides
    @Singleton
    fun provideWaveExporter(
        @ApplicationContext context: Context,
        kortholt: Kortholt.Player
    ): WaveExporter = WaveExporter(context, kortholt)

    @Provides
    @Singleton
    fun providePureData(
        kortholt: Kortholt.Player
    ): PureData = PureData(kortholt)

    @Provides
    @Singleton
    fun providePlaybackController(
        @ApplicationContext context: Context,
        kortholt: Kortholt.Player,
        pureData: PureData,
        waveExporter: WaveExporter
    ): PlaybackController = PlaybackController(context, kortholt, pureData, waveExporter)
}
