package net.simno.dmach.patch

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.jakewharton.rxbinding3.view.clicks
import com.jakewharton.rxbinding3.widget.textChanges
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import net.simno.dmach.R
import net.simno.dmach.databinding.PatchActivityBinding
import net.simno.dmach.logError
import net.simno.dmach.rx.clicks
import net.simno.dmach.rx.dismisses
import net.simno.dmach.rx.longClicks
import net.simno.dmach.rx.negatives
import net.simno.dmach.rx.positives
import java.util.concurrent.TimeUnit.MILLISECONDS

class PatchActivity : AppCompatActivity() {

    private val viewModel: PatchViewModel by viewModels(factoryProducer = {
        PatchViewModelFactory(application)
    })
    private val binding: PatchActivityBinding by lazy { PatchActivityBinding.inflate(layoutInflater) }
    private val overwriteDialog by lazy { AlertDialog.Builder(this, R.style.DialogTheme).create() }
    private val deleteDialog by lazy { AlertDialog.Builder(this, R.style.DialogTheme).create() }
    private val patchAdapter by lazy { PatchAdapter() }
    private var disposable: Disposable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = patchAdapter
            itemAnimator = null
            setHasFixedSize(true)
        }

        disposable = actions()
            .compose(viewModel)
            .subscribeOn(AndroidSchedulers.mainThread())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(::render) { logError("PatchActivity", "actions", it) }
    }

    override fun onDestroy() {
        disposable?.dispose()
        super.onDestroy()
    }

    private fun actions(): Flowable<Action> = Observable
        .mergeArray(
            patchAdapter.clicks().map { SelectPatchAction(it.title) },
            patchAdapter.longClicks().map { DeletePatchAction(it.title) },
            overwriteDialog.dismisses().map { DismissAction },
            overwriteDialog.negatives(getString(R.string.cancel)).map { DismissAction },
            overwriteDialog.positives(getString(R.string.overwrite)).map { ConfirmOverwriteAction },
            deleteDialog.dismisses().map { DismissAction },
            deleteDialog.negatives(getString(R.string.cancel)).map { DismissAction },
            deleteDialog.positives(getString(R.string.delete)).map { ConfirmDeleteAction },
            binding.saveButton.clicks()
                .throttleFirst(500L, MILLISECONDS, Schedulers.computation())
                .flatMap { binding.saveText.textChanges().take(1) }
                .map { SavePatchAction(it.toString()) }
                .filter { it.title.isNotBlank() }
        )
        .startWith(LoadAllAction)
        .toFlowable(BackpressureStrategy.BUFFER)

    private fun render(state: ViewState) {
        if (state.finish) {
            disposable?.dispose()
            finish()
            return
        }
        if (state.showDelete) {
            deleteDialog.setMessage(getString(R.string.delete_patch, state.deleteTitle))
            deleteDialog.show()
        } else {
            deleteDialog.hide()
        }
        if (state.showOverwrite) {
            overwriteDialog.setMessage(getString(R.string.overwrite_patch, state.title))
            overwriteDialog.show()
        } else {
            overwriteDialog.hide()
        }
        binding.saveText.setText(state.title)
        binding.saveText.setSelection(state.title.length)

        patchAdapter.submitList(state.patches)
    }
}
