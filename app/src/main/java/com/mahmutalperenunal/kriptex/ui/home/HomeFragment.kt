package com.mahmutalperenunal.kriptex.ui.home

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.mahmutalperenunal.kriptex.R
import com.mahmutalperenunal.kriptex.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        val binding = FragmentHomeBinding.inflate(inflater, container, false)

        binding.ivSettings.setOnClickListener {
            findNavController().navigate(R.id.action_home_to_settings)
        }

        return binding.root
    }
}