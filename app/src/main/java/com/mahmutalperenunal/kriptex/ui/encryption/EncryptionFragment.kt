package com.mahmutalperenunal.kriptex.ui.encryption

import android.Manifest
import android.app.Activity
import android.app.DatePickerDialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
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
import android.widget.AutoCompleteTextView
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
import com.mahmutalperenunal.kriptex.util.EncryptionType
import com.mahmutalperenunal.kriptex.util.EncryptionUtil
import com.mahmutalperenunal.kriptex.util.QrUtils
import com.mahmutalperenunal.kriptex.util.ShareUtils
import kotlinx.coroutines.launch
import java.util.Calendar
import androidx.core.net.toUri
import com.google.android.material.textfield.TextInputLayout
import com.mahmutalperenunal.kriptex.util.AdManager

class EncryptionFragment : Fragment() {

    private var _binding: FragmentEncryptionBinding? = null
    private val binding get() = _binding!!

    private lateinit var db: AppDatabase

    private lateinit var qrBitmap: Bitmap

    private lateinit var fab: FloatingActionButton

    private var isDateClickSet = false

    private val cameraPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            findNavController().navigate(R.id.action_encryption_to_qrScanner)
        } else {
            Toast.makeText(
                requireContext(),
                requireContext().resources.getString(R.string.camera_permission_required),
                Toast.LENGTH_SHORT
            ).show()
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

        val typeLabels = EncryptionType.entries.map { it.getLocalizedLabel(requireContext()) }

        val adapter =
            ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, typeLabels)
        binding.actEncryptionType.setAdapter(adapter)
        binding.actEncryptionType.setText(typeLabels.first(), false)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEncryptionBinding.inflate(inflater, container, false)

        db = AppDatabase.getDatabase(requireContext())

        fab = requireActivity().findViewById(R.id.fab)

        binding.actEncryptionType.setOnItemClickListener { _, _, position, _ ->
            val selectedType = EncryptionType.entries[position]
            updateInputFieldsForType(selectedType)
        }

        binding.etPlainText.addTextChangedListener { editable ->
            val inputText = editable?.toString() ?: ""

            if (inputText.isNotBlank()) fab.show() else fab.hide()

            if (binding.tlPlainText.error != null) {
                binding.tlPlainText.error = null
            }
        }

        fab.setOnClickListener {
            AdManager.recordActionAndShowAdIfNeeded(context as Activity, 3)

            val rawInput = binding.etPlainText.text.toString()
            val selectedLabel = binding.actEncryptionType.text.toString()
            val selectedType = EncryptionType.entries.firstOrNull {
                it.getLocalizedLabel(requireContext()) == selectedLabel
            } ?: EncryptionType.TEXT
            val formattedInput = formatInputByType(selectedType, rawInput)

            try {
                val result = EncryptionUtil.encrypt(formattedInput)

                binding.tvEncrypted.text = result

                qrBitmap = QrUtils.generate(result, context = requireContext())
                binding.ivQrCode.setImageBitmap(qrBitmap)

                binding.ivQrCode.visibility = View.VISIBLE
                binding.btnCopy.visibility = View.VISIBLE
                binding.btnShare.visibility = View.VISIBLE

                lifecycleScope.launch {
                    db.encryptedTextDao().insert(
                        EncryptedText(
                            originalText = formattedInput,
                            encryptedText = result,
                            qrContent = result,
                            type = selectedType.name
                        )
                    )
                }

                binding.tlPlainText.error = null

                Toast.makeText(
                    requireContext(),
                    getString(R.string.encrypt_success),
                    Toast.LENGTH_SHORT
                ).show()
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
                val clip = ClipData.newPlainText(
                    requireContext().resources.getString(R.string.encrypted_text),
                    text
                )
                clipboard?.setPrimaryClip(clip)
                Toast.makeText(
                    requireContext(),
                    requireContext().resources.getString(R.string.copied),
                    Toast.LENGTH_SHORT
                ).show()
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

    private fun updateInputFieldsForType(type: EncryptionType) {
        binding.tlInput2.visibility = View.GONE
        binding.tlInput3.visibility = View.GONE

        binding.etInput2.setOnClickListener(null)
        binding.etInput2.isFocusable = true
        binding.etInput2.isFocusableInTouchMode = true

        binding.etInput3.setOnClickListener(null)
        binding.etInput3.isFocusable = true
        binding.etInput3.isFocusableInTouchMode = true

        when (type) {
            EncryptionType.TEXT, EncryptionType.URL, EncryptionType.EMAIL,
            EncryptionType.PHONE, EncryptionType.SMS -> {
                binding.tlPlainText.hint = getString(R.string.enter_text)
            }

            EncryptionType.GEO -> {
                binding.tlInput3.hint = getString(R.string.enter_coordinates)

                binding.tlInput3.setOnClickListener {
                    showLocationInputOptions()
                }

                binding.tlPlainText.visibility = View.GONE
                binding.tlInput2.visibility = View.GONE
                binding.tlInput3.visibility = View.VISIBLE
            }

            EncryptionType.WIFI -> {
                val wifiTypes = listOf("WPA", "WEP", "nopass")
                val wifiAdapter = ArrayAdapter(
                    requireContext(),
                    android.R.layout.simple_dropdown_item_1line,
                    wifiTypes
                )
                (binding.etInput3 as? AutoCompleteTextView)?.setAdapter(wifiAdapter)
                (binding.etInput3 as? AutoCompleteTextView)?.setText(wifiTypes.first(), false)

                binding.tlPlainText.endIconMode = TextInputLayout.END_ICON_NONE
                binding.tlPlainText.hint = getString(R.string.wifi_ssid)
                binding.tlInput2.hint = getString(R.string.wifi_password)
                binding.tlInput3.hint = getString(R.string.wifi_type)
                binding.tlInput2.visibility = View.VISIBLE
                binding.tlInput3.visibility = View.VISIBLE
            }

            EncryptionType.VCARD -> {
                binding.tlPlainText.endIconMode = TextInputLayout.END_ICON_NONE
                binding.tlPlainText.hint = getString(R.string.name)
                binding.tlInput2.hint = getString(R.string.phone)
                binding.tlInput3.hint = getString(R.string.email)
                binding.tlInput2.visibility = View.VISIBLE
                binding.tlInput3.visibility = View.VISIBLE
            }

            EncryptionType.EVENT -> {
                binding.tlPlainText.endIconMode = TextInputLayout.END_ICON_NONE
                binding.tlPlainText.hint = getString(R.string.title)
                binding.tlInput2.hint = getString(R.string.date)
                binding.tlInput3.hint = getString(R.string.location)
                binding.tlInput2.visibility = View.VISIBLE
                binding.tlInput3.visibility = View.VISIBLE

                if (!isDateClickSet) {
                    binding.etInput2.setOnClickListener {
                        showDatePicker { selectedDate ->
                            binding.etInput2.setText(selectedDate)
                        }
                    }
                    isDateClickSet = true
                }

                binding.etInput3.setOnClickListener {
                    val intent = Intent(Intent.ACTION_VIEW).apply {
                        data = "geo:0,0?q=".toUri()
                    }
                    try {
                        startActivity(intent)
                    } catch (e: Exception) {
                        Toast.makeText(requireContext(), getString(R.string.no_map_app_found), Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun showLocationInputOptions() {
        QrInputBottomSheet().apply {
            onCameraSelected = {
                val intent = Intent(Intent.ACTION_VIEW).apply {
                    data = "geo:0,0?q=".toUri()
                }
                try {
                    startActivity(intent)
                } catch (e: Exception) {
                    Toast.makeText(requireContext(), getString(R.string.no_map_app_found), Toast.LENGTH_SHORT).show()
                }
            }
            onGallerySelected = {
                Toast.makeText(requireContext(), getString(R.string.enter_coordinates_manually), Toast.LENGTH_SHORT).show()
            }
        }.show(parentFragmentManager, "LocationInputSheet")
    }

    private fun showDatePicker(onDateSelected: (String) -> Unit) {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePicker = DatePickerDialog(requireContext(), { _, selectedYear, selectedMonth, selectedDay ->
                val selectedDate = String.format("%04d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDay)
                onDateSelected(selectedDate)
            }, year, month, day)

        datePicker.show()
    }

    private fun formatInputByType(type: EncryptionType, input: String): String {
        val input2 = binding.etInput2.text?.toString().orEmpty()
        val input3 = binding.etInput3.text?.toString().orEmpty()
        val context = requireContext()

        return when (type) {
            EncryptionType.TEXT -> input
            EncryptionType.URL -> context.getString(R.string.readable_url, input)
            EncryptionType.EMAIL -> context.getString(R.string.readable_email, input)
            EncryptionType.PHONE -> {
                val masked = maskPhoneNumber(input)
                context.getString(R.string.readable_phone, masked)
            }
            EncryptionType.SMS -> context.getString(R.string.readable_sms, input)
            EncryptionType.WIFI -> context.getString(R.string.readable_wifi, input, input2, input3)
            EncryptionType.GEO -> context.getString(R.string.readable_geo, input)
            EncryptionType.VCARD -> {
                val maskedPhone = maskPhoneNumber(input2)
                context.getString(R.string.readable_vcard, input, maskedPhone, input3)
            }
            EncryptionType.EVENT -> context.getString(R.string.readable_event, input, input2, input3)
        }
    }

    private fun maskPhoneNumber(number: String): String {
        val digits = number.filter { it.isDigit() }
        if (digits.length < 4) return "*".repeat(digits.length)

        val lastTwo = digits.takeLast(2)
        val maskedLength = digits.length - 2
        val masked = "*".repeat(maskedLength)

        return buildString {
            append(masked.chunked(3).joinToString(" "))
            append(" ")
            append(lastTwo)
        }.trim()
    }

    private fun checkCameraPermissionAndNavigate() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED
        ) {
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
                    Toast.makeText(
                        requireContext(),
                        requireContext().resources.getString(R.string.qr_scan_failed),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            .addOnFailureListener {
                Toast.makeText(
                    requireContext(),
                    requireContext().resources.getString(R.string.invalid_qr_image),
                    Toast.LENGTH_SHORT
                ).show()
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