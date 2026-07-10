package com.atmam.app.data

import android.content.ContentValues
import android.database.Cursor
import com.atmam.app.models.Note
import java.util.Calendar

class NoteDao(private val dbHelper: AtmamDatabaseHelper) {

    private fun cursorToNote(cursor: Cursor): Note {
        return Note(
            id = cursor.getLong(cursor.getColumnIndexOrThrow(AtmamDatabaseHelper.COL_ID)),
            title = cursor.getString(cursor.getColumnIndexOrThrow(AtmamDatabaseHelper.COL_TITLE)),
            content = cursor.getString(cursor.getColumnIndexOrThrow(AtmamDatabaseHelper.COL_CONTENT)) ?: "",
            folder = cursor.getString(cursor.getColumnIndexOrThrow(AtmamDatabaseHelper.COL_FOLDER)) ?: "عام",
            tags = cursor.getString(cursor.getColumnIndexOrThrow(AtmamDatabaseHelper.COL_TAGS)) ?: "",
            isLocked = cursor.getInt(cursor.getColumnIndexOrThrow(AtmamDatabaseHelper.COL_IS_LOCKED)) == 1,
            passwordHash = cursor.getString(cursor.getColumnIndexOrThrow(AtmamDatabaseHelper.COL_PASSWORD_HASH)),
            mood = cursor.getIntOrNull(cursor.getColumnIndexOrThrow(AtmamDatabaseHelper.COL_MOOD)),
            isDailyJournal = cursor.getInt(cursor.getColumnIndexOrThrow(AtmamDatabaseHelper.COL_IS_DAILY_JOURNAL)) == 1,
            journalDate = cursor.getLongOrNull(cursor.getColumnIndexOrThrow(AtmamDatabaseHelper.COL_JOURNAL_DATE)),
            createdAt = cursor.getLong(cursor.getColumnIndexOrThrow(AtmamDatabaseHelper.COL_CREATED_AT)),
            updatedAt = cursor.getLong(cursor.getColumnIndexOrThrow(AtmamDatabaseHelper.COL_UPDATED_AT)),
            deletedAt = cursor.getLongOrNull(cursor.getColumnIndexOrThrow(AtmamDatabaseHelper.COL_DELETED_AT))
        )
    }

    private fun Cursor.getLongOrNull(columnIndex: Int): Long? = if (isNull(columnIndex)) null else getLong(columnIndex)
    private fun Cursor.getIntOrNull(columnIndex: Int): Int? = if (isNull(columnIndex)) null else getInt(columnIndex)

    // ==================== CRUD ====================

    fun insert(note: Note): Long {
        return dbHelper.writableDatabase.insert(AtmamDatabaseHelper.TABLE_NOTES, null, dbHelper.noteToContentValues(note))
    }

    fun update(note: Note): Int {
        return dbHelper.writableDatabase.update(AtmamDatabaseHelper.TABLE_NOTES, dbHelper.noteToContentValues(note), "${AtmamDatabaseHelper.COL_ID} = ?", arrayOf(note.id.toString()))
    }

    fun delete(id: Long): Int {
        val values = ContentValues().apply { put(AtmamDatabaseHelper.COL_DELETED_AT, System.currentTimeMillis()) }
        return dbHelper.writableDatabase.update(AtmamDatabaseHelper.TABLE_NOTES, values, "${AtmamDatabaseHelper.COL_ID} = ?", arrayOf(id.toString()))
    }

    fun getById(id: Long): Note? {
        val cursor = dbHelper.readableDatabase.query(AtmamDatabaseHelper.TABLE_NOTES, null, "${AtmamDatabaseHelper.COL_ID} = ? AND ${AtmamDatabaseHelper.COL_DELETED_AT} IS NULL", arrayOf(id.toString()), null, null, null)
        return cursor.use { if (it.moveToFirst()) cursorToNote(it) else null }
    }

    // ==================== LIST QUERIES ====================

    fun getAll(): List<Note> {
        val cursor = dbHelper.readableDatabase.query(AtmamDatabaseHelper.TABLE_NOTES, null, "${AtmamDatabaseHelper.COL_DELETED_AT} IS NULL AND ${AtmamDatabaseHelper.COL_IS_DAILY_JOURNAL} = 0", null, null, null, "${AtmamDatabaseHelper.COL_UPDATED_AT} DESC")
        return cursor.use { val notes = mutableListOf<Note>(); while (it.moveToNext()) notes.add(cursorToNote(it)); notes }
    }

    fun getByFolder(folder: String): List<Note> {
        val cursor = dbHelper.readableDatabase.query(AtmamDatabaseHelper.TABLE_NOTES, null, "${AtmamDatabaseHelper.COL_DELETED_AT} IS NULL AND ${AtmamDatabaseHelper.COL_FOLDER} = ? AND ${AtmamDatabaseHelper.COL_IS_DAILY_JOURNAL} = 0", arrayOf(folder), null, null, "${AtmamDatabaseHelper.COL_UPDATED_AT} DESC")
        return cursor.use { val notes = mutableListOf<Note>(); while (it.moveToNext()) notes.add(cursorToNote(it)); notes }
    }

