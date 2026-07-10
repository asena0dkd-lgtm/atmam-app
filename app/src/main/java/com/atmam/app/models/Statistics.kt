package com.atmam.app.models

data class Statistics(
    val totalTasks: Int = 0,
    val completedTasks: Int = 0,
    val failedTasks: Int = 0,
    val postponedTasks: Int = 0,
    val completionRate: Float = 0f,
    val currentStreak: Int = 0,
    val bestStreak: Int = 0,
    val overdueTasks: Int = 0,
    val tasksByPriority: Map<Int, Int> = emptyMap(),
    val tasksByTag: Map<String, Int> = emptyMap(),
    val weeklyCompletion: List<DailyStat> = emptyList(),
    val failedByCategory: Map<String, Int> = emptyMap(),
    val averagePostponeCount: Float = 0f
) {
    val activeTasks: Int get() = totalTasks - completedTasks - failedTasks

    data class DailyStat(val dayName: String, val completed: Int, val failed: Int, val date: Long)
}
