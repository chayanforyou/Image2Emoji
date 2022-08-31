package me.chayan.image2emoji.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import me.chayan.image2emoji.R
import me.chayan.image2emoji.databinding.FragmentHomeBinding
import me.chayan.image2emoji.utils.AnimationUtil

class HomeFragment : Fragment() {

    private lateinit var binding: FragmentHomeBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        buttonConfig()
    }

    private fun buttonConfig() {

        // Play Animation
        AnimationUtil.start(binding.buttonMulti, R.anim.lefttoright, 50)
        AnimationUtil.start(binding.buttonSingle, R.anim.lefttoright, 100)
        AnimationUtil.start(binding.buttonSingle2, R.anim.lefttoright, 150)
        AnimationUtil.start(binding.buttonSingleMulticolor, R.anim.lefttoright, 200)

        // Setup listener
        binding.buttonMulti.setOnClickListener {
            findNavController().navigate(HomeFragmentDirections.actionNavHomeToNavMultiEmoji())
        }

        binding.buttonSingle.setOnClickListener {
            findNavController().navigate(HomeFragmentDirections.actionNavHomeToNavSingleEmojiSize())
        }

        binding.buttonSingle2.setOnClickListener {
            findNavController().navigate(HomeFragmentDirections.actionNavHomeToNavSingleEmoji2())
        }

        binding.buttonSingleMulticolor.setOnClickListener {
            findNavController().navigate(HomeFragmentDirections.actionNavHomeToNavSingleEmojiColor())
        }
    }
}