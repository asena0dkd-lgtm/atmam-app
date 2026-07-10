package com.atmam.app.data

import android.content.ContentValues
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import com.atmam.app.models.Statistics
import com.atmam.app.models.Task
import java.util.Calendar

class TaskDao(private val dbHelper: AtmamDatabaseHelper) {

    private fun cursorToTask(cursor: Cursor): Task {
        return Task(
            id = cursor.getLong(cursor.getColumnIndexOrThrow(AtmamDatabaseHelper.COL_ID)),
            title = cursor.getString(cursor.getColumnIndexOrThrow(AtmamDatabaseHelper.COL_TITLE)),
            description = cursor.getString(cursor.getColumnIndexOrThrow(AtmamDatabaseHelper.COL_DESCRIPTION)) ?: "",
            priority = cursor.getInt(cursor.getColumnIndexOrThrow(AtmamDatabaseHelper.COL_PRIORITY)),
            energyLevel = cursor.getInt(cursor.getColumnIndexOrThrow(AtmamDatabaseHelper.COL_ENERGY_LEVEL)),
            estimatedMinutes = cursor.getInt(cursor.getColumnIndexOrThrow(AtmamDatabaseHelper.COL_ESTIMATED_MINUTES)),
            dueDate = cursor.getLongOrNull(cursor.getColumnIndexOrThrow(AtmamDatabaseHelper.COL_DUE_DATE)),
            reminderTime = cursor.getLongOrNull(cursor.getColumnIndexOrThrow(AtmamDatabaseHelper.COL_REMINDER_TIME)),
            repeatType = cursor.getInt(cursor.getColumnIndexOrThrow(AtmamDatabaseHelper.COL_REPEAT_TYPE)),
            repeatInterval = cursor.getInt(cursor.getColumnIndexOrThrow(AtmamDatabaseHelper.COL_REPEAT_INTERVAL)),
            repeatDaysOfWeek = cursor.getString(cursor.getColumnIndexOrThrow(AtmamDatabaseHelper.COL_REPEAT_DAYS_OF_WEEK)),
            tags = cursor.getString(cursor.getColumnIndexOrThrow(AtmamDatabaseHelper.COL_TAGS)) ?: "",
            status = cursor.getInt(cursor.getColumnIndexOrThrow(AtmamDatabaseHelper.COL_STATUS)),
            postponeCount = cursor.getInt(cursor.getColumnIndexOrThrow(AtmamDatabaseHelper.COL_POSTPONE_COUNT)),
            repeatMode = cursor.getInt(cursor.getColumnIndexOrThrow(AtmamDatabaseHelper.COL_REPEAT_MODE)),
            createdAt = cursor.getLong(cursor.getColumnIndexOrThrow(AtmamDatabaseHelper.COL_CREATED_AT)),
            completedAt = cursor.getLongOrNull(cursor.getColumnIndexOrThrow(AtmamDatabaseHelper.COL_COMPLETED_AT)),
            failedAt = cursor.getLongOrNull(cursor.getColumnIndexOrThrow(AtmamDatabaseHelper.COL_FAILED_AT)),
            nextRepeatDate = cursor.getLongOrNull(cursor.getColumnIndexOrThrow(AtmamDatabaseHelper.COL_NEXT_REPEAT_DATE)),
            isArchived = cursor.getInt(cursor.getColumnIndexOrThrow(AtmamDatabaseHelper.COL_IS_ARCHIVED)) == 1,
            deletedAt = cursor.getLongOrNull(cursor.getColumnIndexOrThrow(AtmamDatabaseHelper.COL_DELETED_AT))
        )
    }

    private fun Cursor.getLongOrNull(columnIndex: Int): Long? {
        return if (isNull(columnIndex)) null else getLong(columnIndex)
    }

    // ==================== CRUD ====================

