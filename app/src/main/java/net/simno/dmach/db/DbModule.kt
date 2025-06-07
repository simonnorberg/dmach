package net.simno.dmach.db

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import kotlinx.coroutines.runBlocking
import net.simno.dmach.data.defaultPatch
import net.simno.dmach.db.PatchRepository.Companion.toEntity

@Module
@InstallIn(SingletonComponent::class)
object DbModule {
    @Provides
    @Singleton
    fun providePatchDatabase(@ApplicationContext context: Context): PatchDatabase {
        val db = Room
            .databaseBuilder(context, PatchDatabase::class.java, PatchDatabase.NAME)
            .addMigrations(
                PatchDatabase.MIGRATION_2_3,
                PatchDatabase.MIGRATION_3_4,
                PatchDatabase.MIGRATION_4_5
            )
            .fallbackToDestructiveMigration(dropAllTables = true)
            .build()

        val dao = db.patchDao()
        runBlocking {
            if (dao.count() == 0) {
                val defaultPatch = defaultPatch()
                dao.insertPatch(defaultPatch.toEntity(defaultPatch.title))
            }
        }

        return db
    }

    @Provides
    @Singleton
    fun providePatchRepository(
        patchDatabase: PatchDatabase
    ): PatchRepository = PatchRepository(patchDatabase.patchDao())
}
