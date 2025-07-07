package com.mahmutalperenunal.kriptex.ui.home

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import com.google.android.gms.ads.AdView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.mahmutalperenunal.kriptex.MainActivity
import com.mahmutalperenunal.kriptex.R
import com.mahmutalperenunal.kriptex.databinding.FragmentHomeBinding
import com.mahmutalperenunal.kriptex.util.BillingHelper

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
        adView.setBackgroundColor(requireContext().resources.getColor(R.color.background_color))

        requireActivity().window.navigationBarColor = requireContext().resources.getColor(R.color.background_color)

        binding.tvRemoveAds.isVisible = !BillingHelper.isAdsRemoved()

        binding.tvRemoveAds.setOnClickListener {
            BillingHelper.launchPurchaseFlow(
                activity = requireActivity(),
                context = requireContext(),
                onError = { errorMessage ->
                    Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_LONG).show()
                }
            )
        }

        return binding.root
    }
}