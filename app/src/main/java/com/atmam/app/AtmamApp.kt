package com.atmam.app

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import com.atmam.app.data.AtmamDatabaseHelper
import com.atmam.app.data.NoteDao
import com.atmam.app.data.TaskDao

class AtmamApp : Application() {

    lateinit var database: AtmamDatabaseHelper
        private set
    lateinit var taskDao: TaskDao
        private set
    lateinit var noteDao: NoteDao
        private set

    companion object {
        const val CHANNEL_REMINDERS = "task_reminders"
        
        private var instance: AtmamApp? = null
        
        fun getInstance(): AtmamApp {
            return instance ?: throw IllegalStateException("Application not initialized")
        }
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        
        database = AtmamDatabaseHelper.getInstance(this)
        taskDao = TaskDao(database)
        noteDao = NoteDao(database)
        
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val remindersChannel = NotificationChannel(
                CHANNEL_REMINDERS,
                "تذكيرات المهام",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "إشعارات لتذكيرك بالمهام القادمة"
                enableVibration(true)
                enableLights(true)
            }

            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(remindersChannel)
        }
    }
}
