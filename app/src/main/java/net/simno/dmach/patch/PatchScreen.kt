package net.simno.dmach.patch

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.paging.compose.collectAsLazyPagingItems
import net.simno.dmach.patch.ui.Patch

@Composable
fun PatchScreen(
    navController: NavController,
    viewModel: PatchViewModel = hiltViewModel()
) {
    val state by viewModel.viewState.collectAsStateWithLifecycle()
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
