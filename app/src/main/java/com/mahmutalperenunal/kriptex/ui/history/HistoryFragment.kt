package com.mahmutalperenunal.kriptex.ui.history

import android.content.ClipData
import android.content.ClipboardManager
import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.mahmutalperenunal.kriptex.R
import com.mahmutalperenunal.kriptex.databinding.FragmentHistoryBinding
import com.mahmutalperenunal.kriptex.data.AppDatabase
import com.mahmutalperenunal.kriptex.util.QrUtils
import com.mahmutalperenunal.kriptex.util.ShareUtils
import kotlinx.coroutines.launch

class HistoryFragment : Fragment() {

    private lateinit var db: AppDatabase

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        val binding = FragmentHistoryBinding.inflate(inflater, container, false)
        db = AppDatabase.getDatabase(requireContext())

        val fab = requireActivity().findViewById<FloatingActionButton>(R.id.fab)
        fab.hide()

        val adapter = HistoryAdapter { item, actionType ->
            when (actionType) {
                HistoryAdapter.ActionType.COPY -> {
                    val clipboard = ContextCompat.getSystemService(requireContext(), ClipboardManager::class.java)
                    clipboard?.setPrimaryClip(ClipData.newPlainText(requireContext().resources.getString(R.string.text), item.originalText))
                    Toast.makeText(requireContext(), requireContext().resources.getString(R.string.copied), Toast.LENGTH_SHORT).show()
                }

                HistoryAdapter.ActionType.SHARE -> {
                    val qrBitmapForShare = QrUtils.generateQrCodeForSharing(item.originalText, Color.BLACK)
                    ShareUtils.shareTextWithQrCode(requireContext(), item.originalText, qrBitmapForShare)
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
                binding.tvEmptyState.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
            }
        }

        return binding.root
    }
}