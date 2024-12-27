package net.simno.dmach.core

import androidx.compose.material3.ElevatedButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.dropUnlessResumed

@Composable
fun LightButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    ElevatedButton(
        onClick = dropUnlessResumed(block = onClick),
        modifier = modifier
    ) {
        DarkMediumLabel(text)
    }
}
