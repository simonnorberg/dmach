package net.simno.dmach.playback

import android.content.Context
import java.io.File
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.time.DurationUnit
import kotlin.time.toDuration
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.simno.dmach.data.Steps
import net.simno.dmach.data.Tempo
import net.simno.kortholt.ExperimentalWaveFile
import net.simno.kortholt.Kortholt

@OptIn(ExperimentalWaveFile::class)
class WaveExporter(
    private val context: Context,
    private val kortholt: Kortholt.Player
) {
    private val isExporting = AtomicBoolean(false)

    fun isExporting(): Boolean = isExporting.get()

    suspend fun saveWaveFile(
        title: String,
        tempo: Tempo,
        steps: Steps
    ): File? = withContext(Dispatchers.IO) {
        isExporting.set(true)
        val result = runCatching {
            val fileName = "${title}_${tempo.value}_BPM.wav"
            val dir = File(context.filesDir, "wav")
            dir.mkdirs()
            val outputFile = File(dir, fileName)

            val milliSecondsPerBeat = (60 * 1000) / tempo.value.toDouble()
            val duration = (steps.value * milliSecondsPerBeat).toDuration(DurationUnit.MILLISECONDS)

            kortholt.saveWaveFile(
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
