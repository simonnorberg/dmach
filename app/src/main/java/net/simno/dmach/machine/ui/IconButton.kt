package net.simno.dmach.machine.ui

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Dp
import androidx.lifecycle.compose.dropUnlessResumed
import net.simno.dmach.core.hapticClick
import net.simno.dmach.theme.AppTheme

@Composable
fun IconButton(
    icon: ImageVector,
    @StringRes description: Int,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    iconPadding: Dp = AppTheme.dimens.paddingLarge,
    onLongClick: (() -> Unit)? = null
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val background = when {
        pressed -> MaterialTheme.colorScheme.onSecondary
        selected -> MaterialTheme.colorScheme.onSecondary
        else -> MaterialTheme.colorScheme.primary
    }
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                color = background,
                shape = MaterialTheme.shapes.medium
            )
            .combinedClickable(
                onClick = dropUnlessResumed(block = onClick),
                onLongClick = hapticClick(block = onLongClick),
                enabled = true,
                role = Role.Button,
                interactionSource = interactionSource,
                indication = null
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = stringResource(description),
            tint = MaterialTheme.colorScheme.surface,
            modifier = Modifier
                .fillMaxSize()
                .padding(iconPadding)
        )
    }
}
