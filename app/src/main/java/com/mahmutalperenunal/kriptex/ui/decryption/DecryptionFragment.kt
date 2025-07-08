package com.mahmutalperenunal.kriptex.ui.decryption

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
import android.widget.ArrayAdapter
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
import com.mahmutalperenunal.kriptex.data.AppDatabase
import com.mahmutalperenunal.kriptex.data.model.EncryptedText
import com.mahmutalperenunal.kriptex.databinding.FragmentDecryptionBinding
import com.mahmutalperenunal.kriptex.ui.qr.QrInputBottomSheet
import com.mahmutalperenunal.kriptex.util.EncryptionType
import com.mahmutalperenunal.kriptex.util.EncryptionUtil
import com.mahmutalperenunal.kriptex.util.QrUtils
import com.mahmutalperenunal.kriptex.util.ShareUtils
import kotlinx.coroutines.launch

class DecryptionFragment : Fragment() {

    private var _binding: FragmentDecryptionBinding? = null
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
            binding.etEncryptedText.setText(scannedText)

            val inferredType = inferEncryptionType(scannedText)
            binding.actDecryptionType.setText(inferredType.getLocalizedLabel(requireContext()), false)

            fab.performClick()
        }

        val typeLabels = EncryptionType.entries.map { it.getLocalizedLabel(requireContext()) }

        val typeAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            typeLabels
        )
        binding.actDecryptionType.setAdapter(typeAdapter)
        binding.actDecryptionType.setText(typeLabels.first(), false)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDecryptionBinding.inflate(inflater, container, false)

        db = AppDatabase.getDatabase(requireContext())

        fab = requireActivity().findViewById(R.id.fab)

        binding.etEncryptedText.addTextChangedListener { editable ->
            val inputText = editable?.toString() ?: ""

            if (inputText.isNotBlank()) fab.show() else fab.hide()

            if (binding.tlEncryptedText.error != null) {
                binding.tlEncryptedText.error = null
            }
        }

        fab.setOnClickListener {
            val input = binding.etEncryptedText.text.toString()
            try {
                val result = EncryptionUtil.decrypt(input)

                binding.tvDecrypted.text = result

                qrBitmap = QrUtils.generate(result, context = requireContext())
                binding.ivQrCode.setImageBitmap(qrBitmap)

                binding.ivQrCode.visibility = View.VISIBLE
                binding.btnCopy.visibility = View.VISIBLE
                binding.btnShare.visibility = View.VISIBLE

                lifecycleScope.launch {
                    val selectedLabel = binding.actDecryptionType.text.toString()
                    val selectedType = EncryptionType.entries.firstOrNull {
                        it.getLocalizedLabel(requireContext()) == selectedLabel
                    } ?: EncryptionType.TEXT

                    db.encryptedTextDao().insert(
                        EncryptedText(
                            originalText = result,
                            encryptedText = input,
                            qrContent = result,
                            type = selectedType.name
                        )
                    )
                }

                binding.tlEncryptedText.error = null

                Toast.makeText(requireContext(), getString(R.string.decrypt_success), Toast.LENGTH_SHORT).show()
                fab.hide()
            } catch (e: Exception) {
                binding.tlEncryptedText.error = getString(R.string.incorrect_text_or_encryption)
                binding.tvDecrypted.text = ""
                binding.ivQrCode.setImageDrawable(null)
                binding.btnCopy.visibility = View.GONE
                binding.btnShare.visibility = View.GONE
                fab.hide()
                binding.ivQrCode.visibility = View.GONE
            }
        }

        binding.btnCopy.setOnClickListener {
            val text = binding.tvDecrypted.text.toString()
            if (text.isNotBlank()) {
                val clipboard = ContextCompat.getSystemService(requireContext(), ClipboardManager::class.java)
                val clip = ClipData.newPlainText(requireContext().resources.getString(R.string.decrypted_text), text)
                clipboard?.setPrimaryClip(clip)
                Toast.makeText(requireContext(), requireContext().resources.getString(R.string.copied), Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnShare.setOnClickListener {
            val text = binding.tvDecrypted.text.toString()
            if (text.isNotBlank()) {
                val qrBitmapForShare = QrUtils.generateQrCodeForSharing(text, Color.BLACK)
                ShareUtils.shareTextWithQrCode(requireContext(), text, qrBitmapForShare)
            }
        }

        binding.tlEncryptedText.setEndIconOnClickListener {
            QrInputBottomSheet().apply {
                onCameraSelected = { checkCameraPermissionAndNavigate() }
                onGallerySelected = { checkGalleryPermissionAndPick() }
            }.show(parentFragmentManager, "QrInputSheet")
        }

        return binding.root
    }

    private fun inferEncryptionType(text: String): EncryptionType {
        return when {
            text.startsWith("mailto:", true) -> EncryptionType.EMAIL
            text.startsWith("tel:", true) -> EncryptionType.PHONE
            text.startsWith("sms:", true) -> EncryptionType.SMS
            text.startsWith("WIFI:", true) -> EncryptionType.WIFI
            text.startsWith("geo:", true) -> EncryptionType.GEO
            text.startsWith("BEGIN:VCARD", true) -> EncryptionType.VCARD
            text.startsWith("BEGIN:VEVENT", true) -> EncryptionType.EVENT
            text.startsWith("http://", true) || text.startsWith("https://", true) -> EncryptionType.URL
            else -> EncryptionType.TEXT
        }
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
        if (binding.etEncryptedText.text?.isNotBlank() == true) fab.show() else fab.hide()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        fab.hide()
    }
}