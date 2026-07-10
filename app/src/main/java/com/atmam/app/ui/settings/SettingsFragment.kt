package com.atmam.app.ui.settings

import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.preference.PreferenceFragmentCompat
import com.atmam.app.AtmamApp
import com.atmam.app.R

class SettingsFragment : PreferenceFragmentCompat(), SharedPreferences.OnSharedPreferenceChangeListener {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)
        findPreference<androidx.preference.Preference>("pref_empty_trash")?.setOnPreferenceClickListener {
            AlertDialog.Builder(requireContext()).setTitle("إفراغ سلة المحذوفات").setMessage("هل أنت متأكد؟ لا يمكن التراجع عن هذا الإجراء.")
                .setPositiveButton("إفراغ") { _, _ ->
                    val taskCount = AtmamApp.getInstance().taskDao.emptyTrash()
                    val noteCount = AtmamApp.getInstance().noteDao.emptyTrash()
                    Toast.makeText(requireContext(), "تم حذف $taskCount مهمة و $noteCount ملاحظة", Toast.LENGTH_SHORT).show()
                }.setNegativeButton("إلغاء", null).show()
            true
        }
        findPreference<androidx.preference.Preference>("pref_about")?.setOnPreferenceClickListener {
            AlertDialog.Builder(requireContext()).setTitle("اتمم").setMessage("تطبيق مهام + ملاحظات + مدونة\n\nيعمل 100% بدون إنترنت\nبدون ذكاء اصطناعي\nخصوصية كاملة\n\nالإصدار 1.0.0").setPositiveButton("حسناً", null).show()
            true
        }
    }

    override fun onResume() { super.onResume(); preferenceScreen.sharedPreferences?.registerOnSharedPreferenceChangeListener(this) }
    override fun onPause() { super.onPause(); preferenceScreen.sharedPreferences?.unregisterOnSharedPreferenceChangeListener(this) }
    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {}
}
