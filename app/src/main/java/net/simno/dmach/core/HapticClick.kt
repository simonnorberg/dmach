package net.simno.dmach.core

import androidx.compose.runtime.Composable
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback

@Composable
fun hapticClick(block: (() -> Unit)?): () -> Unit {
    val haptic = LocalHapticFeedback.current
    return {
        block?.let {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            it()
        }
    }
}
