package com.atmam.app.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Note(
    val id: Long = 0,
    val title: String,
    val content: String = "",
    val folder: String = "عام",
    val tags: String = "",
    val isLocked: Boolean = false,
    val passwordHash: String? = null,
    val mood: Int? = null,
    val isDailyJournal: Boolean = false,
    val journalDate: Long? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val deletedAt: Long? = null
) : Parcelable {

    companion object {
        const val MOOD_EXCELLENT = 5
        const val MOOD_GOOD = 4
        const val MOOD_NEUTRAL = 3
        const val MOOD_BAD = 2
        const val MOOD_TERRIBLE = 1
    }

    fun getTagList(): List<String> = if (tags.isEmpty()) emptyList() else tags.split(",").map { it.trim() }.filter { it.isNotEmpty() }
    fun getPreview(maxLength: Int = 120): String = if (content.length <= maxLength) content else content.substring(0, maxLength) + "..."

    fun findBacklinks(otherNotes: List<Note>): List<Note> {
        val thisWords = extractSignificantWords(title + " " + content)
        return otherNotes.filter { other ->
            if (other.id == this.id) return@filter false
            val otherWords = extractSignificantWords(other.title + " " + other.content)
            thisWords.intersect(otherWords).isNotEmpty()
        }
    }

    private fun extractSignificantWords(text: String): Set<String> {
        val commonWords = setOf("و", "في", "من", "إلى", "على", "أن", "هو", "هي", "هذا", "هذه", "التي", "الذي", "كان", "يكون", "أو", "لا", "ما", "لم", "قد", "كل", "أي", "بعد", "قبل", "عن", "مع", "بين", "فوق", "تحت", "أيضا", "ثم", "the", "and", "is", "are", "was", "were", "be", "been", "have", "has", "had", "do", "does", "did", "will", "would", "could", "should", "may", "might", "to", "of", "in", "for", "on", "with", "at", "by", "from", "as")
        return text.lowercase().replace(Regex("[^\\w\\s\\u0600-\\u06FF]"), " ").split(Regex("\\s+")).filter { it.length > 2 }.filter { it !in commonWords }.toSet()
    }

    fun toTask() = Task(title = title, description = content, tags = tags)
}
