package net.simno.dmach

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.Surface
import androidx.core.view.WindowCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import net.simno.dmach.machine.MachineScreen
import net.simno.dmach.machine.MachineViewModel
import net.simno.dmach.patch.PatchScreen
import net.simno.dmach.patch.PatchViewModel
import net.simno.dmach.theme.AppTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        setContent {
            AppTheme {
                Surface {
                    val machineViewModel = hiltViewModel<MachineViewModel>()
                    machineViewModel.lifecycleObservers.forEach { lifecycle.addObserver(it) }

                    val navController = rememberNavController()
                    NavHost(
                        navController = navController,
                        startDestination = Destination.Machine.name
                    ) {
                        composable(Destination.Machine.name) {
                            MachineScreen(navController, machineViewModel)
                        }
                        composable(Destination.Patch.name) {
                            val patchViewModel = hiltViewModel<PatchViewModel>()
                            PatchScreen(navController, patchViewModel)
                        }
                    }
                }
            }
        }
    }
}
