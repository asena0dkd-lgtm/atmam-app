package com.atmam.app.ui.notes

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.atmam.app.AtmamApp
import com.atmam.app.R
import com.atmam.app.databinding.ActivityNoteDetailBinding
import com.atmam.app.utils.DateUtils

class NoteDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNoteDetailBinding
    private val noteDao by lazy { AtmamApp.getInstance().noteDao }
    private var noteId: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNoteDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        noteId = intent.getLongExtra("note_id", 0)
        if (noteId == 0L) { finish(); return }
        loadNote()
    }

    private fun loadNote() {
        val note = noteDao.getById(noteId) ?: run { finish(); return }
        supportActionBar?.title = note.title
        binding.tvContent.text = note.content.ifEmpty { "لا يوجد محتوى" }
        binding.tvFolder.text = "المجلد: ${note.folder}"
        binding.tvTags.text = if (note.tags.isNotEmpty()) "الوسوم: ${note.tags}" else "لا توجد وسوم"
        binding.tvCreatedAt.text = "تاريخ الإنشاء: ${DateUtils.formatDateTime(note.createdAt)}"
        binding.tvUpdatedAt.text = "آخر تحديث: ${DateUtils.formatDateTime(note.updatedAt)}"

        val backlinks = note.findBacklinks(noteDao.getAll())
        if (backlinks.isNotEmpty()) binding.tvBacklinks.text = "روابط مرتبطة: ${backlinks.joinToString(", ") { it.title }}"
        else binding.tvBacklinks.visibility = android.view.View.GONE
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean { menuInflater.inflate(R.menu.menu_note_detail, menu); return true }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> { finish(); true }
            R.id.action_edit -> { startActivity(Intent(this, NoteEditActivity::class.java).apply { putExtra("note_id", noteId); putExtra("is_new", false) }); true }
            R.id.action_convert_to_task -> { AtmamApp.getInstance().taskDao.insert(noteDao.getById(noteId)!!.toTask()); Toast.makeText(this, "تم تحويل الملاحظة لمهمة!", Toast.LENGTH_SHORT).show(); true }
            R.id.action_delete -> {
                AlertDialog.Builder(this).setTitle(R.string.action_delete).setMessage(R.string.msg_confirm_delete)
                    .setPositiveButton(R.string.action_delete) { _, _ -> noteDao.delete(noteId); Toast.makeText(this, R.string.msg_note_deleted, Toast.LENGTH_SHORT).show(); finish() }
                    .setNegativeButton(R.string.action_cancel, null).show(); true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
    override fun onResume() { super.onResume(); loadNote() }
}
