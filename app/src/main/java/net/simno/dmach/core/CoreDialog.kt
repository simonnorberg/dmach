package net.simno.dmach.core

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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import net.simno.dmach.theme.AppTheme

@Composable
fun CoreDialog(
    text: String,
    option1Text: String,
    option2Text: String,
    onDismiss: () -> Unit,
    onOption1: () -> Unit,
    onOption2: () -> Unit,
    enabled: Boolean = true,
    properties: DialogProperties = DialogProperties()
) {
    val surface = MaterialTheme.colorScheme.surface
    val primary = MaterialTheme.colorScheme.primary
    val onPrimary = MaterialTheme.colorScheme.onPrimary
    val shapeSmall = MaterialTheme.shapes.small
    val paddingLarge = AppTheme.dimens.PaddingLarge
    val paddingSmall = AppTheme.dimens.PaddingSmall

    Dialog(
        onDismissRequest = onDismiss,
        properties = properties
    ) {
        Box(
            modifier = Modifier
                .background(
                    color = primary,
                    shape = shapeSmall
                )
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(paddingSmall)
        ) {
            Column(
                modifier = Modifier
                    .background(
                        color = onPrimary,
                        shape = shapeSmall
                    )
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .padding(paddingLarge),
                verticalArrangement = Arrangement.spacedBy(paddingLarge)
            ) {
                LightMediumText(text = text)
                if (enabled) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        LightMediumText(
                            text = option1Text,
                            modifier = Modifier
                                .clip(shapeSmall)
                                .clickable(onClick = onOption1)
                                .padding(paddingSmall)
                        )
                        Spacer(modifier = Modifier.size(paddingLarge))
                        LightMediumText(
                            text = option2Text,
                            modifier = Modifier
                                .clip(shapeSmall)
                                .clickable(onClick = onOption2)
                                .padding(paddingSmall)
                        )
                    }
                } else {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.CenterHorizontally),
                        color = surface
                    )
                }
            }
        }
    }
}
