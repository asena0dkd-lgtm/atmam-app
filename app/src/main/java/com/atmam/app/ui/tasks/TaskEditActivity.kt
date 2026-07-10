package com.atmam.app.ui.tasks

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.MenuItem
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.atmam.app.AtmamApp
import com.atmam.app.R
import com.atmam.app.databinding.ActivityTaskEditBinding
import com.atmam.app.models.Task
import com.atmam.app.utils.NotificationHelper
import java.util.Calendar

class TaskEditActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTaskEditBinding
    private val taskDao by lazy { AtmamApp.getInstance().taskDao }
    private val notificationHelper by lazy { NotificationHelper(this) }
    private var taskId: Long = 0
    private var isNew: Boolean = true
    private var selectedDueDate: Long? = null
    private var selectedReminderTime: Long? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTaskEditBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        taskId = intent.getLongExtra("task_id", 0)
        isNew = intent.getBooleanExtra("is_new", true)
        supportActionBar?.title = if (isNew) getString(R.string.title_task_new) else getString(R.string.title_task_edit)

        setupSpinners()
        setupDateTimePickers()
        setupSaveButton()
        if (!isNew && taskId > 0) loadTask(taskId)
    }

    private fun setupSpinners() {
        binding.spinnerPriority.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item,
            listOf(getString(R.string.priority_normal), getString(R.string.priority_important), getString(R.string.priority_urgent))).apply { setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }
        binding.spinnerEnergy.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item,
            listOf(getString(R.string.energy_low), getString(R.string.energy_medium), getString(R.string.energy_high))).apply { setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }
        binding.spinnerRepeat.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item,
            listOf(getString(R.string.repeat_none), getString(R.string.repeat_daily), getString(R.string.repeat_weekly), getString(R.string.repeat_monthly), getString(R.string.repeat_yearly))).apply { setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }
        binding.spinnerRepeatMode.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item,
            listOf("متوقف على الإنجاز (لا تتضاعف)", "ثابت (يتكرر بغض النظر)")).apply { setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }
    }

    private fun setupDateTimePickers() {
        binding.btnPickDueDate.setOnClickListener {
            val cal = Calendar.getInstance().apply { selectedDueDate?.let { timeInMillis = it } }
            DatePickerDialog(this, { _, y, m, d ->
                cal.set(y, m, d); selectedDueDate = cal.timeInMillis
                binding.tvDueDate.text = "$d/${m + 1}/$y"
            }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
        }
        binding.btnPickReminder.setOnClickListener {
            if (selectedDueDate == null) { Toast.makeText(this, "اختر الموعد النهائي أولاً", Toast.LENGTH_SHORT).show(); return@setOnClickListener }
            val cal = Calendar.getInstance().apply { timeInMillis = selectedDueDate!! }
            TimePickerDialog(this, { _, h, m -> cal.set(Calendar.HOUR_OF_DAY, h); cal.set(Calendar.MINUTE, m); selectedReminderTime = cal.timeInMillis; binding.tvReminderTime.text = String.format("%02d:%02d", h, m) }, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true).show()
        }
    }

    private fun setupSaveButton() {
        binding.btnSave.setOnClickListener { saveTask() }
    }

    private fun loadTask(id: Long) {
        val task = taskDao.getById(id) ?: return
        binding.etTitle.setText(task.title)
        binding.etDescription.setText(task.description)
        binding.etEstimatedTime.setText(if (task.estimatedMinutes > 0) task.estimatedMinutes.toString() else "")
        binding.etTags.setText(task.tags)
        binding.spinnerPriority.setSelection(when (task.priority) { Task.PRIORITY_IMPORTANT -> 1; Task.PRIORITY_URGENT -> 2; else -> 0 })
        binding.spinnerEnergy.setSelection(when (task.energyLevel) { Task.ENERGY_MEDIUM -> 1; Task.ENERGY_HIGH -> 2; else -> 0 })
        binding.spinnerRepeat.setSelection(task.repeatType)
        binding.spinnerRepeatMode.setSelection(task.repeatMode)
        selectedDueDate = task.dueDate; selectedReminderTime = task.reminderTime
        task.dueDate?.let { val c = Calendar.getInstance().apply { timeInMillis = it }; binding.tvDueDate.text = "${c.get(Calendar.DAY_OF_MONTH)}/${c.get(Calendar.MONTH) + 1}/${c.get(Calendar.YEAR)}" }
        task.reminderTime?.let { val c = Calendar.getInstance().apply { timeInMillis = it }; binding.tvReminderTime.text = String.format("%02d:%02d", c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE)) }
    }

    private fun saveTask() {
        val title = binding.etTitle.text.toString().trim()
        if (title.isEmpty()) { binding.etTitle.error = getString(R.string.msg_empty_title); return }
        val priority = when (binding.spinnerPriority.selectedItemPosition) { 1 -> Task.PRIORITY_IMPORTANT; 2 -> Task.PRIORITY_URGENT; else -> Task.PRIORITY_NORMAL }
        val energy = when (binding.spinnerEnergy.selectedItemPosition) { 1 -> Task.ENERGY_MEDIUM; 2 -> Task.ENERGY_HIGH; else -> Task.ENERGY_LOW }
        val task = Task(id = taskId, title = title, description = binding.etDescription.text.toString().trim(),
            priority = priority, energyLevel = energy, estimatedMinutes = binding.etEstimatedTime.text.toString().toIntOrNull() ?: 0,
            dueDate = selectedDueDate, reminderTime = selectedReminderTime, repeatType = binding.spinnerRepeat.selectedItemPosition,
            tags = binding.etTags.text.toString().trim(), repeatMode = binding.spinnerRepeatMode.selectedItemPosition, status = Task.STATUS_PENDING)
        if (isNew) { val newId = taskDao.insert(task); selectedReminderTime?.let { notificationHelper.scheduleTaskReminder(newId, it) } }
        else { taskDao.update(task); notificationHelper.cancelTaskReminder(task.id); selectedReminderTime?.let { notificationHelper.scheduleTaskReminder(task.id, it) } }
        Toast.makeText(this, R.string.msg_task_saved, Toast.LENGTH_SHORT).show(); finish()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if (item.itemId == android.R.id.home) { finish(); true } else super.onOptionsItemSelected(item)
    }
}
