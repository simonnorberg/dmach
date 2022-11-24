package net.simno.dmach.patch

import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.navigation.NavController
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemsIndexed
import net.simno.dmach.R
import net.simno.dmach.core.DarkMediumText
import net.simno.dmach.core.TextButton
import net.simno.dmach.data.Patch
import net.simno.dmach.theme.AppTheme

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

@Composable
private fun Patch(
    state: ViewState,
    patches: LazyPagingItems<Patch>,
    onAction: (Action) -> Unit
) {
    val onSurface = MaterialTheme.colorScheme.onSurface
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant
    val paddingLarge = AppTheme.dimens.PaddingLarge
    val paddingSmall = AppTheme.dimens.PaddingSmall
    val buttonLarge = AppTheme.dimens.ButtonLarge
    Column(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding()
            .navigationBarsPadding()
            .safeDrawingPadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
        ) {
            if (state.title.isNotEmpty()) {
                val keyboardController = LocalSoftwareKeyboardController.current
                var title by remember { mutableStateOf(state.title) }
                BasicTextField(
                    value = title,
                    onValueChange = {
                        title = it.take(50)
                    },
                    modifier = Modifier
                        .weight(1f)
                        .align(Alignment.CenterVertically)
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .padding(paddingLarge),
                    textStyle = MaterialTheme.typography.bodyMedium.copy(
                        color = MaterialTheme.colorScheme.primary
                    ),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions.Default.copy(
                        capitalization = KeyboardCapitalization.None,
                        autoCorrect = false,
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            keyboardController?.hide()
                            if (title.isNotBlank()) {
                                onAction(SavePatchAction(title))
                            }
                        }
                    )
                )
                TextButton(
                    text = stringResource(R.string.patch_save),
                    selected = false,
                    modifier = Modifier
                        .wrapContentSize()
                        .padding(paddingLarge),
                    textPadding = PaddingValues(paddingLarge),
                    onClick = {
                        keyboardController?.hide()
                        if (title.isNotBlank()) {
                            onAction(SavePatchAction(title))
                        }
                    }
                )
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = paddingLarge, vertical = paddingSmall)
        ) {
            DarkMediumText(
                text = stringResource(R.string.patch_name).uppercase(),
                modifier = Modifier.weight(1f)
            )
            DarkMediumText(
                text = stringResource(R.string.patch_swing).uppercase(),
                modifier = Modifier.defaultMinSize(minWidth = buttonLarge),
                textAlign = TextAlign.End
            )
            DarkMediumText(
                text = stringResource(R.string.patch_bpm).uppercase(),
                modifier = Modifier.defaultMinSize(minWidth = buttonLarge),
                textAlign = TextAlign.End
            )
        }
        LazyColumn {
            itemsIndexed(patches) { index, patch ->
                if (patch != null) {
                    val background = when {
                        index % 2 == 0 -> onSurface
                        else -> onSurfaceVariant
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(background)
                            .combinedClickable(
                                onClick = {
                                    onAction(SelectPatchAction(patch.title))
                                },
                                onLongClick = {
                                    if (patches.itemCount > 1) {
                                        onAction(DeletePatchAction(patch.title))
                                    }
                                }
                            )
                            .padding(paddingLarge)
                    ) {
                        DarkMediumText(
                            text = patch.title,
                            modifier = Modifier.weight(1f)
                        )
                        DarkMediumText(
                            text = patch.swing.toString(),
                            modifier = Modifier.defaultMinSize(minWidth = buttonLarge),
                            textAlign = TextAlign.End
                        )
                        DarkMediumText(
                            text = patch.tempo.toString(),
                            modifier = Modifier.defaultMinSize(minWidth = buttonLarge),
                            textAlign = TextAlign.End
                        )
                    }
                }
            }
        }
    }
    when {
        state.showDelete -> {
            PatchDialog(
                text = stringResource(R.string.delete_patch, state.title),
                confirmText = R.string.delete,
                onDismiss = { onAction(DismissAction) },
                onConfirm = { onAction(ConfirmDeleteAction) }
            )
        }
        state.showOverwrite -> {
            PatchDialog(
                text = stringResource(R.string.overwrite_patch, state.title),
                confirmText = R.string.overwrite,
                onDismiss = { onAction(DismissAction) },
                onConfirm = { onAction(ConfirmOverwriteAction) }
            )
        }
    }
}
