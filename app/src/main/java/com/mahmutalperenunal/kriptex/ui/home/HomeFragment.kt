package com.mahmutalperenunal.kriptex.ui.home

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.MenuProvider
import androidx.core.view.isVisible
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.mahmutalperenunal.kriptex.MainActivity
import com.mahmutalperenunal.kriptex.R
import com.mahmutalperenunal.kriptex.databinding.FragmentHomeBinding
import com.mahmutalperenunal.kriptex.ui.settings.BottomSheetLanguage
import com.mahmutalperenunal.kriptex.ui.settings.BottomSheetTheme
import com.mahmutalperenunal.kriptex.util.BillingHelper

class HomeFragment : Fragment() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireActivity().addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menu.clear()
                menuInflater.inflate(R.menu.menu_main, menu)

                menu.findItem(R.id.action_theme).setOnMenuItemClickListener {
                    BottomSheetTheme().show(parentFragmentManager, "ThemeSheet")
                    true
                }

                menu.findItem(R.id.action_language).setOnMenuItemClickListener {
                    BottomSheetLanguage().show(parentFragmentManager, "LanguageSheet")
                    true
                }
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return false
            }
        }, viewLifecycleOwner)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        val binding = FragmentHomeBinding.inflate(inflater, container, false)

        val fab = requireActivity().findViewById<FloatingActionButton>(R.id.fab)
        fab.hide()

        val bottomNavigationView = requireActivity().findViewById<BottomNavigationView>(R.id.bottomNav)
        bottomNavigationView.visibility = View.VISIBLE

        (requireActivity() as MainActivity).apply {
            isFilterVisible = false
            isSearchVisible = false
            isThemeVisible = true
            isLanguageVisible = true
            invalidateOptionsMenu()
        }

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

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        menu.clear()
        inflater.inflate(R.menu.menu_main, menu)

        menu.findItem(R.id.action_theme).setOnMenuItemClickListener {
            BottomSheetTheme().show(parentFragmentManager, "ThemeSheet")
            true
        }

        menu.findItem(R.id.action_language).setOnMenuItemClickListener {
            BottomSheetLanguage().show(parentFragmentManager, "LanguageSheet")
            true
        }
    }
}