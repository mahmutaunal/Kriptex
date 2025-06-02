package com.mahmutalperenunal.kriptex.ui.settings

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.findNavController
import com.google.android.gms.ads.AdView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.mahmutalperenunal.kriptex.R
import com.mahmutalperenunal.kriptex.databinding.FragmentSettingsBinding

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
        val toolbar = binding.tbHeader
        (requireActivity() as AppCompatActivity).setSupportActionBar(toolbar)
        (requireActivity() as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)

        toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }

        val fab = requireActivity().findViewById<FloatingActionButton>(R.id.fab)
        fab.hide()

        val bottomNavigationView = requireActivity().findViewById<BottomNavigationView>(R.id.bottomNav)
        bottomNavigationView.visibility = View.GONE

        val adView = requireActivity().findViewById<AdView>(R.id.adView)
        adView.setBackgroundColor(requireContext().resources.getColor(R.color.background_color))

        requireActivity().window.navigationBarColor = requireContext().resources.getColor(R.color.background_color)

        binding.llTheme.setOnClickListener { onThemeClicked() }
        binding.llLanguage.setOnClickListener { onLanguageClicked() }
    }

    private fun onThemeClicked() {
        BottomSheetTheme().show(parentFragmentManager, "ThemeSheet")
    }

    private fun onLanguageClicked() {
        BottomSheetLanguage().show(parentFragmentManager, "LanguageSheet")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}