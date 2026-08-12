package com.example.weatherapp.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.weatherapp.R
import com.example.weatherapp.data.PreferencesManager
import com.example.weatherapp.databinding.FragmentDashboardBinding
import com.example.weatherapp.network.RetrofitClient
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!

    private var unitSystem: String = "metric"
    private var currentCity: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnSearchWeather.setOnClickListener {
            val city = binding.etCityName.text.toString().trim()
            if (city.isEmpty()) {
                binding.tvErrorMessage.text = "Please enter a city name"
                binding.tvErrorMessage.visibility = View.VISIBLE
                binding.cvWeather.visibility = View.GONE
            } else {
                searchWeather(city)
            }
        }

        binding.btnSaveCity.setOnClickListener {
            val city = currentCity ?: return@setOnClickListener
            val context = requireContext()
            if (PreferencesManager.isCitySaved(context, city)) {
                PreferencesManager.removeSavedCity(context, city)
            } else {
                PreferencesManager.addSavedCity(context, city)
            }
            updateSaveIcon(city)
        }
        
        // Hide card initially
        binding.cvWeather.visibility = View.GONE
    }

    override fun onResume() {
        super.onResume()
        unitSystem = PreferencesManager.getUnitSystem(requireContext())
    }

    private fun searchWeather(city: String) {
        viewLifecycleOwner.lifecycleScope.launch {
            binding.progressBar.visibility = View.VISIBLE
            binding.tvErrorMessage.visibility = View.GONE
            binding.cvWeather.visibility = View.GONE

            try {
                val response = RetrofitClient.api.getWeather(city, "83a32c8b66151ca48893e4a1a3be5457", unitSystem)
                
                binding.progressBar.visibility = View.GONE
                binding.tvErrorMessage.visibility = View.GONE
                binding.cvWeather.visibility = View.VISIBLE
                
                currentCity = response.name
                
                binding.tvCityName.text = response.name
                val tempUnit = if (unitSystem == "metric") "°C" else "°F"
                binding.tvTemperature.text = "${response.main.temp.toInt()}$tempUnit"
                binding.tvCondition.text = response.weather.firstOrNull()?.main ?: ""
                binding.tvHumidity.text = "${response.main.humidity}%"
                
                val windUnit = if (unitSystem == "metric") " km/h" else " mph"
                binding.tvWindSpeed.text = "${response.wind.speed}$windUnit"
                
                updateSaveIcon(response.name)

            } catch (e: HttpException) {
                binding.progressBar.visibility = View.GONE
                if (e.code() == 404) {
                    binding.tvErrorMessage.text = "City not found. Please check the spelling."
                } else {
                    binding.tvErrorMessage.text = "Something went wrong. Please try again."
                }
                binding.tvErrorMessage.visibility = View.VISIBLE
            } catch (e: IOException) {
                binding.progressBar.visibility = View.GONE
                binding.tvErrorMessage.text = "Network error. Please check your internet connection."
                binding.tvErrorMessage.visibility = View.VISIBLE
            } catch (e: Exception) {
                binding.progressBar.visibility = View.GONE
                binding.tvErrorMessage.text = "Something went wrong. Please try again."
                binding.tvErrorMessage.visibility = View.VISIBLE
            }
        }
    }

    private fun updateSaveIcon(city: String) {
        val isSaved = PreferencesManager.isCitySaved(requireContext(), city)
        val iconRes = if (isSaved) R.drawable.ic_star_filled else R.drawable.ic_star_outline
        binding.btnSaveCity.setImageResource(iconRes)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
