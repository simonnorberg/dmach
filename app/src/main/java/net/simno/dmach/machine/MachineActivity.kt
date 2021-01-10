package net.simno.dmach.machine

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.shareIn
import net.simno.dmach.data.Channel.Companion.NONE_ID
import net.simno.dmach.data.Patch
import net.simno.dmach.databinding.MachineActivityBinding
import net.simno.dmach.flow.checkedChanges
import net.simno.dmach.flow.clicks
import net.simno.dmach.flow.launchWhenCreatedIn
import net.simno.dmach.flow.longClicks
import net.simno.dmach.flow.selections
import net.simno.dmach.patch.PatchActivity
import net.simno.dmach.util.logError

@AndroidEntryPoint
class MachineActivity : AppCompatActivity() {

    private val binding: MachineActivityBinding by lazy {
        MachineActivityBinding.inflate(layoutInflater)
    }

    private val viewModel: MachineViewModel by viewModels()

    private val channels: List<View> by lazy {
        listOf(
            binding.channelBD,
            binding.channelSD,
            binding.channelCP,
            binding.channelTT,
            binding.channelCB,
            binding.channelHH
        )
    }
    private val settings: List<View> by lazy {
        listOf(
            binding.setting1,
            binding.setting2,
            binding.setting3,
            binding.setting4,
            binding.setting5,
            binding.setting6
        )
    }

    private val dialogBackPresses = Channel<Unit>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.gravityFader.positions.onEach { position ->
            binding.chaosPad.setGravity(1f - position.y)
        }.launchWhenCreatedIn(lifecycleScope)

        binding.patchButton.clicks().onEach {
            startActivity(Intent(this, PatchActivity::class.java))
        }.launchWhenCreatedIn(lifecycleScope)

        binding.logoText.longClicks().onEach {
            startActivity(Intent(this, OssLicensesMenuActivity::class.java))
        }.launchWhenCreatedIn(lifecycleScope)

        actions()
            .buffer(0)
            .shareIn(lifecycleScope, SharingStarted.Lazily)
            .let(viewModel)
            .flowOn(Dispatchers.Default)
            .onEach(::render)
            .catch { logError("MachineActivity", "actions", it) }
            .launchWhenCreatedIn(lifecycleScope)
    }

    override fun onBackPressed() {
        if (binding.dialogShadow.isVisible) {
            runCatching { dialogBackPresses.offer(Unit) }
        } else {
            super.onBackPressed()
        }
    }

    private fun actions(): Flow<Action> = merge(
        *channels.map { ch -> ch.selections().map { SelectChannelAction(it.first, it.second) } }.toTypedArray(),
        *settings.map { s -> s.selections().map { SelectSettingAction(it.first) } }.toTypedArray(),
        dialogBackPresses.consumeAsFlow().map { DismissAction },
        binding.dialogShadow.clicks().map { DismissAction },
        binding.configDialog.tempoChanger.values.map { ChangeTempoAction(it) },
        binding.configDialog.swingChanger.values.map { ChangeSwingAction(it) },
        binding.configDialog.audioFocusCheck.checkedChanges().map { AudioFocusAction(it) },
        binding.playButton.clicks().map { PlayPauseAction },
        binding.configButton.clicks().map { ConfigAction },
        binding.resetButton.clicks().map { ChangeSeqenceAction(Patch.EMPTY_SEQUENCE) },
        binding.randomButton.clicks().map { ChangeSeqenceAction(Patch.RANDOM_SEQUENCE) },
        binding.stepSequencer.sequences.map { ChangeSeqenceAction(it) },
        binding.chaosPad.positions.map { ChangePositionAction(it) },
        binding.panFader.positions.map { ChangePanAction(it.y) }
    ).onStart { emitAll(flowOf(LoadAction, PlaybackAction)) }

    private fun render(state: ViewState) {
        binding.playButton.isSelected = state.isPlaying
        binding.configButton.isSelected = state.showConfig
        binding.dialogShadow.isVisible = state.showConfig
        binding.configDialog.root.isVisible = state.showConfig
        if (state.showConfig) {
            binding.configDialog.tempoValue.text = state.tempo.toString()
            binding.configDialog.swingValue.text = state.swing.toString()
            binding.configDialog.tempoChanger.setValue(state.tempo)
            binding.configDialog.swingChanger.setValue(state.swing)
            binding.configDialog.audioFocusCheck.isChecked = state.ignoreAudioFocus
            return
        }

        binding.stepSequencer.setSequence(state.sequence)
        binding.stepSequencer.isVisible = state.selectedChannel == NONE_ID
        binding.patchGroup.isVisible = state.selectedChannel != NONE_ID

        channels.forEach { channel ->
            channel.isSelected = state.selectedChannel == channel.tag.toString().toInt()
        }
        settings.forEachIndexed { index, setting ->
            setting.isVisible = state.settingsSize > index
            setting.isSelected = state.selectedSetting == setting.tag.toString().toInt()
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
}
