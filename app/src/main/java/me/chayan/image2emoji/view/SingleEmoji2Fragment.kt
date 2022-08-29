package me.chayan.image2emoji.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import me.chayan.image2emoji.databinding.FragmentSingleEmoji2Binding


class SingleEmoji2Fragment : Fragment() {

    private lateinit var binding: FragmentSingleEmoji2Binding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSingleEmoji2Binding.inflate(inflater, container, false)
        return binding.root
    }
}