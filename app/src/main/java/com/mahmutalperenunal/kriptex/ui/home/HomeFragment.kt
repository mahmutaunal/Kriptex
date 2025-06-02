package com.mahmutalperenunal.kriptex.ui.home

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.google.android.gms.ads.AdView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.mahmutalperenunal.kriptex.R
import com.mahmutalperenunal.kriptex.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        val binding = FragmentHomeBinding.inflate(inflater, container, false)

        val fab = requireActivity().findViewById<FloatingActionButton>(R.id.fab)
        fab.hide()

        val bottomNavigationView = requireActivity().findViewById<BottomNavigationView>(R.id.bottomNav)
        bottomNavigationView.visibility = View.VISIBLE

        val adView = requireActivity().findViewById<AdView>(R.id.adView)
        adView.setBackgroundColor(requireContext().resources.getColor(R.color.adview_background_color))

        requireActivity().window.navigationBarColor = requireContext().resources.getColor(R.color.adview_background_color)

        binding.ivSettings.setOnClickListener {
            findNavController().navigate(R.id.action_home_to_settings)
        }

        return binding.root
    }
}