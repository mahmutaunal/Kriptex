package com.mahmutalperenunal.cryptosentinel.ui.encryption

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
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.mahmutalperenunal.cryptosentinel.R
import com.mahmutalperenunal.cryptosentinel.databinding.FragmentEncryptionBinding
import com.mahmutalperenunal.cryptosentinel.data.AppDatabase
import com.mahmutalperenunal.cryptosentinel.data.model.EncryptedText
import com.mahmutalperenunal.cryptosentinel.util.EncryptionUtil
import com.mahmutalperenunal.cryptosentinel.util.QrCodeGenerator
import kotlinx.coroutines.launch

class EncryptionFragment : Fragment() {

    private lateinit var db: AppDatabase

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        val binding = FragmentEncryptionBinding.inflate(inflater, container, false)

        db = AppDatabase.getDatabase(requireContext())

        val toolbar = binding.tbHeader
        (requireActivity() as AppCompatActivity).setSupportActionBar(toolbar)
        (requireActivity() as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)

        toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }

        binding.etPlainText.addTextChangedListener { editable ->
            val inputText = editable?.toString() ?: ""

            binding.btnEncrypt.visibility = if (inputText.isNotBlank()) View.VISIBLE else View.GONE
            if (binding.tlPlainText.error != null) {
                binding.tlPlainText.error = null
            }
        }

        binding.btnEncrypt.setOnClickListener {
            val input = binding.etPlainText.text.toString()
            try {
                val result = EncryptionUtil.encrypt(input)

                binding.tvEncrypted.text = result
                binding.ivQrCode.setImageBitmap(QrCodeGenerator.generate(result, context = requireContext()))

                binding.ivQrCode.visibility = View.VISIBLE
                binding.btnCopy.visibility = View.VISIBLE
                binding.btnShare.visibility = View.VISIBLE

                lifecycleScope.launch {
                    db.encryptedTextDao().insert(
                        EncryptedText(
                            originalText = input,
                            encryptedText = result,
                            qrContent = result
                        )
                    )
                }
            } catch (e: Exception) {
                binding.tlPlainText.error = getString(R.string.incorrect_text_or_encryption)
                binding.tvEncrypted.text = ""
                binding.ivQrCode.setImageDrawable(null)
                binding.btnCopy.visibility = View.GONE
                binding.btnShare.visibility = View.GONE
                binding.btnEncrypt.visibility = View.GONE
                binding.ivQrCode.visibility = View.GONE
            }
        }

        binding.btnCopy.setOnClickListener {
            val text = binding.tvEncrypted.text.toString()
            if (text.isNotBlank()) {
                val clipboard =
                    ContextCompat.getSystemService(requireContext(), ClipboardManager::class.java)
                val clip = ClipData.newPlainText(requireContext().resources.getString(R.string.encrypted_text), text)
                clipboard?.setPrimaryClip(clip)
                Toast.makeText(requireContext(), requireContext().resources.getString(R.string.copied), Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnShare.setOnClickListener {
            val text = binding.tvEncrypted.text.toString()
            if (text.isNotBlank()) {
                val intent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TEXT, text)
                    type = "text/plain"
                }
                startActivity(Intent.createChooser(intent, requireContext().resources.getString(R.string.share)))
            }
        }

        return binding.root
    }
}