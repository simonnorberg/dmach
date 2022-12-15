package net.simno.dmach.machine.ui

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts.CreateDocument
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.simno.dmach.BuildConfig
import net.simno.dmach.R
import net.simno.dmach.core.OptionsDialog
import java.io.File

private const val WAV_MIME_TYPE = "audio/x-wav"

@Composable
fun ExportDialog(
    enabled: Boolean,
    waveFile: File?,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val updatedOnDismiss by rememberUpdatedState(onDismiss)

    val shareLauncher = rememberLauncherForActivityResult(StartActivityForResult()) {
    }

    val saveLauncher = rememberLauncherForActivityResult(CreateDocument(WAV_MIME_TYPE)) { uri ->
        if (uri != null && waveFile != null) {
            scope.launch(Dispatchers.IO) {
                runCatching {
                    context.contentResolver.openOutputStream(uri)?.use { stream ->
                        stream.write(waveFile.readBytes())
                    }
                }
            }
        }
    }

    OptionsDialog(
        text = stringResource(R.string.export_wave_file),
        option1Text = stringResource(R.string.save),
        option2Text = stringResource(R.string.share),
        onDismiss = {
            runCatching { waveFile?.delete() }
            updatedOnDismiss()
        },
        onOption1 = {
            waveFile?.name?.let { name -> saveLauncher.launch(name) }
        },
        onOption2 = {
            waveFile?.let { file ->
                val uri = FileProvider.getUriForFile(context, BuildConfig.APPLICATION_ID, file)
                val target = Intent(Intent.ACTION_SEND).apply {
                    setDataAndType(uri, WAV_MIME_TYPE)
                    putExtra(Intent.EXTRA_STREAM, uri)
                    putExtra(Intent.EXTRA_SUBJECT, file.name)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                val intent = Intent.createChooser(target, context.getString(R.string.share))
                shareLauncher.launch(intent)
            }
        },
        enabled = enabled,
        properties = DialogProperties(
            dismissOnBackPress = enabled,
            dismissOnClickOutside = enabled
        )
    )
}
