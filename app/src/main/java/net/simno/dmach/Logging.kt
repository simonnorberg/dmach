package net.simno.dmach

import android.util.Log

fun logError(tag: String, message: String, error: Throwable) {
    if (BuildConfig.DEBUG) {
        Log.e(tag, message, error)
    }
}
