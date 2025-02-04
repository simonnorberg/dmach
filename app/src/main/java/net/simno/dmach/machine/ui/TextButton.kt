package net.simno.dmach.machine.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.lifecycle.compose.dropUnlessResumed
import net.simno.dmach.core.LightMediumText
import net.simno.dmach.core.hapticClick

@Composable
fun TextButton(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    muted: Boolean = false,
    radioButton: Boolean = false,
    onLongClick: (() -> Unit)? = null
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val background = when {
        !enabled -> MaterialTheme.colorScheme.surface
        selected && radioButton -> MaterialTheme.colorScheme.secondary
        pressed -> MaterialTheme.colorScheme.onSecondary
        selected -> MaterialTheme.colorScheme.secondary
        muted -> MaterialTheme.colorScheme.onSurfaceVariant
        else -> MaterialTheme.colorScheme.primary
    }
    Box(
        modifier = modifier
            .background(
                color = background,
                shape = MaterialTheme.shapes.small
            )
            .combinedClickable(
                onClick = dropUnlessResumed(block = onClick),
                onLongClick = hapticClick(block = onLongClick),
                enabled = enabled,
                role = Role.Button,
                interactionSource = interactionSource,
                indication = null
            ),
        contentAlignment = Alignment.Center
    ) {
        LightMediumText(text = text.uppercase())
    }
}
