package net.simno.dmach.patch

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavController
import androidx.paging.compose.collectAsLazyPagingItems
import net.simno.dmach.patch.ui.Patch

@Composable
fun PatchScreen(
    navController: NavController,
    viewModel: PatchViewModel
) {
    val state by viewModel.viewState.collectAsState()
    val patches = viewModel.patches.collectAsLazyPagingItems()

    LaunchedEffect(state.finish) {
        if (state.finish) {
            navController.navigateUp()
        }
    }
    Patch(
        state = state,
        patches = patches,
        onAction = viewModel::onAction
    )
}
