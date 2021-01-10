package net.simno.dmach.patch

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import net.simno.dmach.db.PatchRepository

@Module
@InstallIn(ActivityComponent::class)
object PatchModule {
    @Provides
    fun providePatchProcesssor(patchRepository: PatchRepository): PatchProcessor {
        return PatchProcessor(patchRepository)
    }
}
