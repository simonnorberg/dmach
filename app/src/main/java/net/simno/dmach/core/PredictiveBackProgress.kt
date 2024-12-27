package net.simno.dmach.core

import androidx.activity.compose.PredictiveBackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import kotlin.coroutines.cancellation.CancellationException

@Composable
fun PredictiveBackProgress(
    onProgress: (Float) -> Unit,
    onInPredictiveBack: (Boolean) -> Unit,
    onBack: () -> Unit,
    enabled: Boolean = true
) {
    val currentOnProgress by rememberUpdatedState(onProgress)
    val currentOnInPredictiveBack by rememberUpdatedState(onInPredictiveBack)
    val currentOnBack by rememberUpdatedState(onBack)

    PredictiveBackHandler(enabled = enabled) { backEvents ->
        currentOnProgress(0f)
        try {
            backEvents.collect { event ->
                currentOnInPredictiveBack(true)
                currentOnProgress(event.progress)
            }
            currentOnInPredictiveBack(false)
            currentOnBack()
        } catch (e: CancellationException) {
            currentOnInPredictiveBack(false)
        }
    }
}
