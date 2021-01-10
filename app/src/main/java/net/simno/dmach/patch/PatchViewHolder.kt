package net.simno.dmach.patch

import androidx.recyclerview.widget.RecyclerView
import net.simno.dmach.data.Patch
import net.simno.dmach.databinding.PatchItemBinding

data class PatchViewHolder(
    private val binding: PatchItemBinding,
    private val background: Int
) : RecyclerView.ViewHolder(binding.root) {

    init {
        binding.root.setBackgroundColor(background)
    }

    fun bind(
        patch: Patch,
        onClick: (Int) -> Unit,
        onLongClick: (Int) -> Unit
    ) {
        binding.apply {
            titleText.text = patch.title
            swingText.text = patch.swing.toString()
            tempoText.text = patch.tempo.toString()
            root.setOnClickListener {
                onClick(bindingAdapterPosition)
            }
            root.setOnLongClickListener {
                onLongClick(bindingAdapterPosition)
                true
            }
        }
    }
}
