package net.simno.dmach.patch

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.compose.collectAsLazyPagingItems
import net.simno.dmach.patch.ui.Patch

@Composable
fun PatchScreen(
    navigateUp: () -> Unit,
    viewModel: PatchViewModel = hiltViewModel()
) {
    val currentNavigateUp by rememberUpdatedState(navigateUp)
    val state by viewModel.viewState.collectAsStateWithLifecycle()
    val patches = viewModel.patches.collectAsLazyPagingItems()

    LaunchedEffect(state.finish) {
        if (state.finish) {
            currentNavigateUp()
        }
    }
    Patch(
        state = state,
        patches = patches,
        onAction = viewModel::onAction
    )
}
