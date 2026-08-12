package com.example.weatherapp.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.weatherapp.adapters.SavedCityAdapter
import com.example.weatherapp.data.PreferencesManager
import com.example.weatherapp.databinding.FragmentSavedBinding
import com.example.weatherapp.network.RetrofitClient
import com.example.weatherapp.network.WeatherResponse
import kotlinx.coroutines.launch

class SavedFragment : Fragment() {

    private var _binding: FragmentSavedBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: SavedCityAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSavedBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        setupRecyclerView()
    }

    private fun setupRecyclerView() {
        val context = requireContext()
        val savedCities = PreferencesManager.getSavedCities(context).toList()
        val unitSystem = PreferencesManager.getUnitSystem(context)

        if (savedCities.isEmpty()) {
            binding.tvNoSavedCities.visibility = View.VISIBLE
            binding.rvSavedCities.visibility = View.GONE
        } else {
            binding.tvNoSavedCities.visibility = View.GONE
            binding.rvSavedCities.visibility = View.VISIBLE

            val items = savedCities.map { Pair(it, null as WeatherResponse?) }.toMutableList()
            adapter = SavedCityAdapter(items, unitSystem) { cityToRemove ->
                PreferencesManager.removeSavedCity(context, cityToRemove)
                adapter.removeItem(cityToRemove)
                if (adapter.itemCount == 0) {
                    binding.tvNoSavedCities.visibility = View.VISIBLE
                    binding.rvSavedCities.visibility = View.GONE
                }
            }

            binding.rvSavedCities.layoutManager = LinearLayoutManager(context)
            binding.rvSavedCities.adapter = adapter

            // Fetch weather for each city
            savedCities.forEach { city ->
                viewLifecycleOwner.lifecycleScope.launch {
                    try {
                        val result = RetrofitClient.api.getWeather(
                            city, "MY_API_KEY_HERE", unitSystem
                        )
                        adapter.updateWeatherFor(city, result)
                    } catch (e: Exception) {
                        // Silently fail or handle error for individual items
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
