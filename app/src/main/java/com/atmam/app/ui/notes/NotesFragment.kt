package com.atmam.app.ui.notes

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
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
import com.atmam.app.databinding.FragmentNotesBinding
import com.atmam.app.models.Note

class NotesFragment : Fragment() {

    private var _binding: FragmentNotesBinding? = null
    private val binding get() = _binding!!
    private val noteDao by lazy { AtmamApp.getInstance().noteDao }
    private lateinit var noteAdapter: NoteAdapter
    private var currentFolder: String? = null
    private var searchQuery: String = ""

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentNotesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupSearch()
        setupFab()
        loadFolders()
        loadNotes()
    }

    private fun setupRecyclerView() {
        noteAdapter = NoteAdapter(
            onNoteClick = { note -> openNoteDetail(note) },
            onNoteLongClick = { note -> showNoteOptions(note); true }
        )
        binding.recyclerNotes.apply { layoutManager = LinearLayoutManager(requireContext()); adapter = noteAdapter }
    }

    private fun setupSearch() {
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) { searchQuery = s?.toString() ?: ""; loadNotes() }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun loadFolders() {
        val folders = noteDao.getAllFolders()
        binding.chipGroupFolders.removeAllViews()
        val allChip = com.google.android.material.chip.Chip(requireContext()).apply {
            text = "الكل"; isCheckable = true; isChecked = currentFolder == null
            setOnCheckedChangeListener { _, isChecked -> if (isChecked) { currentFolder = null; loadNotes() } }
        }
        binding.chipGroupFolders.addView(allChip)
        folders.forEach { folder ->
            val chip = com.google.android.material.chip.Chip(requireContext()).apply {
                text = folder; isCheckable = true; isChecked = currentFolder == folder
                setOnCheckedChangeListener { _, isChecked -> if (isChecked) { currentFolder = folder; loadNotes() } }
            }
            binding.chipGroupFolders.addView(chip)
        }
    }

    private fun setupFab() {
        binding.fabAddNote.setOnClickListener {
            startActivity(Intent(requireContext(), NoteEditActivity::class.java).apply { putExtra("is_new", true) })
        }
    }

    private fun loadNotes() {
        val notes = when {
            searchQuery.isNotEmpty() -> noteDao.search(searchQuery)
            currentFolder != null -> noteDao.getByFolder(currentFolder!!)
            else -> noteDao.getAll()
        }
        noteAdapter.submitList(notes)
        binding.tvSubtitle.text = "${notes.size} ملاحظة"
        binding.emptyState.visibility = if (notes.isEmpty()) View.VISIBLE else View.GONE
    }

    private fun openNoteDetail(note: Note) {
        if (note.isLocked) { showPasswordDialog(note); return }
        startActivity(Intent(requireContext(), NoteDetailActivity::class.java).apply { putExtra("note_id", note.id) })
    }

    private fun showPasswordDialog(note: Note) {
        val editText = android.widget.EditText(requireContext()).apply {
            inputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_VARIATION_PASSWORD
            hint = "أدخل الرقم السري"
        }
        AlertDialog.Builder(requireContext()).setTitle("ملاحظة مقفلة").setView(editText)
            .setPositiveButton("فتح") { _, _ ->
                if (editText.text.toString() == note.passwordHash) {
                    startActivity(Intent(requireContext(), NoteDetailActivity::class.java).apply { putExtra("note_id", note.id) })
                } else { Toast.makeText(requireContext(), "رقم سري خاطئ", Toast.LENGTH_SHORT).show() }
            }
            .setNegativeButton("إلغاء", null).show()
    }

    private fun showNoteOptions(note: Note) {
        val popup = PopupMenu(requireContext(), binding.recyclerNotes)
        popup.menuInflater.inflate(R.menu.menu_note_options, popup.menu)
        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_edit -> { startActivity(Intent(requireContext(), NoteEditActivity::class.java).apply { putExtra("note_id", note.id); putExtra("is_new", false) }); true }
                R.id.action_convert_to_task -> { AtmamApp.getInstance().taskDao.insert(note.toTask()); Toast.makeText(requireContext(), "تم تحويل الملاحظة لمهمة", Toast.LENGTH_SHORT).show(); true }
                R.id.action_delete -> {
                    AlertDialog.Builder(requireContext()).setTitle(R.string.action_delete).setMessage(R.string.msg_confirm_delete)
                        .setPositiveButton(R.string.action_delete) { _, _ -> noteDao.delete(note.id); loadNotes(); Toast.makeText(requireContext(), R.string.msg_note_deleted, Toast.LENGTH_SHORT).show() }
                        .setNegativeButton(R.string.action_cancel, null).show(); true
                }
                else -> false
            }
        }
        popup.show()
    }

    override fun onResume() { super.onResume(); loadFolders(); loadNotes() }
    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}
