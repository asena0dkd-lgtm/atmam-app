package com.atmam.app.ui.notes

import android.os.Bundle
import android.view.MenuItem
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.atmam.app.AtmamApp
import com.atmam.app.R
import com.atmam.app.databinding.ActivityNoteEditBinding
import com.atmam.app.models.Note

class NoteEditActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNoteEditBinding
    private val noteDao by lazy { AtmamApp.getInstance().noteDao }
    private var noteId: Long = 0
    private var isNew: Boolean = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNoteEditBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        noteId = intent.getLongExtra("note_id", 0)
        isNew = intent.getBooleanExtra("is_new", true)
        supportActionBar?.title = if (isNew) getString(R.string.title_note_new) else getString(R.string.title_note_edit)

        setupFolderSpinner()
        setupSaveButton()
        setupConvertButton()
        if (!isNew && noteId > 0) loadNote(noteId)
    }

    private fun setupFolderSpinner() {
        val folders = noteDao.getAllFolders().toMutableList().apply { add("+ مجلد جديد") }
        binding.spinnerFolder.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, folders).apply { setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }
    }

    private fun setupSaveButton() { binding.btnSave.setOnClickListener { saveNote() } }
    private fun setupConvertButton() {
        binding.btnConvertToTask.setOnClickListener {
            if (isNew) { Toast.makeText(this, "احفظ الملاحظة أولاً", Toast.LENGTH_SHORT).show(); return@setOnClickListener }
            AlertDialog.Builder(this).setTitle("تحويل لمهمة").setMessage("هل تريد تحويل هذه الملاحظة لمهمة؟")
                .setPositiveButton("تحويل") { _, _ -> AtmamApp.getInstance().taskDao.insert(noteDao.getById(noteId)!!.toTask()); Toast.makeText(this, "تم التحويل لمهمة!", Toast.LENGTH_SHORT).show() }
                .setNegativeButton("إلغاء", null).show()
        }
    }

    private fun loadNote(id: Long) {
        val note = noteDao.getById(id) ?: return
        binding.etTitle.setText(note.title)
        binding.etContent.setText(note.content)
        binding.etTags.setText(note.tags)
        binding.switchLock.isChecked = note.isLocked
        note.passwordHash?.let { binding.etPassword.setText(it) }
        val folderIndex = noteDao.getAllFolders().indexOf(note.folder)
        if (folderIndex >= 0) binding.spinnerFolder.setSelection(folderIndex)
    }

    private fun saveNote() {
        val title = binding.etTitle.text.toString().trim()
        if (title.isEmpty()) { binding.etTitle.error = getString(R.string.msg_empty_title); return }
        var folder = binding.spinnerFolder.selectedItem?.toString() ?: "عام"
        if (folder == "+ مجلد جديد") {
            val editText = android.widget.EditText(this).apply { hint = "اسم المجلد" }
            AlertDialog.Builder(this).setTitle("مجلد جديد").setView(editText)
                .setPositiveButton("إنشاء") { _, _ -> folder = editText.text.toString().ifEmpty { "عام" }; saveNoteInternal(title, folder) }
                .setNegativeButton("إلغاء", null).show()
            return
        }
        saveNoteInternal(title, folder)
    }

    private fun saveNoteInternal(title: String, folder: String) {
        val isLocked = binding.switchLock.isChecked
        val passwordHash = if (isLocked) binding.etPassword.text.toString() else null
        val note = Note(id = noteId, title = title, content = binding.etContent.text.toString().trim(),
            folder = folder, tags = binding.etTags.text.toString().trim(), isLocked = isLocked, passwordHash = passwordHash)
        if (isNew) noteDao.insert(note) else noteDao.update(note)
        Toast.makeText(this, R.string.msg_note_saved, Toast.LENGTH_SHORT).show()
        finish()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if (item.itemId == android.R.id.home) { finish(); true } else super.onOptionsItemSelected(item)
    }
}
