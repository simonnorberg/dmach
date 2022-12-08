package net.simno.dmach.playback

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.simno.kortholt.ExperimentalWaveFile
import net.simno.kortholt.Kortholt
import java.io.File
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.time.DurationUnit
import kotlin.time.toDuration

@OptIn(ExperimentalWaveFile::class)
class KortholtController(
    private val context: Context
) {
    private val isExporting = AtomicBoolean(false)

    fun create() {
        Kortholt.create(context)
    }

    fun destroy() {
        Kortholt.destroy()
    }

    fun isExporting(): Boolean = isExporting.get()

    suspend fun saveWaveFile(
        title: String,
        tempo: Int
    ): File? = withContext(Dispatchers.IO) {
        isExporting.set(true)
        val result = runCatching {
            val fileName = "${title}_${tempo}_BPM.wav"
            val dir = File(context.filesDir, "wav")
            dir.mkdirs()
            val outputFile = File(dir, fileName)

            val beats = 16
            val milliSecondsPerBeat = (60 * 1000) / tempo.toDouble()
            val duration = (beats * milliSecondsPerBeat).toDuration(DurationUnit.MILLISECONDS)

            Kortholt.saveWaveFile(
                context = context,
                outputFile = outputFile,
                duration = duration,
                startBang = "play",
                stopBang = "stop"
            )
            outputFile
        }
        isExporting.set(false)
        result.getOrNull()
    }
}
