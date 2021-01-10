package net.simno.dmach.util

import android.util.Log
import net.simno.dmach.BuildConfig

fun logError(tag: String, message: String, error: Throwable) {
    if (BuildConfig.DEBUG) {
        Log.e(tag, message, error)
    }
}
