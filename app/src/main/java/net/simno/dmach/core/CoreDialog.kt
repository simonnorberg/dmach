package net.simno.dmach.core

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.window.DialogProperties
import net.simno.dmach.theme.AppTheme

@Composable
fun CoreDialog(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    properties: DialogProperties = DialogProperties(),
    content: @Composable BoxScope.() -> Unit
) {
    BasicAlertDialog(
        onDismissRequest = onDismiss,
        properties = properties
    ) {
        var backProgress by remember { mutableFloatStateOf(0f) }
        var inPredictiveBack by remember { mutableStateOf(false) }
        PredictiveBackProgress(
            onProgress = { backProgress = it },
            onInPredictiveBack = { inPredictiveBack = it },
            onBack = onDismiss
        )
        Box(
            modifier = modifier
                .scale((1f - backProgress).coerceAtLeast(0.85f))
                .background(
                    color = MaterialTheme.colorScheme.primary,
                    shape = MaterialTheme.shapes.medium
                )
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(AppTheme.dimens.paddingSmall),
            content = content
        )
    }
}
