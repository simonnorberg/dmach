package net.simno.dmach.machine.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import net.simno.dmach.core.LightMediumText
import net.simno.dmach.theme.AppTheme

@Composable
fun ConfigCheckbox(
    text: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    val surface = MaterialTheme.colorScheme.surface
    val onPrimary = MaterialTheme.colorScheme.onPrimary
    val shapeMedium = MaterialTheme.shapes.medium
    val paddingLarge = AppTheme.dimens.PaddingLarge
    val paddingSmall = AppTheme.dimens.PaddingSmall
    val configHeightSmall = AppTheme.dimens.ConfigHeightSmall

    Row(
        modifier = Modifier
            .background(
                color = onPrimary,
                shape = shapeMedium
            )
            .fillMaxWidth()
            .height(configHeightSmall)
            .padding(
                start = paddingLarge,
                top = paddingSmall,
                end = paddingSmall,
                bottom = paddingSmall
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        LightMediumText(text = text)
        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = CheckboxDefaults.colors(
                checkedColor = surface,
                uncheckedColor = surface,
                checkmarkColor = onPrimary
            )
        )
    }
}
