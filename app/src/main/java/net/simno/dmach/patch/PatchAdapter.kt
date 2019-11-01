package net.simno.dmach.patch

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import net.simno.dmach.R
import net.simno.dmach.data.Patch
import net.simno.dmach.databinding.PatchItemBinding

class PatchAdapter : PagedListAdapter<Patch, PatchAdapter.PatchViewHolder>(DIFF_CALLBACK) {

    var onClick: ((Patch) -> Unit)? = null
    var onLongClick: ((Patch) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PatchViewHolder {
        val binding = PatchItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        val color = ContextCompat.getColor(parent.context, if (viewType == 1) R.color.khaki else R.color.gurkha)
        binding.root.setBackgroundColor(color)
        return PatchViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PatchViewHolder, position: Int) {
        val patch = getItem(position) ?: return
        holder.binding.titleText.text = patch.title
        holder.binding.swingText.text = patch.swing.toString()
        holder.binding.tempoText.text = patch.tempo.toString()
        holder.binding.itemPatch.setOnClickListener {
            getItem(holder.adapterPosition)?.let { item ->
                onClick?.invoke(item)
            }
        }
        holder.binding.itemPatch.setOnLongClickListener {
            getItem(holder.adapterPosition)?.let { item ->
                if (itemCount > 1) {
                    onLongClick?.invoke(item)
                }
                true
            } ?: false
        }
    }

    override fun getItemViewType(position: Int) = if (position % 2 == 0) 1 else 0

    class PatchViewHolder(val binding: PatchItemBinding) : RecyclerView.ViewHolder(binding.root)

    companion object {
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Patch>() {
            override fun areItemsTheSame(oldItem: Patch, newItem: Patch) = oldItem.title == newItem.title

            override fun areContentsTheSame(oldItem: Patch, newItem: Patch) = oldItem == newItem
        }
    }
}
