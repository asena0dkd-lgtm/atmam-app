package com.atmam.app.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Attachment(
    val id: Long = 0,
    val taskId: Long? = null,
    val noteId: Long? = null,
    val type: Int = TYPE_FILE,
    val filePath: String,
    val fileName: String,
    val mimeType: String = "",
    val fileSize: Long = 0,
    val createdAt: Long = System.currentTimeMillis()
) : Parcelable {

    companion object {
        const val TYPE_IMAGE = 1
        const val TYPE_FILE = 2
        const val TYPE_VIDEO = 3
        const val TYPE_AUDIO = 4
    }

    fun isImage(): Boolean = type == TYPE_IMAGE || mimeType.startsWith("image/")
    fun isAudio(): Boolean = type == TYPE_AUDIO || mimeType.startsWith("audio/")
    fun isVideo(): Boolean = type == TYPE_VIDEO || mimeType.startsWith("video/")
}
