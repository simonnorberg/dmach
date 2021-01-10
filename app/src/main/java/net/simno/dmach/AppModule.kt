package net.simno.dmach

import android.content.Context
import android.content.SharedPreferences
import android.media.AudioManager
import androidx.core.content.getSystemService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    fun provideAudioManager(@ApplicationContext context: Context): AudioManager {
        return context.getSystemService()!!
    }

    @Provides
    fun provideSharedPrerences(@ApplicationContext context: Context): SharedPreferences {
        return context.getSharedPreferences("dmach", Context.MODE_PRIVATE)
    }
}
