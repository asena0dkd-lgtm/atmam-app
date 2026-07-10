package com.atmam.app.utils

object Constants {
    const val REQUEST_RECORD_AUDIO = 1001
    const val REQUEST_PICK_IMAGE = 1002
    const val REQUEST_PICK_FILE = 1003
    const val REQUEST_NOTIFICATION_PERMISSION = 1004

    const val PREFS_NAME = "atmam_prefs"
    const val PREF_AUTO_ARCHIVE = "auto_archive"
    const val PREF_AUTO_ARCHIVE_DAYS = "auto_archive_days"
    const val PREF_THEME = "theme"
    const val PREF_NOTIFICATIONS_ENABLED = "notifications_enabled"
    const val PREF_VIBRATION = "vibration_enabled"
    const val PREF_SOUND = "sound_enabled"
    const val PREF_FIRST_LAUNCH = "first_launch"

    const val EXTRA_TASK_ID = "task_id"
    const val EXTRA_NOTE_ID = "note_id"
    const val EXTRA_IS_NEW = "is_new"

    const val DRAWING_ACTION_COMPLETE = "complete"
    const val DRAWING_ACTION_FAIL = "fail"

    const val ANIM_DURATION_SHORT = 150L
    const val ANIM_DURATION_MEDIUM = 300L
    const val ANIM_DURATION_LONG = 500L
}
