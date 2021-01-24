package net.simno.dmach.patch

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import net.simno.dmach.R
import net.simno.dmach.data.Patch
import net.simno.dmach.databinding.PatchItemBinding

class PatchAdapter : PagingDataAdapter<Patch, PatchViewHolder>(DIFF_CALLBACK) {

    private val _clicks = Channel<Patch>()
    private val _longClicks = Channel<Patch>()
    val clicks: Flow<Patch> = _clicks.receiveAsFlow()
    val longClicks: Flow<Patch> = _longClicks.receiveAsFlow()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PatchViewHolder {
        val binding = PatchItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        val background = ContextCompat.getColor(parent.context, viewType)
        return PatchViewHolder(binding, background)
    }

    override fun onBindViewHolder(holder: PatchViewHolder, position: Int) {
        val patch = getItem(position) ?: return
        holder.bind(
            patch = patch,
            onClick = {
                runCatching { _clicks.offer(patch) }
            },
            onLongClick = {
                if (itemCount > 1) {
                    runCatching { _longClicks.offer(patch) }
                }
                true
            }
        )
    }

    override fun getItemViewType(position: Int): Int {
        return when {
            position % 2 == 0 -> R.color.khaki
            else -> R.color.gurkha
        }
    }

    companion object {
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Patch>() {
            override fun areItemsTheSame(oldItem: Patch, newItem: Patch) = oldItem.title == newItem.title

            override fun areContentsTheSame(oldItem: Patch, newItem: Patch) = oldItem == newItem
        }
    }
}
