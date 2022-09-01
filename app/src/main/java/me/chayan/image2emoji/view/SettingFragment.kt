package me.chayan.image2emoji.view

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import me.chayan.image2emoji.databinding.FragmentSettingBinding


class SettingFragment : Fragment() {

    private lateinit var binding: FragmentSettingBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSettingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val sharedPreferences = requireContext().getSharedPreferences("i2e", Context.MODE_PRIVATE)
        val format = sharedPreferences.getString("save_format", "jpg")

        binding.settingQualitySeek.setProgress(sharedPreferences.getInt("save_quality", 100).toFloat())

        if (format == "jpg") {
            binding.radioButtonJpg.isChecked = true
            binding.settingQualitySeek.isEnabled = true
        }
        if (format == "png") {
            binding.radioButtonPng.isChecked = true
            binding.settingQualitySeek.isEnabled = false
        }

        binding.radioButtonJpg.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                binding.settingQualitySeek.isEnabled = true
            }
        }

        binding.radioButtonPng.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                binding.settingQualitySeek.isEnabled = false
            }
        }
    }

    private fun saveSettings() {
        val edit = requireContext().getSharedPreferences("i2e", Context.MODE_PRIVATE).edit()
        if (binding.radioButtonJpg.isChecked) {
            edit.putString("save_format", "jpg")
        }
        if (binding.radioButtonPng.isChecked) {
            edit.putString("save_format", "png")
        }
        edit.putInt("save_quality", binding.settingQualitySeek.progress)
        edit.apply()

        Toast.makeText(requireContext(), "Saved", Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        saveSettings()
        super.onDestroyView()
    }
}