package net.simno.dmach.patch

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.scopes.ViewModelScoped
import net.simno.dmach.db.PatchRepository
import net.simno.dmach.patch.state.PatchProcessor

@Module
@InstallIn(ViewModelComponent::class)
object PatchModule {
    @Provides
    @ViewModelScoped
    fun providePatchProcesssor(patchRepository: PatchRepository): PatchProcessor {
        return PatchProcessor(patchRepository)
    }
}
