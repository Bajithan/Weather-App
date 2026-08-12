package com.example.weatherapp

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.weatherapp.databinding.ActivityMainBinding
import com.example.weatherapp.network.RetrofitClient
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnSearchWeather.setOnClickListener {
            val city = binding.etCityName.text.toString().trim()
            if (city.isEmpty()) {
                binding.tvErrorMessage.text = "Please enter a city name"
                binding.tvErrorMessage.visibility = View.VISIBLE
            } else {
                fetchWeather(city)
            }
        }
    }

    private fun fetchWeather(city: String) {
        lifecycleScope.launch {
            binding.progressBar.visibility = View.VISIBLE
            binding.tvErrorMessage.visibility = View.GONE

            try {
                // Perform the network call using Retrofit
                val response = RetrofitClient.api.getWeather(city, "571a78e70b70408831b68fd7ddffaf54")
                
                // On success: hide progress and error, then populate views
                binding.progressBar.visibility = View.GONE
                binding.tvErrorMessage.visibility = View.GONE
                
                binding.tvCityName.text = response.name
                binding.tvTemperature.text = "${response.main.temp}°C"
                binding.tvCondition.text = response.weather.firstOrNull()?.main ?: ""
                binding.tvHumidity.text = "${response.main.humidity}%"
                binding.tvWindSpeed.text = "${response.wind.speed} km/h"

            } catch (e: HttpException) {
                // On HttpException with code 404: show city not found error
                binding.progressBar.visibility = View.GONE
                if (e.code() == 404) {
                    binding.tvErrorMessage.text = "City not found. Please check the spelling."
                } else {
                    binding.tvErrorMessage.text = "Something went wrong. Please try again."
                }
                binding.tvErrorMessage.visibility = View.VISIBLE
            } catch (e: IOException) {
                // On any other IOException/network failure: show network error
                binding.progressBar.visibility = View.GONE
                binding.tvErrorMessage.text = "Network error. Please check your internet connection."
                binding.tvErrorMessage.visibility = View.VISIBLE
            } catch (e: Exception) {
                // On any other exception: show generic error
                binding.progressBar.visibility = View.GONE
                binding.tvErrorMessage.text = "Something went wrong. Please try again."
                binding.tvErrorMessage.visibility = View.VISIBLE
            }
        }
    }
}
