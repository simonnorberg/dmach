package net.simno.dmach.patch

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import net.simno.dmach.DMachApp

class PatchViewModelFactory(
    private val application: Application
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        val processor = PatchProcessor((application as DMachApp).db)
        return PatchViewModel(processor) as T
    }
}
