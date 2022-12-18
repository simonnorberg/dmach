package net.simno.dmach.util

import android.util.Log
import net.simno.dmach.BuildConfig
import net.simno.dmach.data.Patch.Companion.MASKS
import net.simno.dmach.data.Patch.Companion.STEPS

fun logSequence(tag: String, sequence: List<Int>) {
    if (BuildConfig.DEBUG) {
        val fold = { index: Int, acc: String, step: Int ->
            acc + (if (step > 0) "◼" else "◻") + (if ((index + 1) % 4 == 0) "|" else "")
        }
        Log.d(tag, " ̲ ̲ ̲ ̲ ̲ ̲ ̲ ̲ ̲ ̲ ̲ ̲ ̲ ̲ ̲ ̲ ̲ ̲ ̲ ̲ ̲ ̲ ̲ ̲")
        Log.d(tag, sequence.take(STEPS).map { it and MASKS[0] }.foldIndexed("|BD|", fold))
        Log.d(tag, sequence.take(STEPS).map { it and MASKS[1] }.foldIndexed("|SD|", fold))
        Log.d(tag, sequence.take(STEPS).map { it and MASKS[2] }.foldIndexed("|CP|", fold))
        Log.d(tag, sequence.takeLast(STEPS).map { it and MASKS[0] }.foldIndexed("|TT|", fold))
        Log.d(tag, sequence.takeLast(STEPS).map { it and MASKS[1] }.foldIndexed("|CB|", fold))
        Log.d(tag, sequence.takeLast(STEPS).map { it and MASKS[2] }.foldIndexed("|HH|", fold))
        Log.d(tag, "‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾‾")
    }
}

fun logError(tag: String, message: String, error: Throwable) {
    if (BuildConfig.DEBUG) {
        Log.e(tag, message, error)
    }
}
