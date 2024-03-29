package net.simno.dmach.core

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import net.simno.dmach.theme.AppTheme

@Composable
fun OptionsDialog(
    text: String,
    option1Text: String,
    option2Text: String,
    onDismiss: () -> Unit,
    onOption1: () -> Unit,
    onOption2: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    properties: DialogProperties = DialogProperties()
) {
    val surface = MaterialTheme.colorScheme.surface
    val onPrimary = MaterialTheme.colorScheme.onPrimary
    val shapeMedium = MaterialTheme.shapes.medium
    val paddingLarge = AppTheme.dimens.paddingLarge

    CoreDialog(
        onDismiss = onDismiss,
        properties = properties
    ) {
        Column(
            modifier = modifier
                .background(
                    color = onPrimary,
                    shape = shapeMedium
                )
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(paddingLarge),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            LightMediumText(text = text)
            if (enabled) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    LightButton(
                        text = option1Text,
                        onClick = onOption1
                    )
                    Spacer(modifier = Modifier.size(paddingLarge))
                    LightButton(
                        text = option2Text,
                        onClick = onOption2
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
