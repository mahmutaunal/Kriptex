package com.mahmutalperenunal.cryptosentinel.ui.decryption

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
import com.mahmutalperenunal.cryptosentinel.data.AppDatabase
import com.mahmutalperenunal.cryptosentinel.data.model.EncryptedText
import com.mahmutalperenunal.cryptosentinel.databinding.FragmentDecryptionBinding
import com.mahmutalperenunal.cryptosentinel.util.EncryptionUtil
import com.mahmutalperenunal.cryptosentinel.util.QrCodeGenerator
import kotlinx.coroutines.launch

class DecryptionFragment : Fragment() {

    private lateinit var db: AppDatabase

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        val binding = FragmentDecryptionBinding.inflate(inflater, container, false)

        db = AppDatabase.getDatabase(requireContext())

        val toolbar = binding.tbHeader
        (requireActivity() as AppCompatActivity).setSupportActionBar(toolbar)
        (requireActivity() as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)

        toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }

        binding.etEncryptedText.addTextChangedListener { editable ->
            val inputText = editable?.toString() ?: ""

            binding.btnDecrypt.visibility = if (inputText.isNotBlank()) View.VISIBLE else View.GONE
            if (binding.tlEncryptedText.error != null) {
                binding.tlEncryptedText.error = null
            }
        }

        binding.btnDecrypt.setOnClickListener {
            val input = binding.etEncryptedText.text.toString()
            try {
                val result = EncryptionUtil.decrypt(input)

                binding.tvDecrypted.text = result
                binding.ivQrCode.setImageBitmap(QrCodeGenerator.generate(result, context = requireContext()))

                binding.ivQrCode.visibility = View.VISIBLE
                binding.btnCopy.visibility = View.VISIBLE
                binding.btnShare.visibility = View.VISIBLE

                lifecycleScope.launch {
                    db.encryptedTextDao().insert(
                        EncryptedText(
                            originalText = result,
                            encryptedText = input,
                            qrContent = result
                        )
                    )
                }

                binding.tlEncryptedText.error = null
            } catch (e: Exception) {
                binding.tlEncryptedText.error = getString(R.string.incorrect_text_or_encryption)
                binding.tvDecrypted.text = ""
                binding.ivQrCode.setImageDrawable(null)
                binding.btnCopy.visibility = View.GONE
                binding.btnShare.visibility = View.GONE
                binding.btnDecrypt.visibility = View.GONE
                binding.ivQrCode.visibility = View.GONE
            }
        }

        binding.btnCopy.setOnClickListener {
            val text = binding.tvDecrypted.text.toString()
            if (text.isNotBlank()) {
                val clipboard =
                    ContextCompat.getSystemService(requireContext(), ClipboardManager::class.java)
                val clip = ClipData.newPlainText(requireContext().resources.getString(R.string.decrypted_text), text)
                clipboard?.setPrimaryClip(clip)
                Toast.makeText(requireContext(), requireContext().resources.getString(R.string.copied), Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnShare.setOnClickListener {
            val text = binding.tvDecrypted.text.toString()
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