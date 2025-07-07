package com.mahmutalperenunal.kriptex.ui.qr

import android.app.Dialog
import android.os.Bundle
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.mahmutalperenunal.kriptex.databinding.BottomSheetQrInputBinding

class QrInputBottomSheet : BottomSheetDialogFragment() {

    private var _binding: BottomSheetQrInputBinding? = null
    private val binding get() = _binding!!

    var onCameraSelected: (() -> Unit)? = null
    var onGallerySelected: (() -> Unit)? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        _binding = BottomSheetQrInputBinding.inflate(layoutInflater)
        dialog.setContentView(binding.root)

        binding.optionCamera.setOnClickListener {
            onCameraSelected?.invoke()
            dismiss()
        }

        binding.optionGallery.setOnClickListener {
            onGallerySelected?.invoke()
            dismiss()
        }

        return dialog
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}