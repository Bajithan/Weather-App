package com.example.weatherapp.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.weatherapp.databinding.ItemSavedCityBinding
import com.example.weatherapp.network.WeatherResponse

class SavedCityAdapter(
    private val items: MutableList<Pair<String, WeatherResponse?>>,
    private val unitSystem: String,
    private val onRemoveClick: (String) -> Unit
) : RecyclerView.Adapter<SavedCityAdapter.ViewHolder>() {

    class ViewHolder(val binding: ItemSavedCityBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemSavedCityBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val (cityName, weather) = items[position]
        holder.binding.tvItemCityName.text = cityName

        if (weather == null) {
            holder.binding.tvItemTemperature.text = "Loading..."
        } else {
            val temp = weather.main.temp.toInt()
            val unit = if (unitSystem == "metric") "°C" else "°F"
            val condition = weather.weather.firstOrNull()?.main ?: ""
            holder.binding.tvItemTemperature.text = "$temp$unit · $condition"
        }

        holder.binding.btnRemoveCity.setOnClickListener {
            onRemoveClick(cityName)
        }
    }

    override fun getItemCount(): Int = items.size

    fun updateWeatherFor(city: String, response: WeatherResponse) {
        val index = items.indexOfFirst { it.first == city }
        if (index != -1) {
            items[index] = Pair(city, response)
            notifyItemChanged(index)
        }
    }

    fun removeItem(city: String) {
        val index = items.indexOfFirst { it.first == city }
        if (index != -1) {
            items.removeAt(index)
            notifyItemRemoved(index)
        }
    }
}
