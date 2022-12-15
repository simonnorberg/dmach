package net.simno.dmach.core

import androidx.compose.material3.ElevatedButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun LightButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    ElevatedButton(
        onClick = onClick,
        modifier = modifier
    ) {
        DarkMediumLabel(text)
    }
}
