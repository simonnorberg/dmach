package net.simno.dmach.machine

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.Button
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity
import com.jakewharton.rxbinding3.view.clicks
import com.jakewharton.rxbinding3.widget.checkedChanges
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import net.simno.dmach.R
import net.simno.dmach.data.Channel.Companion.NONE_ID
import net.simno.dmach.data.Patch
import net.simno.dmach.databinding.ConfigDialogBinding
import net.simno.dmach.databinding.MachineActivityBinding
import net.simno.dmach.logError
import net.simno.dmach.machine.view.ChaosPad
import net.simno.dmach.patch.PatchActivity
import net.simno.dmach.rx.dismisses
import net.simno.dmach.rx.positions
import net.simno.dmach.rx.selections
import net.simno.dmach.rx.sequences
import net.simno.dmach.rx.values

class MachineActivity : AppCompatActivity() {

    private val viewModel: MachineViewModel by viewModels(factoryProducer = {
        MachineViewModelFactory(application, lifecycle)
    })
    private val binding: MachineActivityBinding by lazy {
        MachineActivityBinding.inflate(layoutInflater)
    }
    private val dialogBinding: ConfigDialogBinding by lazy {
        ConfigDialogBinding.inflate(layoutInflater)
    }
    private val configDialog: AlertDialog by lazy {
        AlertDialog.Builder(this, R.style.DialogTheme).setView(dialogBinding.root).create()
    }
    private val channels: List<Button> by lazy {
        listOf(
            binding.channelBD,
            binding.channelSD,
            binding.channelCP,
            binding.channelTT,
            binding.channelCB,
            binding.channelHH
        )
    }
    private val settings: List<Button> by lazy {
        listOf(
            binding.setting1,
            binding.setting2,
            binding.setting3,
            binding.setting4,
            binding.setting5,
            binding.setting6
        )
    }
    private var disposable: Disposable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.gravityFader.onPositionChangedListener = ChaosPad.GravityListener(binding.chaosPad)
        binding.patchButton.setOnClickListener {
            startActivity(Intent(this, PatchActivity::class.java))
        }
        binding.logoText.setOnLongClickListener {
            startActivity(Intent(this, OssLicensesMenuActivity::class.java))
            true
        }

        disposable = actions()
            .compose(viewModel)
            .subscribeOn(AndroidSchedulers.mainThread())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(::render) { logError("MachineActivity", "actions", it) }
    }

    override fun onDestroy() {
        disposable?.dispose()
        super.onDestroy()
    }

    private fun actions(): Flowable<Action> = Observable
        .mergeArray<Action>(
            *channels.map { ch -> ch.selections().map { SelectChannelAction(it.first, it.second) } }.toTypedArray(),
            *settings.map { s -> s.selections().map { SelectSettingAction(it.first) } }.toTypedArray(),
            configDialog.dismisses().map { ConfigAction(false) },
            dialogBinding.tempoChanger.values().map { ChangeTempoAction(it) },
            dialogBinding.swingChanger.values().map { ChangeSwingAction(it) },
            dialogBinding.audioFocusCheck.checkedChanges().skipInitialValue().map { AudioFocusAction(it) },
            binding.playButton.clicks().map { PlayPauseAction },
            binding.configButton.clicks().map { ConfigAction(true) },
            binding.resetButton.clicks().map { ChangeSeqenceAction(Patch.EMPTY_SEQUENCE) },
            binding.randomButton.clicks().map { ChangeSeqenceAction(Patch.RANDOM_SEQUENCE) },
            binding.stepSequencer.sequences().map { ChangeSeqenceAction(it) },
            binding.chaosPad.positions().map { ChangePositionAction(it) },
            binding.panFader.positions().map { ChangePanAction(it.y) }
        )
        .startWithArray(LoadAction, PlaybackAction)
        .toFlowable(BackpressureStrategy.BUFFER)

    private fun render(state: ViewState) {
        binding.playButton.selectedIf(state.isPlaying)
        binding.configButton.selectedIf(state.showConfig)
        if (state.showConfig && !configDialog.isShowing) {
            configDialog.show()
        }
        dialogBinding.tempoValue.text = state.tempo.toString()
        dialogBinding.swingValue.text = state.swing.toString()
        dialogBinding.tempoChanger.setValue(state.tempo)
        dialogBinding.swingChanger.setValue(state.swing)
        dialogBinding.audioFocusCheck.isChecked = state.ignoreAudioFocus

        binding.stepSequencer.setSequence(state.sequence)
        binding.stepSequencer.visibleIf(state.selectedChannel == NONE_ID)
        binding.patchGroup.visibleIf(state.selectedChannel != NONE_ID)

        channels.forEach { channel ->
            channel.selectedIf(state.selectedChannel == channel.tag.toString().toInt())
        }
        settings.forEachIndexed { index, setting ->
            setting.visibleIf(state.settingsSize > index)
            setting.selectedIf(state.selectedSetting == setting.tag.toString().toInt())
        }

        binding.horizontalText.text = state.hText
        binding.verticalText.text = state.vText

        state.position?.let {
            binding.chaosPad.setPosition(state.position.x, state.position.y)
        }
        state.pan?.let {
            binding.panFader.setPosition(y = state.pan)
        }
    }

    private fun View.visibleIf(condition: Boolean) {
        visibility = if (condition) VISIBLE else GONE
    }

    private fun View.selectedIf(condition: Boolean) {
        isSelected = condition
    }
}
