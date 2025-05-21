package com.mahmutalperenunal.cryptosentinel.ui.history

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.mahmutalperenunal.cryptosentinel.R
import com.mahmutalperenunal.cryptosentinel.databinding.FragmentHistoryBinding
import com.mahmutalperenunal.cryptosentinel.data.AppDatabase
import kotlinx.coroutines.launch

class HistoryFragment : Fragment() {

    private lateinit var db: AppDatabase

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        val binding = FragmentHistoryBinding.inflate(inflater, container, false)
        db = AppDatabase.getDatabase(requireContext())

        val toolbar = binding.tbHeader
        (requireActivity() as AppCompatActivity).setSupportActionBar(toolbar)
        (requireActivity() as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)

        toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }

        val adapter = HistoryAdapter { item, actionType ->
            when (actionType) {
                HistoryAdapter.ActionType.COPY -> {
                    val clipboard = ContextCompat.getSystemService(requireContext(), ClipboardManager::class.java)
                    clipboard?.setPrimaryClip(ClipData.newPlainText(requireContext().resources.getString(R.string.text), item.originalText))
                    Toast.makeText(requireContext(), requireContext().resources.getString(R.string.copied), Toast.LENGTH_SHORT).show()
                }

                HistoryAdapter.ActionType.SHARE -> {
                    val intent = Intent().apply {
                        action = Intent.ACTION_SEND
                        putExtra(Intent.EXTRA_TEXT, item.originalText)
                        type = "text/plain"
                    }
                    startActivity(Intent.createChooser(intent, requireContext().resources.getString(
                        R.string.share)))
                }

                HistoryAdapter.ActionType.DELETE -> {
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
                adapter.submitList(list)
            }
        }

        return binding.root
    }
}