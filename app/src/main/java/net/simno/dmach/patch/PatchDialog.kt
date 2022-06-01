package net.simno.dmach.patch

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.window.Dialog
import net.simno.dmach.R
import net.simno.dmach.core.LightMediumText
import net.simno.dmach.theme.AppTheme

@Composable
fun PatchDialog(
    text: String,
    @StringRes confirmText: Int,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    val primary = MaterialTheme.colorScheme.primary
    val onPrimary = MaterialTheme.colorScheme.onPrimary
    val paddingLarge = AppTheme.dimens.PaddingLarge
    val paddingSmall = AppTheme.dimens.PaddingSmall
    Dialog(
        onDismissRequest = onDismiss
    ) {
        Box(
            modifier = Modifier
                .background(
                    color = primary,
                    shape = MaterialTheme.shapes.small
                )
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(paddingSmall)
        ) {
            Column(
                modifier = Modifier
                    .background(
                        color = onPrimary,
                        shape = MaterialTheme.shapes.small
                    )
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .padding(paddingLarge),
                verticalArrangement = Arrangement.spacedBy(paddingLarge)
            ) {
                LightMediumText(
                    text = text,
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    LightMediumText(
                        text = stringResource(R.string.cancel).uppercase(),
                        modifier = Modifier
                            .clip(MaterialTheme.shapes.small)
                            .clickable(onClick = onDismiss)
                            .padding(paddingSmall)
                    )
                    Spacer(modifier = Modifier.size(paddingLarge))
                    LightMediumText(
                        text = stringResource(confirmText).uppercase(),
                        modifier = Modifier
                            .clip(MaterialTheme.shapes.small)
                            .clickable(onClick = onConfirm)
                            .padding(paddingSmall)
                    )
                }
            }
        }
    }
}
