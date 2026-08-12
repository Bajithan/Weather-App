package com.example.weatherapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.weatherapp.databinding.ActivityMainBinding
import com.example.weatherapp.fragments.DashboardFragment
import com.example.weatherapp.fragments.SavedFragment
import com.example.weatherapp.fragments.SettingsFragment

/**
 * The main container for the application, hosting the bottom navigation
 * and the fragments for Dashboard, Saved Cities, and Settings.
 */
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // On first launch, show the DashboardFragment by default
        if (savedInstanceState == null) {
            loadFragment(DashboardFragment())
        }

        // Set up listener for bottom navigation item selections
        binding.bottomNav.setOnItemSelectedListener { item ->
            val fragment: Fragment = when (item.itemId) {
                R.id.nav_forecast -> DashboardFragment()
                R.id.nav_saved -> SavedFragment()
                R.id.nav_settings -> SettingsFragment()
                else -> DashboardFragment()
            }
            loadFragment(fragment)
            true
        }
    }

    /**
     * Replaces the current fragment in the container with the provided fragment.
     * @param fragment The new fragment to display.
     */
    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }
}
