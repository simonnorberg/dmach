package net.simno.dmach.db

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DbModule {
    @Provides
    @Singleton
    fun providePatchDatabase(@ApplicationContext context: Context): PatchDatabase {
        return Room
            .databaseBuilder(context, PatchDatabase::class.java, PatchDatabase.NAME)
            .createFromAsset("databases/dmach.db")
            .addMigrations(PatchDatabase.MIGRATION_1_2, PatchDatabase.MIGRATION_2_3)
            .fallbackToDestructiveMigrationOnDowngrade()
            .build()
    }

    @Provides
    @Singleton
    fun providePatchRepository(patchDatabase: PatchDatabase): PatchRepository {
        return PatchRepository(patchDatabase.patchDao())
    }
}
