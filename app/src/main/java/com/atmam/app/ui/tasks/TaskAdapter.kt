package com.atmam.app.ui.tasks

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.atmam.app.R
import com.atmam.app.databinding.ItemTaskBinding
import com.atmam.app.models.Task
import com.atmam.app.utils.DateUtils

class TaskAdapter(
    private val onTaskClick: (Task) -> Unit,
    private val onTaskLongClick: (Task) -> Boolean,
    private val onCompleteClick: (Task) -> Unit
) : ListAdapter<Task, TaskAdapter.TaskViewHolder>(TaskDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        return TaskViewHolder(ItemTaskBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class TaskViewHolder(private val binding: ItemTaskBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(task: Task) {
            binding.apply {
                tvTitle.text = task.title
                tvDescription.visibility = if (task.description.isNotEmpty()) View.VISIBLE else View.GONE
                tvDescription.text = task.description

                val (priorityText, priorityColor) = when (task.priority) {
                    Task.PRIORITY_URGENT -> "عاجل" to root.context.getColor(R.color.priority_urgent)
                    Task.PRIORITY_IMPORTANT -> "مهم" to root.context.getColor(R.color.priority_important)
                    else -> "عادي" to root.context.getColor(R.color.priority_normal)
                }
                chipPriority.text = priorityText
                chipPriority.chipBackgroundColor = android.content.res.ColorStateList.valueOf(
                    when (task.priority) {
                        Task.PRIORITY_URGENT -> root.context.getColor(R.color.priority_urgent_light)
                        Task.PRIORITY_IMPORTANT -> root.context.getColor(R.color.priority_important_light)
                        else -> root.context.getColor(R.color.priority_normal_light)
                    }
                )
                chipPriority.setTextColor(priorityColor)

                tvPostponeCount.visibility = if (task.postponeCount > 0) View.VISIBLE else View.GONE
                tvPostponeCount.text = task.postponeCount.toString()
                if (task.postponeCount > 0) tvPostponeCount.setBackgroundColor(android.graphics.Color.parseColor(task.getPostponeColor()))

                if (task.dueDate != null) {
                    layoutDueDate.visibility = View.VISIBLE
                    tvDueDate.text = when {
                        DateUtils.isToday(task.dueDate) -> "اليوم"
                        DateUtils.isTomorrow(task.dueDate) -> "غداً"
                        else -> DateUtils.formatDate(task.dueDate)
                    }
                    if (task.isOverdue()) tvDueDate.setTextColor(root.context.getColor(R.color.danger))
                } else layoutDueDate.visibility = View.GONE

                layoutEnergy.visibility = if (task.energyLevel > 0) View.VISIBLE else View.GONE
                tvEnergy.text = Task.getEnergyLabel(task.energyLevel)

                layoutEstimatedTime.visibility = if (task.estimatedMinutes > 0) View.VISIBLE else View.GONE
                tvEstimatedTime.text = "${task.estimatedMinutes} دقيقة"

                ivRepeat.visibility = if (task.repeatType != Task.REPEAT_NONE) View.VISIBLE else View.GONE

                chipGroupTags.removeAllViews()
                val tags = task.getTagList()
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

                root.setOnClickListener { onTaskClick(task) }
                root.setOnLongClickListener { onTaskLongClick(task) }
            }
        }
    }

    class TaskDiffCallback : DiffUtil.ItemCallback<Task>() {
        override fun areItemsTheSame(oldItem: Task, newItem: Task) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Task, newItem: Task) = oldItem == newItem
    }
}
