package net.simno.dmach.machine

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import net.simno.dmach.Destination
import net.simno.dmach.machine.ui.Machine

@Composable
fun MachineScreen(
    navController: NavController,
    viewModel: MachineViewModel = hiltViewModel()
) {
    val state by viewModel.viewState.collectAsState()

    Machine(
        state = state,
        onAction = viewModel::onAction,
        onClickPatch = { navController.navigate(Destination.Patch.name) }
    )
}
