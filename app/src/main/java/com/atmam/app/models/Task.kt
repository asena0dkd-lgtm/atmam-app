package com.atmam.app.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Task(
    val id: Long = 0,
    val title: String,
    val description: String = "",
    val priority: Int = PRIORITY_NORMAL,
    val energyLevel: Int = ENERGY_MEDIUM,
    val estimatedMinutes: Int = 0,
    val dueDate: Long? = null,
    val reminderTime: Long? = null,
    val repeatType: Int = REPEAT_NONE,
    val repeatInterval: Int = 1,
    val repeatDaysOfWeek: String? = null,
    val tags: String = "",
    val status: Int = STATUS_PENDING,
    val postponeCount: Int = 0,
    val repeatMode: Int = REPEAT_ON_COMPLETION,
    val createdAt: Long = System.currentTimeMillis(),
    val completedAt: Long? = null,
    val failedAt: Long? = null,
    val nextRepeatDate: Long? = null,
    val isArchived: Boolean = false,
    val deletedAt: Long? = null
) : Parcelable {

    companion object {
        const val PRIORITY_URGENT = 3
        const val PRIORITY_IMPORTANT = 2
        const val PRIORITY_NORMAL = 1
        const val ENERGY_HIGH = 3
        const val ENERGY_MEDIUM = 2
        const val ENERGY_LOW = 1
        const val STATUS_PENDING = 0
        const val STATUS_COMPLETED = 1
        const val STATUS_FAILED = 2
        const val REPEAT_NONE = 0
        const val REPEAT_DAILY = 1
        const val REPEAT_WEEKLY = 2
        const val REPEAT_MONTHLY = 3
        const val REPEAT_YEARLY = 4
        const val REPEAT_CUSTOM = 5
        const val REPEAT_ON_COMPLETION = 0
        const val REPEAT_FIXED = 1

        fun getPriorityLabel(priority: Int): String = when (priority) {
            PRIORITY_URGENT -> "عاجل"
            PRIORITY_IMPORTANT -> "مهم"
            else -> "عادي"
        }
        fun getEnergyLabel(energy: Int): String = when (energy) {
            ENERGY_HIGH -> "طاقة عالية"
            ENERGY_MEDIUM -> "طاقة متوسطة"
            ENERGY_LOW -> "طاقة منخفضة"
            else -> ""
        }
        fun getRepeatLabel(repeatType: Int): String = when (repeatType) {
            REPEAT_DAILY -> "يومي"
            REPEAT_WEEKLY -> "أسبوعي"
            REPEAT_MONTHLY -> "شهري"
            REPEAT_YEARLY -> "سنوي"
            REPEAT_CUSTOM -> "مخصص"
            else -> "بدون تكرار"
        }
    }

    fun isOverdue(): Boolean {
        if (dueDate == null || status != STATUS_PENDING) return false
        return System.currentTimeMillis() > dueDate
    }

    fun getPostponeColor(): String = when {
        postponeCount <= 1 -> "#FFCC00"
        postponeCount == 2 -> "#FF9500"
        else -> "#FF3B30"
    }

    fun getTagList(): List<String> = if (tags.isEmpty()) emptyList() else tags.split(",").map { it.trim() }.filter { it.isNotEmpty() }
}
