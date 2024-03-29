package net.simno.dmach

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.Surface
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import net.simno.dmach.machine.MachineScreen
import net.simno.dmach.patch.PatchScreen
import net.simno.dmach.playback.PlaybackController
import net.simno.dmach.theme.AppTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var playbackController: PlaybackController

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        lifecycle.addObserver(playbackController)

        setContent {
            AppTheme {
                Surface {
                    val navController = rememberNavController()
                    NavHost(
                        navController = navController,
                        startDestination = Destination.Machine.name
                    ) {
                        composable(Destination.Machine.name) {
                            MachineScreen(navController)
                        }
                        composable(Destination.Patch.name) {
                            PatchScreen(navController)
                        }
                    }
                }
            }
        }
    }
}
