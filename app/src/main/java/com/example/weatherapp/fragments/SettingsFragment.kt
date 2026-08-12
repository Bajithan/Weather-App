package com.example.weatherapp.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.weatherapp.R
import com.example.weatherapp.data.PreferencesManager
import com.example.weatherapp.databinding.FragmentSettingsBinding

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val currentUnit = PreferencesManager.getUnitSystem(requireContext())
        if (currentUnit == "imperial") {
            binding.toggleUnits.check(R.id.btnImperial)
        } else {
            binding.toggleUnits.check(R.id.btnMetric)
        }

        binding.toggleUnits.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                val unit = if (checkedId == R.id.btnImperial) "imperial" else "metric"
                PreferencesManager.setUnitSystem(requireContext(), unit)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
