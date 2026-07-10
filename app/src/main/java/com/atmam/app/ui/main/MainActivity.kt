package com.atmam.app.ui.main

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.atmam.app.R
import com.atmam.app.databinding.ActivityMainBinding
import com.atmam.app.ui.notes.NotesFragment
import com.atmam.app.ui.settings.SettingsFragment
import com.atmam.app.ui.stats.StatisticsFragment
import com.atmam.app.ui.tasks.TasksFragment

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (savedInstanceState == null) {
            navigateToFragment(TasksFragment())
        }

        binding.bottomNavigation.setOnItemSelectedListener { item ->
            val fragment = when (item.itemId) {
                R.id.nav_tasks -> TasksFragment()
                R.id.nav_notes -> NotesFragment()
                R.id.nav_stats -> StatisticsFragment()
                R.id.nav_settings -> SettingsFragment()
                else -> TasksFragment()
            }
            navigateToFragment(fragment)
            true
        }
    }

    private fun navigateToFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }
}
