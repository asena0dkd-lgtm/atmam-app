package com.atmam.app.ui.tasks

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.atmam.app.AtmamApp
import com.atmam.app.R
import com.atmam.app.databinding.FragmentTasksBinding
import com.atmam.app.models.Task

class TasksFragment : Fragment() {

    private var _binding: FragmentTasksBinding? = null
    private val binding get() = _binding!!
    private val taskDao by lazy { AtmamApp.getInstance().taskDao }
    private lateinit var taskAdapter: TaskAdapter
    private var currentFilter: TaskFilter = TaskFilter.ALL

    private enum class TaskFilter { ALL, TODAY, UPCOMING, OVERDUE, FAILED }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentTasksBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupFilterChips()
        setupFab()
        loadTasks()
    }

    private fun setupRecyclerView() {
        taskAdapter = TaskAdapter(
            onTaskClick = { task -> openTaskDetail(task) },
            onTaskLongClick = { task -> showTaskOptions(task); true },
            onCompleteClick = { task -> showCompletionDialog(task) }
        )
        binding.recyclerTasks.apply { layoutManager = LinearLayoutManager(requireContext()); adapter = taskAdapter }
    }

    private fun setupFilterChips() {
        binding.chipGroupFilter.setOnCheckedStateChangeListener { _, checkedIds ->
            currentFilter = when (checkedIds.firstOrNull()) {
                R.id.chipToday -> TaskFilter.TODAY
                R.id.chipUpcoming -> TaskFilter.UPCOMING
                R.id.chipOverdue -> TaskFilter.OVERDUE
                R.id.chipFailed -> TaskFilter.FAILED
                else -> TaskFilter.ALL
            }
            loadTasks()
        }
    }

    private fun setupFab() {
        binding.fabAddTask.setOnClickListener {
            startActivity(Intent(requireContext(), TaskEditActivity::class.java).apply { putExtra("is_new", true) })
        }
    }

    private fun loadTasks() {
        val tasks = when (currentFilter) {
            TaskFilter.ALL -> taskDao.getAllActive()
            TaskFilter.TODAY -> taskDao.getTasksForToday()
            TaskFilter.UPCOMING -> taskDao.getUpcomingTasks()
            TaskFilter.OVERDUE -> taskDao.getOverdueTasks()
            TaskFilter.FAILED -> taskDao.getFailedTasks()
        }
        taskAdapter.submitList(tasks)
        binding.tvSubtitle.text = "${taskDao.getAllActive().size} مهام قيد الانتظار"
        binding.emptyState.visibility = if (tasks.isEmpty()) View.VISIBLE else View.GONE
    }

    private fun openTaskDetail(task: Task) {
        startActivity(Intent(requireContext(), TaskDetailActivity::class.java).apply { putExtra("task_id", task.id) })
    }

    private fun showTaskOptions(task: Task) {
        val popup = PopupMenu(requireContext(), binding.recyclerTasks)
        popup.menuInflater.inflate(R.menu.menu_task_options, popup.menu)
        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_edit -> {
                    startActivity(Intent(requireContext(), TaskEditActivity::class.java).apply { putExtra("task_id", task.id); putExtra("is_new", false) })
                    true
                }
                R.id.action_complete -> { showCompletionDialog(task); true }
                R.id.action_postpone -> { taskDao.postponeTask(task.id); loadTasks(); Toast.makeText(requireContext(), "تم تأجيل المهمة", Toast.LENGTH_SHORT).show(); true }
                R.id.action_delete -> { showDeleteConfirm(task); true }
                else -> false
            }
        }
        popup.show()
    }

    private fun showCompletionDialog(task: Task) {
        AlertDialog.Builder(requireContext())
            .setTitle("إتمام المهمة")
            .setMessage("هل تريد إتمام المهمة '${task.title}'؟")
            .setPositiveButton("إتمام") { _, _ -> taskDao.completeTask(task.id); loadTasks(); Toast.makeText(requireContext(), "تم إتمام المهمة!", Toast.LENGTH_SHORT).show() }
            .setNegativeButton("إسقاط") { _, _ -> taskDao.failTask(task.id); loadTasks(); Toast.makeText(requireContext(), "تم إسقاط المهمة", Toast.LENGTH_SHORT).show() }
            .setNeutralButton("إلغاء", null)
            .show()
    }

    private fun showDeleteConfirm(task: Task) {
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.action_delete).setMessage(R.string.msg_confirm_delete)
            .setPositiveButton(R.string.action_delete) { _, _ -> taskDao.delete(task.id); loadTasks(); Toast.makeText(requireContext(), R.string.msg_task_deleted, Toast.LENGTH_SHORT).show() }
            .setNegativeButton(R.string.action_cancel, null)
            .show()
    }

    override fun onResume() { super.onResume(); loadTasks() }
    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}
