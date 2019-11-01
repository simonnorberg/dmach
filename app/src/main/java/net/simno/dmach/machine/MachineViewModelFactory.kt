package net.simno.dmach.machine

import android.app.Application
import android.content.Context
import androidx.core.content.getSystemService
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import net.simno.dmach.DMachApp
import net.simno.dmach.playback.AudioFocus
import net.simno.dmach.playback.PlaybackObserver
import net.simno.dmach.playback.PlaybackServiceController
import net.simno.dmach.playback.PureDataController

class MachineViewModelFactory(
    private val application: Application,
    private val lifecycle: Lifecycle
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        val serviceController = PlaybackServiceController(application)
        val pureDataController = PureDataController(application)
        lifecycle.addObserver(serviceController)
        lifecycle.addObserver(pureDataController)
        val playbackObservers = setOf<PlaybackObserver>(serviceController, pureDataController)
        val audioFocus = AudioFocus.create(
            application.getSystemService()!!,
            application.getSharedPreferences("dmach", Context.MODE_PRIVATE)
        )
        val db = (application as DMachApp).db
        val processor = MachineProcessor(pureDataController, audioFocus, playbackObservers, db)
        return MachineViewModel(processor) as T
    }
}
