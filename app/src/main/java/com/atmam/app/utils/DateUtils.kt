package com.atmam.app.utils

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

object DateUtils {
    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale("ar"))
    private val timeFormat = SimpleDateFormat("HH:mm", Locale("ar"))
    private val dateTimeFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("ar"))

    fun formatDate(timestamp: Long): String = dateFormat.format(Date(timestamp))
    fun formatTime(timestamp: Long): String = timeFormat.format(Date(timestamp))
    fun formatDateTime(timestamp: Long): String = dateTimeFormat.format(Date(timestamp))

    fun formatRelative(timestamp: Long): String {
        val diff = System.currentTimeMillis() - timestamp
        return when {
            diff < TimeUnit.MINUTES.toMillis(1) -> "الآن"
            diff < TimeUnit.HOURS.toMillis(1) -> "قبل ${TimeUnit.MILLISECONDS.toMinutes(diff)} دقيقة"
            diff < TimeUnit.DAYS.toMillis(1) -> "قبل ${TimeUnit.MILLISECONDS.toHours(diff)} ساعة"
            diff < TimeUnit.DAYS.toMillis(2) -> "أمس"
            diff < TimeUnit.DAYS.toMillis(7) -> "قبل ${TimeUnit.MILLISECONDS.toDays(diff)} أيام"
            else -> formatDate(timestamp)
        }
    }

    fun isToday(timestamp: Long): Boolean {
        val cal = Calendar.getInstance()
        val targetCal = Calendar.getInstance().apply { timeInMillis = timestamp }
        return cal.get(Calendar.DAY_OF_YEAR) == targetCal.get(Calendar.DAY_OF_YEAR) && cal.get(Calendar.YEAR) == targetCal.get(Calendar.YEAR)
    }

    fun isTomorrow(timestamp: Long): Boolean {
        val cal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 1) }
        val targetCal = Calendar.getInstance().apply { timeInMillis = timestamp }
        return cal.get(Calendar.DAY_OF_YEAR) == targetCal.get(Calendar.DAY_OF_YEAR) && cal.get(Calendar.YEAR) == targetCal.get(Calendar.YEAR)
    }
}
