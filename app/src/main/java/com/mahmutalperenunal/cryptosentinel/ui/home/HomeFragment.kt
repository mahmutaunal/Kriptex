package com.mahmutalperenunal.cryptosentinel.ui.home

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.mahmutalperenunal.cryptosentinel.R
import com.mahmutalperenunal.cryptosentinel.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        val binding = FragmentHomeBinding.inflate(inflater, container, false)

        binding.btnEncrypt.setOnClickListener {
            findNavController().navigate(R.id.action_home_to_encrypt)
        }

        binding.btnDecrypt.setOnClickListener {
            findNavController().navigate(R.id.action_home_to_decrypt)
        }

        binding.btnHistory.setOnClickListener {
            findNavController().navigate(R.id.action_home_to_history)
        }

        binding.btnSettings.setOnClickListener {
            findNavController().navigate(R.id.action_home_to_settings)
        }

        return binding.root
    }
}