package net.simno.dmach.patch

import android.os.Bundle
import android.view.inputmethod.InputMethodManager
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.getSystemService
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.shareIn
import net.simno.dmach.R
import net.simno.dmach.databinding.PatchActivityBinding
import net.simno.dmach.flow.clicks
import net.simno.dmach.flow.launchWhenCreatedIn
import net.simno.dmach.flow.textChanges
import net.simno.dmach.util.logError

@AndroidEntryPoint
class PatchActivity : AppCompatActivity() {

    private val binding: PatchActivityBinding by lazy {
        PatchActivityBinding.inflate(layoutInflater)
    }

    private val viewModel: PatchViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        val patchAdapter = PatchAdapter()

        binding.recyclerView.apply {
            adapter = patchAdapter
            itemAnimator = null
            setHasFixedSize(true)
        }

        viewModel.patches
            .mapLatest(patchAdapter::submitData)
            .buffer(0)
            .catch { logError("PatchActivity", "patches", it) }
            .launchWhenCreatedIn(lifecycleScope)

        actions(patchAdapter)
            .buffer(0)
            .shareIn(lifecycleScope, SharingStarted.Lazily)
            .let(viewModel)
            .flowOn(Dispatchers.Default)
            .onEach(::render)
            .catch { logError("PatchActivity", "actions", it) }
            .launchWhenCreatedIn(lifecycleScope)
    }

    private fun actions(patchAdapter: PatchAdapter): Flow<Action> = merge(
        patchAdapter.clicks.map { SelectPatchAction(it.title) },
        patchAdapter.longClicks.map { DeletePatchAction(it.title) },
        binding.dialogShadow.clicks().map { DismissAction },
        binding.overwriteCancelButton.clicks().map { DismissAction },
        binding.overwriteConfirmButton.clicks().map { ConfirmOverwriteAction },
        binding.deleteCancelButton.clicks().map { DismissAction },
        binding.deleteConfirmButton.clicks().map { ConfirmDeleteAction },
        binding.saveText.textChanges().filter { it.isNotBlank() }.flatMapLatest { text ->
            binding.saveButton.clicks().map { SavePatchAction(text) }
        }
    ).onStart { emit(LoadAction) }

    private fun render(state: ViewState) {
        if (isFinishing) {
            return
        }
        if (state.finish) {
            finish()
            return
        }
        if (state.showDelete || state.showOverwrite) {
            getSystemService<InputMethodManager>()?.hideSoftInputFromWindow(binding.saveText.windowToken, 0)
        }
        binding.deleteDialog.isVisible = state.showDelete
        binding.overwriteDialog.isVisible = state.showOverwrite
        binding.dialogShadow.isVisible = state.showDelete || state.showOverwrite
        binding.deleteTitle.text = getString(R.string.delete_patch, state.deleteTitle)
        binding.overwriteTitle.text = getString(R.string.overwrite_patch, state.title)
        binding.saveText.setText(state.title)
        binding.saveText.setSelection(state.title.length)
    }
}
