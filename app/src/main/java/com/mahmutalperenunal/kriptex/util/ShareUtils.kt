package com.mahmutalperenunal.kriptex.util

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream

object ShareUtils {
    fun shareTextWithQrCode(context: Context, text: String, qrBitmap: Bitmap) {
        val cachePath = File(context.cacheDir, "images").apply { mkdirs() }
        val file = File(cachePath, "qr_shared.png")
        FileOutputStream(file).use {
            qrBitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
        }

        val uri: Uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )

        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "image/*"
            putExtra(Intent.EXTRA_TEXT, text)
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        context.startActivity(Intent.createChooser(shareIntent, "Share via"))
    }
}