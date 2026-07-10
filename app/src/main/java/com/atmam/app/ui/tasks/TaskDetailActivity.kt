package com.atmam.app.ui.tasks

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.atmam.app.AtmamApp
import com.atmam.app.R
import com.atmam.app.databinding.ActivityTaskDetailBinding
import com.atmam.app.models.Task
import com.atmam.app.utils.DateUtils
import com.atmam.app.utils.NotificationHelper

class TaskDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTaskDetailBinding
    private val taskDao by lazy { AtmamApp.getInstance().taskDao }
    private var taskId: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTaskDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        taskId = intent.getLongExtra("task_id", 0)
        if (taskId == 0L) { finish(); return }
        loadTask()
    }

    private fun loadTask() {
        val task = taskDao.getById(taskId) ?: run { finish(); return }

        binding.tvTitle.text = task.title
        binding.tvDescription.text = task.description.ifEmpty { "لا يوجد وصف" }
        binding.tvPriority.text = Task.getPriorityLabel(task.priority)
        binding.tvEnergy.text = Task.getEnergyLabel(task.energyLevel)
        binding.tvDueDate.text = task.dueDate?.let { DateUtils.formatDateTime(it) } ?: "غير محدد"
        binding.tvEstimatedTime.text = if (task.estimatedMinutes > 0) "${task.estimatedMinutes} دقيقة" else "غير محدد"
        binding.tvRepeat.text = Task.getRepeatLabel(task.repeatType)
        binding.tvPostponeCount.text = if (task.postponeCount > 0) "${task.postponeCount} مرة" else "لا يوجد"
        binding.tvTags.text = task.tags.ifEmpty { "لا يوجد" }
        binding.tvCreatedAt.text = DateUtils.formatDateTime(task.createdAt)

        val priorityColor = when (task.priority) {
            Task.PRIORITY_URGENT -> getColor(R.color.priority_urgent)
            Task.PRIORITY_IMPORTANT -> getColor(R.color.priority_important)
            else -> getColor(R.color.priority_normal)
        }
        binding.tvPriority.setBackgroundColor(priorityColor)
        binding.tvPriority.setTextColor(getColor(android.R.color.white))
        if (task.postponeCount > 0) binding.tvPostponeCount.setTextColor(android.graphics.Color.parseColor(task.getPostponeColor()))

        if (task.status == Task.STATUS_COMPLETED || task.status == Task.STATUS_FAILED) {
            binding.btnComplete.visibility = android.view.View.GONE
            binding.btnFail.visibility = android.view.View.GONE
        }

        binding.btnComplete.setOnClickListener {
            AlertDialog.Builder(this).setTitle("إتمام المهمة").setMessage("هل أنت متأكد من إتمام هذه المهمة؟")
                .setPositiveButton("نعم") { _, _ -> taskDao.completeTask(task.id); Toast.makeText(this, "تم إتمام المهمة!", Toast.LENGTH_SHORT).show(); finish() }
                .setNegativeButton("إلغاء", null).show()
        }
        binding.btnFail.setOnClickListener {
            AlertDialog.Builder(this).setTitle("إسقاط المهمة").setMessage("هل أنت متأكد من إسقاط هذه المهمة؟")
                .setPositiveButton("نعم") { _, _ -> taskDao.failTask(task.id); Toast.makeText(this, "تم إسقاط المهمة", Toast.LENGTH_SHORT).show(); finish() }
                .setNegativeButton("إلغاء", null).show()
        }
        binding.btnPostpone.setOnClickListener { taskDao.postponeTask(task.id); Toast.makeText(this, "تم تأجيل المهمة", Toast.LENGTH_SHORT).show(); loadTask() }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean { menuInflater.inflate(R.menu.menu_task_detail, menu); return true }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> { finish(); true }
            R.id.action_edit -> { startActivity(Intent(this, TaskEditActivity::class.java).apply { putExtra("task_id", taskId); putExtra("is_new", false) }); true }
            R.id.action_delete -> {
                AlertDialog.Builder(this).setTitle(R.string.action_delete).setMessage(R.string.msg_confirm_delete)
                    .setPositiveButton(R.string.action_delete) { _, _ -> NotificationHelper(this).cancelTaskReminder(taskId); taskDao.delete(taskId); Toast.makeText(this, R.string.msg_task_deleted, Toast.LENGTH_SHORT).show(); finish() }
                    .setNegativeButton(R.string.action_cancel, null).show(); true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
    override fun onResume() { super.onResume(); loadTask() }
}
