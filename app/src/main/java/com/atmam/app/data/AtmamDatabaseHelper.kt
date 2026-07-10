package com.atmam.app.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.atmam.app.models.Attachment
import com.atmam.app.models.Note
import com.atmam.app.models.Task

class AtmamDatabaseHelper(context: Context) : SQLiteOpenHelper(
    context,
    DATABASE_NAME,
    null,
    DATABASE_VERSION
) {

    companion object {
        const val DATABASE_NAME = "atmam.db"
        const val DATABASE_VERSION = 1

        // Tables
        const val TABLE_TASKS = "tasks"
        const val TABLE_NOTES = "notes"
        const val TABLE_ATTACHMENTS = "attachments"
        const val TABLE_STATS = "statistics"

        // Common columns
        const val COL_ID = "id"
        const val COL_CREATED_AT = "created_at"
        const val COL_DELETED_AT = "deleted_at"

        // Tasks columns
        const val COL_TITLE = "title"
        const val COL_DESCRIPTION = "description"
        const val COL_PRIORITY = "priority"
        const val COL_ENERGY_LEVEL = "energy_level"
        const val COL_ESTIMATED_MINUTES = "estimated_minutes"
        const val COL_DUE_DATE = "due_date"
        const val COL_REMINDER_TIME = "reminder_time"
        const val COL_REPEAT_TYPE = "repeat_type"
        const val COL_REPEAT_INTERVAL = "repeat_interval"
        const val COL_REPEAT_DAYS_OF_WEEK = "repeat_days_of_week"
        const val COL_TAGS = "tags"
        const val COL_STATUS = "status"
        const val COL_POSTPONE_COUNT = "postpone_count"
        const val COL_REPEAT_MODE = "repeat_mode"
        const val COL_COMPLETED_AT = "completed_at"
        const val COL_FAILED_AT = "failed_at"
        const val COL_NEXT_REPEAT_DATE = "next_repeat_date"
        const val COL_IS_ARCHIVED = "is_archived"

        // Notes columns
        const val COL_CONTENT = "content"
        const val COL_FOLDER = "folder"
        const val COL_IS_LOCKED = "is_locked"
        const val COL_PASSWORD_HASH = "password_hash"
        const val COL_MOOD = "mood"
        const val COL_IS_DAILY_JOURNAL = "is_daily_journal"
        const val COL_JOURNAL_DATE = "journal_date"
        const val COL_UPDATED_AT = "updated_at"

        // Attachments columns
        const val COL_TASK_ID = "task_id"
        const val COL_NOTE_ID = "note_id"
        const val COL_TYPE = "type"
        const val COL_FILE_PATH = "file_path"
        const val COL_FILE_NAME = "file_name"
        const val COL_MIME_TYPE = "mime_type"
        const val COL_FILE_SIZE = "file_size"

        // Singleton pattern
        @Volatile
        private var instance: AtmamDatabaseHelper? = null

        fun getInstance(context: Context): AtmamDatabaseHelper {
            return instance ?: synchronized(this) {
                instance ?: AtmamDatabaseHelper(context.applicationContext).also {
                    instance = it
                }
            }
        }
    }

    override fun onCreate(db: SQLiteDatabase) {
        // Tasks table
        db.execSQL("""
            CREATE TABLE $TABLE_TASKS (
                $COL_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_TITLE TEXT NOT NULL,
                $COL_DESCRIPTION TEXT DEFAULT '',
                $COL_PRIORITY INTEGER DEFAULT ${Task.PRIORITY_NORMAL},
                $COL_ENERGY_LEVEL INTEGER DEFAULT ${Task.ENERGY_MEDIUM},
                $COL_ESTIMATED_MINUTES INTEGER DEFAULT 0,
                $COL_DUE_DATE INTEGER,
                $COL_REMINDER_TIME INTEGER,
                $COL_REPEAT_TYPE INTEGER DEFAULT ${Task.REPEAT_NONE},
                $COL_REPEAT_INTERVAL INTEGER DEFAULT 1,
                $COL_REPEAT_DAYS_OF_WEEK TEXT,
                $COL_TAGS TEXT DEFAULT '',
                $COL_STATUS INTEGER DEFAULT ${Task.STATUS_PENDING},
                $COL_POSTPONE_COUNT INTEGER DEFAULT 0,
                $COL_REPEAT_MODE INTEGER DEFAULT ${Task.REPEAT_ON_COMPLETION},
                $COL_CREATED_AT INTEGER DEFAULT (strftime('%s', 'now') * 1000),
                $COL_COMPLETED_AT INTEGER,
                $COL_FAILED_AT INTEGER,
                $COL_NEXT_REPEAT_DATE INTEGER,
                $COL_IS_ARCHIVED INTEGER DEFAULT 0,
                $COL_DELETED_AT INTEGER
            )
        """)

        // Notes table
        db.execSQL("""
            CREATE TABLE $TABLE_NOTES (
                $COL_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_TITLE TEXT NOT NULL,
                $COL_CONTENT TEXT DEFAULT '',
                $COL_FOLDER TEXT DEFAULT 'عام',
                $COL_TAGS TEXT DEFAULT '',
                $COL_IS_LOCKED INTEGER DEFAULT 0,
                $COL_PASSWORD_HASH TEXT,
                $COL_MOOD INTEGER,
                $COL_IS_DAILY_JOURNAL INTEGER DEFAULT 0,
                $COL_JOURNAL_DATE INTEGER,
                $COL_CREATED_AT INTEGER DEFAULT (strftime('%s', 'now') * 1000),
                $COL_UPDATED_AT INTEGER DEFAULT (strftime('%s', 'now') * 1000),
                $COL_DELETED_AT INTEGER
            )
        """)

        // Attachments table
        db.execSQL("""
            CREATE TABLE $TABLE_ATTACHMENTS (
                $COL_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_TASK_ID INTEGER,
                $COL_NOTE_ID INTEGER,
                $COL_TYPE INTEGER DEFAULT ${Attachment.TYPE_FILE},
                $COL_FILE_PATH TEXT NOT NULL,
                $COL_FILE_NAME TEXT NOT NULL,
                $COL_MIME_TYPE TEXT DEFAULT '',
                $COL_FILE_SIZE INTEGER DEFAULT 0,
                $COL_CREATED_AT INTEGER DEFAULT (strftime('%s', 'now') * 1000),
                FOREIGN KEY ($COL_TASK_ID) REFERENCES $TABLE_TASKS($COL_ID) ON DELETE CASCADE,
                FOREIGN KEY ($COL_NOTE_ID) REFERENCES $TABLE_NOTES($COL_ID) ON DELETE CASCADE
            )
        """)

        // Indexes for performance
        db.execSQL("CREATE INDEX idx_tasks_status ON $TABLE_TASKS($COL_STATUS)")
        db.execSQL("CREATE INDEX idx_tasks_due_date ON $TABLE_TASKS($COL_DUE_DATE)")
        db.execSQL("CREATE INDEX idx_tasks_repeat ON $TABLE_TASKS($COL_REPEAT_TYPE)")
        db.execSQL("CREATE INDEX idx_notes_folder ON $TABLE_NOTES($COL_FOLDER)")
        db.execSQL("CREATE INDEX idx_notes_journal ON $TABLE_NOTES($COL_IS_DAILY_JOURNAL, $COL_JOURNAL_DATE)")
        db.execSQL("CREATE INDEX idx_attachments_task ON $TABLE_ATTACHMENTS($COL_TASK_ID)")
        db.execSQL("CREATE INDEX idx_attachments_note ON $TABLE_ATTACHMENTS($COL_NOTE_ID)")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // Future migrations go here
    }

    override fun onConfigure(db: SQLiteDatabase) {
        super.onConfigure(db)
        db.setForeignKeyConstraintsEnabled(true)
    }

    // ==================== CONVERSION HELPERS ====================

    fun taskToContentValues(task: Task): ContentValues {
        return ContentValues().apply {
            if (task.id > 0) put(COL_ID, task.id)
            put(COL_TITLE, task.title)
            put(COL_DESCRIPTION, task.description)
            put(COL_PRIORITY, task.priority)
            put(COL_ENERGY_LEVEL, task.energyLevel)
            put(COL_ESTIMATED_MINUTES, task.estimatedMinutes)
            put(COL_DUE_DATE, task.dueDate)
            put(COL_REMINDER_TIME, task.reminderTime)
            put(COL_REPEAT_TYPE, task.repeatType)
            put(COL_REPEAT_INTERVAL, task.repeatInterval)
            put(COL_REPEAT_DAYS_OF_WEEK, task.repeatDaysOfWeek)
            put(COL_TAGS, task.tags)
            put(COL_STATUS, task.status)
            put(COL_POSTPONE_COUNT, task.postponeCount)
            put(COL_REPEAT_MODE, task.repeatMode)
            put(COL_CREATED_AT, task.createdAt)
            put(COL_COMPLETED_AT, task.completedAt)
            put(COL_FAILED_AT, task.failedAt)
            put(COL_NEXT_REPEAT_DATE, task.nextRepeatDate)
            put(COL_IS_ARCHIVED, if (task.isArchived) 1 else 0)
            put(COL_DELETED_AT, task.deletedAt)
        }
    }

    fun noteToContentValues(note: Note): ContentValues {
        return ContentValues().apply {
            if (note.id > 0) put(COL_ID, note.id)
            put(COL_TITLE, note.title)
            put(COL_CONTENT, note.content)
            put(COL_FOLDER, note.folder)
            put(COL_TAGS, note.tags)
            put(COL_IS_LOCKED, if (note.isLocked) 1 else 0)
            put(COL_PASSWORD_HASH, note.passwordHash)
            put(COL_MOOD, note.mood)
            put(COL_IS_DAILY_JOURNAL, if (note.isDailyJournal) 1 else 0)
            put(COL_JOURNAL_DATE, note.journalDate)
            put(COL_CREATED_AT, note.createdAt)
            put(COL_UPDATED_AT, System.currentTimeMillis())
            put(COL_DELETED_AT, note.deletedAt)
        }
    }

    fun attachmentToContentValues(attachment: Attachment): ContentValues {
        return ContentValues().apply {
            if (attachment.id > 0) put(COL_ID, attachment.id)
            put(COL_TASK_ID, attachment.taskId)
            put(COL_NOTE_ID, attachment.noteId)
            put(COL_TYPE, attachment.type)
            put(COL_FILE_PATH, attachment.filePath)
            put(COL_FILE_NAME, attachment.fileName)
            put(COL_MIME_TYPE, attachment.mimeType)
            put(COL_FILE_SIZE, attachment.fileSize)
            put(COL_CREATED_AT, attachment.createdAt)
        }
    }
}
