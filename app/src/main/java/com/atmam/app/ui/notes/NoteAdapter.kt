package com.atmam.app.ui.notes

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.atmam.app.R
import com.atmam.app.databinding.ItemNoteBinding
import com.atmam.app.models.Note
import com.atmam.app.utils.DateUtils

class NoteAdapter(
    private val onNoteClick: (Note) -> Unit,
    private val onNoteLongClick: (Note) -> Boolean
) : ListAdapter<Note, NoteAdapter.NoteViewHolder>(NoteDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        return NoteViewHolder(ItemNoteBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class NoteViewHolder(private val binding: ItemNoteBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(note: Note) {
            binding.apply {
                tvTitle.text = note.title
                tvPreview.text = note.getPreview(150)
                tvDate.text = DateUtils.formatRelative(note.updatedAt)
                chipFolder.text = note.folder

                chipGroupTags.removeAllViews()
                val tags = note.getTagList()
                chipGroupTags.visibility = if (tags.isNotEmpty()) View.VISIBLE else View.GONE
                tags.take(3).forEach { tag ->
                    val chip = com.google.android.material.chip.Chip(root.context).apply {
                        text = tag; isClickable = false; isCheckable = false
                        chipBackgroundColor = android.content.res.ColorStateList.valueOf(root.context.getColor(R.color.primary_light))
                        setTextColor(root.context.getColor(R.color.primary))
                        chipMinHeight = 24f; textSize = 10f
                    }
                    chipGroupTags.addView(chip)
                }

                ivLocked.visibility = if (note.isLocked) View.VISIBLE else View.GONE
                root.setOnClickListener { onNoteClick(note) }
                root.setOnLongClickListener { onNoteLongClick(note) }
            }
        }
    }

    class NoteDiffCallback : DiffUtil.ItemCallback<Note>() {
        override fun areItemsTheSame(oldItem: Note, newItem: Note) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Note, newItem: Note) = oldItem == newItem
    }
}
