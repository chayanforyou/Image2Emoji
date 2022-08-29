package me.chayan.image2emoji.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import me.chayan.image2emoji.databinding.FragmentSingleEmojiColorBinding


class SingleEmojiColorFragment : Fragment() {

    private lateinit var binding: FragmentSingleEmojiColorBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSingleEmojiColorBinding.inflate(inflater, container, false)
        return binding.root
    }
}