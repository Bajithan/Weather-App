package com.example.weatherapp.data

import android.content.Context
import android.content.SharedPreferences

/**
 * Singleton object that manages SharedPreferences for the Weather App.
 * Handles user settings like unit systems and the list of saved cities.
 */
object PreferencesManager {

    private const val PREFS_NAME = "weather_app_prefs"
    private const val KEY_UNIT_SYSTEM = "unit_system"
    private const val KEY_SAVED_CITIES = "saved_cities"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    /**
     * Retrieves the preferred unit system from preferences.
     * @param context The application context.
     * @return "metric" or "imperial", defaults to "metric".
     */
    fun getUnitSystem(context: Context): String {
        return getPrefs(context).getString(KEY_UNIT_SYSTEM, "metric") ?: "metric"
    }

    /**
     * Saves the preferred unit system to preferences.
     * @param context The application context.
     * @param unit The unit system to save ("metric" or "imperial").
     */
    fun setUnitSystem(context: Context, unit: String) {
        getPrefs(context).edit().putString(KEY_UNIT_SYSTEM, unit).apply()
    }

    /**
     * Retrieves the set of cities saved by the user.
     * @param context The application context.
     * @return A mutable copy of the set of saved city names, or an empty set if none exist.
     */
    fun getSavedCities(context: Context): Set<String> {
        val cities = getPrefs(context).getStringSet(KEY_SAVED_CITIES, emptySet()) ?: emptySet()
        return cities.toMutableSet()
    }

    /**
     * Adds a new city to the set of saved cities in preferences.
     * @param context The application context.
     * @param city The name of the city to add.
     */
    fun addSavedCity(context: Context, city: String) {
        val cities = getSavedCities(context).toMutableSet()
        if (cities.add(city)) {
            getPrefs(context).edit().putStringSet(KEY_SAVED_CITIES, cities).apply()
        }
    }

    /**
     * Removes a city from the set of saved cities in preferences.
     * @param context The application context.
     * @param city The name of the city to remove.
     */
    fun removeSavedCity(context: Context, city: String) {
        val cities = getSavedCities(context).toMutableSet()
        if (cities.remove(city)) {
            getPrefs(context).edit().putStringSet(KEY_SAVED_CITIES, cities).apply()
        }
    }

    /**
     * Checks if a specific city is currently saved in preferences.
     * @param context The application context.
     * @param city The name of the city to check.
     * @return True if the city is found in the saved set, false otherwise.
     */
    fun isCitySaved(context: Context, city: String): Boolean {
        return getSavedCities(context).contains(city)
    }
}
