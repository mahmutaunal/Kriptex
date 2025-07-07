package com.mahmutalperenunal.kriptex.ui.encryption

import android.Manifest
import android.content.ClipData
import android.content.ClipboardManager
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.setFragmentResultListener
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import com.mahmutalperenunal.kriptex.R
import com.mahmutalperenunal.kriptex.databinding.FragmentEncryptionBinding
import com.mahmutalperenunal.kriptex.data.AppDatabase
import com.mahmutalperenunal.kriptex.data.model.EncryptedText
import com.mahmutalperenunal.kriptex.ui.qr.QrInputBottomSheet
import com.mahmutalperenunal.kriptex.util.EncryptionUtil
import com.mahmutalperenunal.kriptex.util.QrUtils
import com.mahmutalperenunal.kriptex.util.ShareUtils
import kotlinx.coroutines.launch

class EncryptionFragment : Fragment() {

    private var _binding: FragmentEncryptionBinding? = null
    private val binding get() = _binding!!

    private lateinit var db: AppDatabase

    private lateinit var qrBitmap: Bitmap

    private lateinit var fab: FloatingActionButton

    private val cameraPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            findNavController().navigate(R.id.action_encryption_to_qrScanner)
        } else {
            Toast.makeText(requireContext(), requireContext().resources.getString(R.string.camera_permission_required), Toast.LENGTH_SHORT).show()
        }
    }

    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            processQrImageFromGallery(uri)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setFragmentResultListener("qr_scan_result") { _, bundle ->
            val scannedText = bundle.getString("scanned_text").orEmpty()
            binding.etPlainText.setText(scannedText)
            fab.performClick()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEncryptionBinding.inflate(inflater, container, false)

        db = AppDatabase.getDatabase(requireContext())

        fab = requireActivity().findViewById(R.id.fab)

        binding.etPlainText.addTextChangedListener { editable ->
            val inputText = editable?.toString() ?: ""

            if (inputText.isNotBlank()) fab.show() else fab.hide()

            if (binding.tlPlainText.error != null) {
                binding.tlPlainText.error = null
            }
        }

        fab.setOnClickListener {
            val input = binding.etPlainText.text.toString()
            try {
                val result = EncryptionUtil.encrypt(input)

                binding.tvEncrypted.text = result

                qrBitmap = QrUtils.generate(result, context = requireContext())
                binding.ivQrCode.setImageBitmap(qrBitmap)

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

                binding.tlPlainText.error = null

                Toast.makeText(requireContext(), getString(R.string.encrypt_success), Toast.LENGTH_SHORT).show()
                fab.hide()
            } catch (e: Exception) {
                binding.tlPlainText.error = getString(R.string.incorrect_text_or_encryption)
                binding.tvEncrypted.text = ""
                binding.ivQrCode.setImageDrawable(null)
                binding.btnCopy.visibility = View.GONE
                binding.btnShare.visibility = View.GONE
                fab.hide()
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
                val qrBitmapForShare = QrUtils.generateQrCodeForSharing(text, Color.BLACK)
                ShareUtils.shareTextWithQrCode(requireContext(), text, qrBitmapForShare)
            }
        }

        binding.tlPlainText.setEndIconOnClickListener {
            QrInputBottomSheet().apply {
                onCameraSelected = { checkCameraPermissionAndNavigate() }
                onGallerySelected = { checkGalleryPermissionAndPick() }
            }.show(parentFragmentManager, "QrInputSheet")
        }

        return binding.root
    }

    private fun checkCameraPermissionAndNavigate() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED) {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        } else {
            findNavController().navigate(R.id.action_encryption_to_qrScanner)
        }
    }

    private fun checkGalleryPermissionAndPick() {
        imagePickerLauncher.launch("image/*")
    }

    private fun processQrImageFromGallery(uri: Uri) {
        val source = InputImage.fromFilePath(requireContext(), uri)
        val scanner = BarcodeScanning.getClient()

        scanner.process(source)
            .addOnSuccessListener { barcodes ->
                val value = barcodes.firstOrNull()?.rawValue
                if (value != null) {
                    setFragmentResult("qr_scan_result", Bundle().apply {
                        putString("scanned_text", value)
                    })
                    findNavController().popBackStack()
                } else {
                    Toast.makeText(requireContext(), requireContext().resources.getString(R.string.qr_scan_failed), Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), requireContext().resources.getString(R.string.invalid_qr_image), Toast.LENGTH_SHORT).show()
            }
    }

    override fun onResume() {
        super.onResume()
        if (binding.etPlainText.text?.isNotBlank() == true) fab.show() else fab.hide()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        fab.hide()
    }
}