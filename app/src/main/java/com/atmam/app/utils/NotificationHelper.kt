package com.atmam.app.utils

import android.app.AlarmManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.atmam.app.AtmamApp
import com.atmam.app.R
import com.atmam.app.models.Task
import com.atmam.app.ui.tasks.TaskDetailActivity

class NotificationHelper(private val context: Context) {

    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun scheduleTaskReminder(taskId: Long, reminderTime: Long) {
        if (reminderTime <= System.currentTimeMillis()) return
        val intent = Intent(context, TaskReminderReceiver::class.java).apply { putExtra(Constants.EXTRA_TASK_ID, taskId) }
        val pendingIntent = PendingIntent.getBroadcast(context, taskId.toInt(), intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && alarmManager.canScheduleExactAlarms()) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, reminderTime, pendingIntent)
        } else {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, reminderTime, pendingIntent)
        }
    }

    fun cancelTaskReminder(taskId: Long) {
        val intent = Intent(context, TaskReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(context, taskId.toInt(), intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        alarmManager.cancel(pendingIntent)
    }

    fun showTaskNotification(taskId: Long, title: String, message: String) {
        val intent = Intent(context, TaskDetailActivity::class.java).apply { putExtra(Constants.EXTRA_TASK_ID, taskId); flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP }
        val pendingIntent = PendingIntent.getActivity(context, taskId.toInt(), intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        val notification = NotificationCompat.Builder(context, AtmamApp.CHANNEL_REMINDERS)
            .setSmallIcon(R.drawable.ic_notification).setContentTitle(title).setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH).setAutoCancel(true).setContentIntent(pendingIntent)
            .setVibrate(longArrayOf(0, 300, 200, 300)).build()
        notificationManager.notify(taskId.toInt(), notification)
    }

    fun scheduleDailyReminders() {
        AtmamApp.getInstance().taskDao.getAllActive().filter { it.reminderTime != null && it.status == Task.STATUS_PENDING }.forEach { task ->
            task.reminderTime?.let { scheduleTaskReminder(task.id, it) }
        }
    }
}

class TaskReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val taskId = intent.getLongExtra(Constants.EXTRA_TASK_ID, -1)
        if (taskId == -1L) return
        val task = AtmamApp.getInstance().taskDao.getById(taskId) ?: return
        if (task.status == Task.STATUS_PENDING) {
            NotificationHelper(context).showTaskNotification(task.id, "تذكير بمهمة: ${task.title}", task.description.ifEmpty { "لديك مهمة مستحقة!" })
        }
    }
}

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) NotificationHelper(context).scheduleDailyReminders()
    }
}