    fun insert(task: Task): Long {
        val db = dbHelper.writableDatabase
        return db.insert(AtmamDatabaseHelper.TABLE_TASKS, null, dbHelper.taskToContentValues(task))
    }

    fun update(task: Task): Int {
        val db = dbHelper.writableDatabase
        return db.update(
            AtmamDatabaseHelper.TABLE_TASKS,
            dbHelper.taskToContentValues(task),
            "${AtmamDatabaseHelper.COL_ID} = ?",
            arrayOf(task.id.toString())
        )
    }

    fun delete(id: Long): Int {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put(AtmamDatabaseHelper.COL_DELETED_AT, System.currentTimeMillis())
        }
        return db.update(
            AtmamDatabaseHelper.TABLE_TASKS,
            values,
            "${AtmamDatabaseHelper.COL_ID} = ?",
            arrayOf(id.toString())
        )
    }

    fun getById(id: Long): Task? {
        val db = dbHelper.readableDatabase
        val cursor = db.query(
            AtmamDatabaseHelper.TABLE_TASKS,
            null,
            "${AtmamDatabaseHelper.COL_ID} = ? AND ${AtmamDatabaseHelper.COL_DELETED_AT} IS NULL",
            arrayOf(id.toString()),
            null, null, null
        )
        return cursor.use {
            if (it.moveToFirst()) cursorToTask(it) else null
        }
    }

    // ==================== LIST QUERIES ====================

    fun getAllActive(): List<Task> {
        val db = dbHelper.readableDatabase
        val cursor = db.query(
            AtmamDatabaseHelper.TABLE_TASKS,
            null,
            "${AtmamDatabaseHelper.COL_DELETED_AT} IS NULL AND ${AtmamDatabaseHelper.COL_STATUS} = ? AND ${AtmamDatabaseHelper.COL_IS_ARCHIVED} = 0",
            arrayOf(Task.STATUS_PENDING.toString()),
            null, null,
            "${AtmamDatabaseHelper.COL_PRIORITY} DESC, ${AtmamDatabaseHelper.COL_DUE_DATE} ASC"
        )
        return cursor.use {
            val tasks = mutableListOf<Task>()
            while (it.moveToNext()) tasks.add(cursorToTask(it))
            tasks
        }
    }

    fun getByStatus(status: Int): List<Task> {
        val db = dbHelper.readableDatabase
        val cursor = db.query(
            AtmamDatabaseHelper.TABLE_TASKS,
            null,
            "${AtmamDatabaseHelper.COL_DELETED_AT} IS NULL AND ${AtmamDatabaseHelper.COL_STATUS} = ?",
            arrayOf(status.toString()),
            null, null,
            "${AtmamDatabaseHelper.COL_CREATED_AT} DESC"
        )
        return cursor.use {
            val tasks = mutableListOf<Task>()
            while (it.moveToNext()) tasks.add(cursorToTask(it))
            tasks
        }
    }

    fun getFailedTasks(): List<Task> = getByStatus(Task.STATUS_FAILED)

    fun getOverdueTasks(): List<Task> {
        val db = dbHelper.readableDatabase
        val now = System.currentTimeMillis()
        val cursor = db.query(
            AtmamDatabaseHelper.TABLE_TASKS,
            null,
            "${AtmamDatabaseHelper.COL_DELETED_AT} IS NULL AND ${AtmamDatabaseHelper.COL_STATUS} = ? AND ${AtmamDatabaseHelper.COL_DUE_DATE} < ? AND ${AtmamDatabaseHelper.COL_IS_ARCHIVED} = 0",
            arrayOf(Task.STATUS_PENDING.toString(), now.toString()),
            null, null,
            "${AtmamDatabaseHelper.COL_DUE_DATE} ASC"
        )
        return cursor.use {
            val tasks = mutableListOf<Task>()
            while (it.moveToNext()) tasks.add(cursorToTask(it))
            tasks
        }
    }

    fun getTasksForToday(): List<Task> {
        val db = dbHelper.readableDatabase
        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val startOfDay = cal.timeInMillis
        val endOfDay = startOfDay + (24 * 60 * 60 * 1000) - 1
        val cursor = db.query(
            AtmamDatabaseHelper.TABLE_TASKS,
            null,
            "${AtmamDatabaseHelper.COL_DELETED_AT} IS NULL AND ${AtmamDatabaseHelper.COL_STATUS} = ? AND ${AtmamDatabaseHelper.COL_DUE_DATE} BETWEEN ? AND ? AND ${AtmamDatabaseHelper.COL_IS_ARCHIVED} = 0",
            arrayOf(Task.STATUS_PENDING.toString(), startOfDay.toString(), endOfDay.toString()),
            null, null,
            "${AtmamDatabaseHelper.COL_PRIORITY} DESC"
        )
        return cursor.use {
            val tasks = mutableListOf<Task>()
            while (it.moveToNext()) tasks.add(cursorToTask(it))
            tasks
        }
    }

    fun getUpcomingTasks(): List<Task> {
        val db = dbHelper.readableDatabase
        val now = System.currentTimeMillis()
        val cursor = db.query(
            AtmamDatabaseHelper.TABLE_TASKS,
            null,
            "${AtmamDatabaseHelper.COL_DELETED_AT} IS NULL AND ${AtmamDatabaseHelper.COL_STATUS} = ? AND ${AtmamDatabaseHelper.COL_DUE_DATE} >= ? AND ${AtmamDatabaseHelper.COL_IS_ARCHIVED} = 0",
            arrayOf(Task.STATUS_PENDING.toString(), now.toString()),
            null, null,
            "${AtmamDatabaseHelper.COL_DUE_DATE} ASC"
        )
        return cursor.use {
            val tasks = mutableListOf<Task>()
            while (it.moveToNext()) tasks.add(cursorToTask(it))
            tasks
        }
    }

    fun getTasksByTag(tag: String): List<Task> {
        val db = dbHelper.readableDatabase
        val cursor = db.query(
            AtmamDatabaseHelper.TABLE_TASKS,
            null,
            "${AtmamDatabaseHelper.COL_DELETED_AT} IS NULL AND ${AtmamDatabaseHelper.COL_TAGS} LIKE ? AND ${AtmamDatabaseHelper.COL_IS_ARCHIVED} = 0",
            arrayOf("%$tag%"),
            null, null,
            "${AtmamDatabaseHelper.COL_CREATED_AT} DESC"
        )
        return cursor.use {
            val tasks = mutableListOf<Task>()
            while (it.moveToNext()) tasks.add(cursorToTask(it))
            tasks
        }
    }

    fun getAllTags(): List<String> {
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery(
            "SELECT DISTINCT ${AtmamDatabaseHelper.COL_TAGS} FROM ${AtmamDatabaseHelper.TABLE_TASKS} WHERE ${AtmamDatabaseHelper.COL_DELETED_AT} IS NULL AND ${AtmamDatabaseHelper.COL_TAGS} != ''",
            null
        )
        val tags = mutableSetOf<String>()
        cursor.use {
            while (it.moveToNext()) {
                val tagString = it.getString(0)
                tagString.split(",").map { t -> t.trim() }.filter { t -> t.isNotEmpty() }.forEach { tag ->
                    tags.add(tag)
                }
            }
        }
        return tags.sorted()
    }

    // ==================== SMART OPERATIONS ====================

    fun completeTask(id: Long): Boolean {
        val db = dbHelper.writableDatabase
        db.beginTransaction()
        try {
            val task = getById(id) ?: return false
            val values = ContentValues().apply {
                put(AtmamDatabaseHelper.COL_STATUS, Task.STATUS_COMPLETED)
                put(AtmamDatabaseHelper.COL_COMPLETED_AT, System.currentTimeMillis())
                put(AtmamDatabaseHelper.COL_POSTPONE_COUNT, 0)
            }
            db.update(AtmamDatabaseHelper.TABLE_TASKS, values, "${AtmamDatabaseHelper.COL_ID} = ?", arrayOf(id.toString()))
            if (task.repeatType != Task.REPEAT_NONE) createNextRepeat(task)
            db.setTransactionSuccessful()
            return true
        } finally {
            db.endTransaction()
        }
    }

    fun failTask(id: Long): Boolean {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put(AtmamDatabaseHelper.COL_STATUS, Task.STATUS_FAILED)
            put(AtmamDatabaseHelper.COL_FAILED_AT, System.currentTimeMillis())
        }
        return db.update(AtmamDatabaseHelper.TABLE_TASKS, values, "${AtmamDatabaseHelper.COL_ID} = ?", arrayOf(id.toString())) > 0
    }

    fun postponeTask(id: Long): Boolean {
        val task = getById(id) ?: return false
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put(AtmamDatabaseHelper.COL_POSTPONE_COUNT, task.postponeCount + 1)
            val nextDate = calculateNextRepeatDate(task)
            if (nextDate != null) put(AtmamDatabaseHelper.COL_DUE_DATE, nextDate)
        }
        return db.update(AtmamDatabaseHelper.TABLE_TASKS, values, "${AtmamDatabaseHelper.COL_ID} = ?", arrayOf(id.toString())) > 0
    }

    fun retryFailedTask(id: Long): Boolean {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put(AtmamDatabaseHelper.COL_STATUS, Task.STATUS_PENDING)
            putNull(AtmamDatabaseHelper.COL_FAILED_AT)
            put(AtmamDatabaseHelper.COL_POSTPONE_COUNT, 0)
        }
        return db.update(AtmamDatabaseHelper.TABLE_TASKS, values, "${AtmamDatabaseHelper.COL_ID} = ?", arrayOf(id.toString())) > 0
    }

    fun emptyTrash(): Int {
        return dbHelper.writableDatabase.delete(
            AtmamDatabaseHelper.TABLE_TASKS,
            "${AtmamDatabaseHelper.COL_DELETED_AT} IS NOT NULL",
            null
        )
    }

    // ==================== REPEAT LOGIC ====================

    private fun createNextRepeat(task: Task) {
        val nextDate = calculateNextRepeatDate(task) ?: return
        val newTask = task.copy(
            id = 0, status = Task.STATUS_PENDING, postponeCount = 0,
            completedAt = null, failedAt = null,
            createdAt = System.currentTimeMillis(), dueDate = nextDate,
            reminderTime = if (task.reminderTime != null && task.dueDate != null) {
                task.reminderTime - task.dueDate + nextDate
            } else null
        )
        insert(newTask)
    }

    private fun calculateNextRepeatDate(task: Task): Long? {
        if (task.dueDate == null) return null
        val cal = Calendar.getInstance().apply { timeInMillis = task.dueDate }
        return when (task.repeatType) {
            Task.REPEAT_DAILY -> { cal.add(Calendar.DAY_OF_MONTH, task.repeatInterval); cal.timeInMillis }
            Task.REPEAT_WEEKLY -> { cal.add(Calendar.WEEK_OF_YEAR, task.repeatInterval); cal.timeInMillis }
            Task.REPEAT_MONTHLY -> { cal.add(Calendar.MONTH, task.repeatInterval); cal.timeInMillis }
            Task.REPEAT_YEARLY -> { cal.add(Calendar.YEAR, task.repeatInterval); cal.timeInMillis }
            Task.REPEAT_CUSTOM -> { cal.add(Calendar.DAY_OF_MONTH, task.repeatInterval); cal.timeInMillis }
            else -> null
        }
    }

    // ==================== STATISTICS ====================

    fun getStatistics(): Statistics {
        val db = dbHelper.readableDatabase
        val totalCursor = db.rawQuery(
            "SELECT COUNT(*), SUM(CASE WHEN ${AtmamDatabaseHelper.COL_STATUS} = ${Task.STATUS_COMPLETED} THEN 1 ELSE 0 END), SUM(CASE WHEN ${AtmamDatabaseHelper.COL_STATUS} = ${Task.STATUS_FAILED} THEN 1 ELSE 0 END), SUM(CASE WHEN ${AtmamDatabaseHelper.COL_POSTPONE_COUNT} > 0 THEN 1 ELSE 0 END) FROM ${AtmamDatabaseHelper.TABLE_TASKS} WHERE ${AtmamDatabaseHelper.COL_DELETED_AT} IS NULL",
            null
        )
        var total = 0; var completed = 0; var failed = 0; var postponed = 0
        totalCursor.use { if (it.moveToFirst()) { total = it.getInt(0); completed = it.getInt(1); failed = it.getInt(2); postponed = it.getInt(3) } }

        val priorityCursor = db.rawQuery("SELECT ${AtmamDatabaseHelper.COL_PRIORITY}, COUNT(*) FROM ${AtmamDatabaseHelper.TABLE_TASKS} WHERE ${AtmamDatabaseHelper.COL_DELETED_AT} IS NULL GROUP BY ${AtmamDatabaseHelper.COL_PRIORITY}", null)
        val byPriority = mutableMapOf<Int, Int>()
        priorityCursor.use { while (it.moveToNext()) byPriority[it.getInt(0)] = it.getInt(1) }

        val streak = calculateCurrentStreak(db)
        return Statistics(
            totalTasks = total, completedTasks = completed, failedTasks = failed, postponedTasks = postponed,
            completionRate = if (total > 0) (completed.toFloat() / total * 100) else 0f,
            currentStreak = streak.first, bestStreak = streak.second, overdueTasks = getOverdueTasks().size,
            tasksByPriority = byPriority
        )
    }

    private fun calculateCurrentStreak(db: SQLiteDatabase): Pair<Int, Int> {
        val cursor = db.rawQuery(
            "SELECT ${AtmamDatabaseHelper.COL_COMPLETED_AT} FROM ${AtmamDatabaseHelper.TABLE_TASKS} WHERE ${AtmamDatabaseHelper.COL_STATUS} = ${Task.STATUS_COMPLETED} AND ${AtmamDatabaseHelper.COL_COMPLETED_AT} IS NOT NULL ORDER BY ${AtmamDatabaseHelper.COL_COMPLETED_AT} DESC", null
        )
        val completionDates = mutableListOf<Long>()
        cursor.use { while (it.moveToNext()) completionDates.add(it.getLong(0)) }
        if (completionDates.isEmpty()) return Pair(0, 0)

        val completedDays = completionDates.map { d -> Calendar.getInstance().apply { timeInMillis = d }.let { it.get(Calendar.YEAR) * 10000 + (it.get(Calendar.MONTH) + 1) * 100 + it.get(Calendar.DAY_OF_MONTH) } }.distinct().sortedDescending()
        val today = Calendar.getInstance().let { it.get(Calendar.YEAR) * 10000 + (it.get(Calendar.MONTH) + 1) * 100 + it.get(Calendar.DAY_OF_MONTH) }
        val yesterday = today - 1

        var currentStreak = 0
        if (completedDays.isNotEmpty() && (completedDays[0] == today || completedDays[0] == yesterday)) {
            currentStreak = 1
            for (i in 1 until completedDays.size) { if (completedDays[i] == completedDays[i-1] - 1) currentStreak++ else break }
        }

        var bestStreak = 1; var tempStreak = 1
        for (i in 1 until completedDays.size) { if (completedDays[i] == completedDays[i-1] - 1) { tempStreak++; if (tempStreak > bestStreak) bestStreak = tempStreak } else tempStreak = 1 }
        return Pair(currentStreak, bestStreak)
    }
}
