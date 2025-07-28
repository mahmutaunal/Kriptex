package com.mahmutalperenunal.kriptex.ui.history

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.MenuProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.mahmutalperenunal.kriptex.MainActivity
import com.mahmutalperenunal.kriptex.R
import com.mahmutalperenunal.kriptex.databinding.FragmentHistoryBinding
import com.mahmutalperenunal.kriptex.data.AppDatabase
import com.mahmutalperenunal.kriptex.data.model.EncryptedText
import com.mahmutalperenunal.kriptex.util.AdManager
import com.mahmutalperenunal.kriptex.util.EncryptionType
import com.mahmutalperenunal.kriptex.util.QrUtils
import com.mahmutalperenunal.kriptex.util.ShareUtils
import kotlinx.coroutines.launch

class HistoryFragment : Fragment() {

    private var _binding: FragmentHistoryBinding? = null
    private val binding get() = _binding!!

    private lateinit var db: AppDatabase

    private lateinit var adapter: HistoryAdapter

    private var allItems: List<EncryptedText> = emptyList()
    private var currentQuery: String = ""
    private var selectedFilters: Set<EncryptionType> = emptySet()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireActivity().addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menu.clear()
                menuInflater.inflate(R.menu.menu_main, menu)

                val searchItem = menu.findItem(R.id.action_search)
                val searchView = searchItem.actionView as SearchView
                searchView.queryHint = getString(R.string.search)

                searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                    override fun onQueryTextSubmit(query: String?): Boolean = false
                    override fun onQueryTextChange(newText: String?): Boolean {
                        filterList(newText.orEmpty())
                        return true
                    }
                })

                menu.findItem(R.id.action_filter).setOnMenuItemClickListener {
                    showFilterBottomSheet()
                    true
                }
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean = false
        }, viewLifecycleOwner)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHistoryBinding.inflate(inflater, container, false)
        db = AppDatabase.getDatabase(requireContext())

        val fab = requireActivity().findViewById<FloatingActionButton>(R.id.fab)
        fab.hide()

        (requireActivity() as MainActivity).apply {
            isFilterVisible = true
            isSearchVisible = true
            isThemeVisible = false
            isLanguageVisible = false
            invalidateOptionsMenu()
        }

        adapter = HistoryAdapter { item, actionType ->
            when (actionType) {
                HistoryAdapter.ActionType.COPY -> {
                    val clipboard = ContextCompat.getSystemService(requireContext(), ClipboardManager::class.java)
                    clipboard?.setPrimaryClip(ClipData.newPlainText(requireContext().resources.getString(R.string.text), item.originalText))
                    Toast.makeText(requireContext(), requireContext().resources.getString(R.string.copied), Toast.LENGTH_SHORT).show()
                }

                HistoryAdapter.ActionType.SHARE -> {
                    AdManager.recordActionAndShowAdIfNeeded(context as Activity, 5)

                    val qrBitmapForShare = QrUtils.generateQrCodeForSharing(item.originalText, Color.BLACK)
                    ShareUtils.shareTextWithQrCode(requireContext(), item.originalText, qrBitmapForShare)
                }

                HistoryAdapter.ActionType.DELETE -> {
                    AdManager.recordActionAndShowAdIfNeeded(context as Activity, 5)

                    lifecycleScope.launch {
                        db.encryptedTextDao().delete(item)
                    }
                }
            }
        }
        binding.recyclerViewHistory.adapter = adapter
        binding.recyclerViewHistory.layoutManager = LinearLayoutManager(requireContext())

        lifecycleScope.launch {
            db.encryptedTextDao().getAll().collect { list ->
                allItems = list
                updateFilteredList()
            }
        }

        return binding.root
    }

    private fun filterList(query: String) {
        currentQuery = query.lowercase()
        updateFilteredList()
    }

    private fun updateFilteredList() {
        val filtered = allItems.filter { item ->
            val matchesQuery = item.originalText.lowercase().contains(currentQuery)
            val matchesType = selectedFilters.isEmpty() || selectedFilters.any { it.name == item.type }
            matchesQuery && matchesType
        }
        adapter.submitList(filtered)
        binding.tvEmptyState.visibility = if (filtered.isEmpty()) View.VISIBLE else View.GONE
    }

    private fun showFilterBottomSheet() {
        FilterBottomSheet(
            selectedTypes = selectedFilters,
            onTypesSelected = { selected ->
                selectedFilters = selected
                updateFilteredList()
            }
        ).show(parentFragmentManager, "FilterBottomSheet")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}