    fun getByTag(tag: String): List<Note> {
        val cursor = dbHelper.readableDatabase.query(AtmamDatabaseHelper.TABLE_NOTES, null, "${AtmamDatabaseHelper.COL_DELETED_AT} IS NULL AND ${AtmamDatabaseHelper.COL_TAGS} LIKE ? AND ${AtmamDatabaseHelper.COL_IS_DAILY_JOURNAL} = 0", arrayOf("%$tag%"), null, null, "${AtmamDatabaseHelper.COL_UPDATED_AT} DESC")
        return cursor.use { val notes = mutableListOf<Note>(); while (it.moveToNext()) notes.add(cursorToNote(it)); notes }
    }

    fun search(query: String): List<Note> {
        val likeQuery = "%$query%"
        val cursor = dbHelper.readableDatabase.query(AtmamDatabaseHelper.TABLE_NOTES, null, "${AtmamDatabaseHelper.COL_DELETED_AT} IS NULL AND (${AtmamDatabaseHelper.COL_TITLE} LIKE ? OR ${AtmamDatabaseHelper.COL_CONTENT} LIKE ?) AND ${AtmamDatabaseHelper.COL_IS_DAILY_JOURNAL} = 0", arrayOf(likeQuery, likeQuery), null, null, "${AtmamDatabaseHelper.COL_UPDATED_AT} DESC")
        return cursor.use { val notes = mutableListOf<Note>(); while (it.moveToNext()) notes.add(cursorToNote(it)); notes }
    }

    // ==================== DAILY JOURNAL ====================

    fun getOrCreateDailyJournal(): Note {
        val cal = Calendar.getInstance().apply { set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0) }
        val startOfDay = cal.timeInMillis
        val endOfDay = startOfDay + (24 * 60 * 60 * 1000) - 1
        val cursor = dbHelper.readableDatabase.query(AtmamDatabaseHelper.TABLE_NOTES, null, "${AtmamDatabaseHelper.COL_IS_DAILY_JOURNAL} = 1 AND ${AtmamDatabaseHelper.COL_JOURNAL_DATE} BETWEEN ? AND ? AND ${AtmamDatabaseHelper.COL_DELETED_AT} IS NULL", arrayOf(startOfDay.toString(), endOfDay.toString()), null, null, null)
        val existing = cursor.use { if (it.moveToFirst()) cursorToNote(it) else null }
        return existing ?: run {
            val today = Calendar.getInstance()
            val dateStr = "${today.get(Calendar.DAY_OF_MONTH)}/${today.get(Calendar.MONTH) + 1}/${today.get(Calendar.YEAR)}"
            val newNote = Note(title = "مذكرة يوم $dateStr", content = "", isDailyJournal = true, journalDate = startOfDay)
            val id = insert(newNote)
            newNote.copy(id = id)
        }
    }

    // ==================== FOLDERS & TAGS ====================

    fun getAllFolders(): List<String> {
        val cursor = dbHelper.readableDatabase.rawQuery("SELECT DISTINCT ${AtmamDatabaseHelper.COL_FOLDER} FROM ${AtmamDatabaseHelper.TABLE_NOTES} WHERE ${AtmamDatabaseHelper.COL_DELETED_AT} IS NULL AND ${AtmamDatabaseHelper.COL_IS_DAILY_JOURNAL} = 0 ORDER BY ${AtmamDatabaseHelper.COL_FOLDER}", null)
        val folders = mutableListOf<String>()
        cursor.use { while (it.moveToNext()) folders.add(it.getString(0)) }
        if (folders.isEmpty()) folders.add("عام")
        return folders
    }

    fun getAllTags(): List<String> {
        val cursor = dbHelper.readableDatabase.rawQuery("SELECT DISTINCT ${AtmamDatabaseHelper.COL_TAGS} FROM ${AtmamDatabaseHelper.TABLE_NOTES} WHERE ${AtmamDatabaseHelper.COL_DELETED_AT} IS NULL AND ${AtmamDatabaseHelper.COL_TAGS} != '' AND ${AtmamDatabaseHelper.COL_IS_DAILY_JOURNAL} = 0", null)
        val tags = mutableSetOf<String>()
        cursor.use { while (it.moveToNext()) it.getString(0).split(",").map { t -> t.trim() }.filter { t -> t.isNotEmpty() }.forEach { tag -> tags.add(tag) } }
        return tags.sorted()
    }

    // ==================== TRASH ====================

    fun emptyTrash(): Int {
        return dbHelper.writableDatabase.delete(AtmamDatabaseHelper.TABLE_NOTES, "${AtmamDatabaseHelper.COL_DELETED_AT} IS NOT NULL", null)
    }
}
