package net.simno.dmach

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Surface
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.LayoutDirection
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
                    Box(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        val navController = rememberNavController()
                        NavHost(
                            navController = navController,
                            startDestination = Destination.Machine,
                            enterTransition = { fadeIn(animationSpec = tween(200)) },
                            exitTransition = { fadeOut(animationSpec = tween(200)) }
                        ) {
                            composable<Destination.Machine> {
                                MachineScreen(navigateToPatch = { navController.navigate(Destination.Patch) })
                            }
                            composable<Destination.Patch> {
                                PatchScreen(navigateUp = navController::navigateUp)
                            }
                        }
                        val displayCutout = WindowInsets.displayCutout.asPaddingValues()
                        Spacer(
                            modifier = Modifier
                                .fillMaxHeight()
                                .width(displayCutout.calculateStartPadding(LayoutDirection.Ltr))
                                .background(Color.Black)
                                .align(Alignment.TopStart)
                        )
                        Spacer(
                            modifier = Modifier
                                .fillMaxHeight()
                                .width(displayCutout.calculateEndPadding(LayoutDirection.Ltr))
                                .background(Color.Black)
                                .align(Alignment.TopEnd)
                        )
                    }
                }
            }
        }
    }
}
