package com.atmam.app.ui.stats

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.atmam.app.AtmamApp
import com.atmam.app.R
import com.atmam.app.databinding.FragmentStatisticsBinding
import com.atmam.app.models.Task

class StatisticsFragment : Fragment() {

    private var _binding: FragmentStatisticsBinding? = null
    private val binding get() = _binding!!
    private val taskDao by lazy { AtmamApp.getInstance().taskDao }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentStatisticsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadStatistics()
    }

    private fun loadStatistics() {
        val stats = taskDao.getStatistics()
        binding.apply {
            tvTotalTasks.text = stats.totalTasks.toString()
            tvCompletedTasks.text = stats.completedTasks.toString()
            tvFailedTasks.text = stats.failedTasks.toString()
            tvPostponedTasks.text = stats.postponedTasks.toString()
            tvCompletionRate.text = "${String.format("%.1f", stats.completionRate)}%"
            tvCurrentStreak.text = stats.currentStreak.toString()
            tvBestStreak.text = stats.bestStreak.toString()
            tvOverdueTasks.text = stats.overdueTasks.toString()
            val urgentCount = stats.tasksByPriority[Task.PRIORITY_URGENT] ?: 0
            val importantCount = stats.tasksByPriority[Task.PRIORITY_IMPORTANT] ?: 0
            val normalCount = stats.tasksByPriority[Task.PRIORITY_NORMAL] ?: 0
            tvUrgentCount.text = urgentCount.toString()
            tvImportantCount.text = importantCount.toString()
            tvNormalCount.text = normalCount.toString()
            val progress = if (stats.totalTasks > 0) (stats.completedTasks.toFloat() / stats.totalTasks * 100).toInt() else 0
            progressCompletion.progress = progress
        }
    }

    override fun onResume() { super.onResume(); loadStatistics() }
    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}
