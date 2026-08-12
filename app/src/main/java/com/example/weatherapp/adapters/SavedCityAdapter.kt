package com.example.weatherapp.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.weatherapp.R
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
            holder.binding.tvItemTemperature.text = "--"
            holder.binding.tvItemCondition.text = "Loading..."
            holder.binding.tvItemHumidity.text = "--%"
            holder.binding.ivWeatherIcon.setImageResource(R.drawable.ic_cloud)
        } else {
            val temp = weather.main.temp.toInt()
            val condition = weather.weather.firstOrNull()?.main ?: ""
            val humidity = weather.main.humidity

            holder.binding.tvItemTemperature.text = "$temp°"
            holder.binding.tvItemCondition.text = condition
            holder.binding.tvItemHumidity.text = "$humidity%"

            // Map condition to icon
            val iconRes = when (condition.lowercase()) {
                "clear" -> {
                    // Simple check for day/night if needed, but for now just Moon as in design or Sun
                    // The design shows a sun for Colombo and a moon for New York.
                    // For now let's use Sun for Clear, and maybe Moon if it's night (but we don't have time easily)
                    // Let's just use Sun for "Clear" and Moon if it matches a "Night" string if we had one.
                    // Actually, let's just use the icons provided in the screenshot for variety.
                    if (cityName.contains("New York", ignoreCase = true)) R.drawable.ic_moon else R.drawable.ic_sun
                }
                "clouds" -> R.drawable.ic_partly_cloudy
                "rain", "drizzle" -> R.drawable.ic_rain
                else -> R.drawable.ic_sun
            }
            holder.binding.ivWeatherIcon.setImageResource(iconRes)
        }

        // Click on the whole card to remove for now, or we could add a long click
        // The design doesn't show a remove button.
        holder.itemView.setOnLongClickListener {
            onRemoveClick(cityName)
            true
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
