package com.mahmutalperenunal.kriptex.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import androidx.core.graphics.createBitmap
import androidx.core.graphics.set
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel

object QrUtils {

    fun generate(text: String, size: Int = 512, context: Context): Bitmap {
        val theme = ThemeHelper.getSavedTheme(context)
        val isDark = when (theme) {
            ThemeHelper.ThemeMode.DARK -> true
            ThemeHelper.ThemeMode.LIGHT -> false
            ThemeHelper.ThemeMode.SYSTEM -> {
                val uiMode = context.resources.configuration.uiMode
                (uiMode and android.content.res.Configuration.UI_MODE_NIGHT_MASK) ==
                        android.content.res.Configuration.UI_MODE_NIGHT_YES
            }
        }

        val foregroundColor = if (isDark) Color.WHITE else Color.BLACK

        val bitMatrix = MultiFormatWriter().encode(text, BarcodeFormat.QR_CODE, size, size)
        val bmp = createBitmap(size, size, Bitmap.Config.ARGB_8888)
        for (x in 0 until size) {
            for (y in 0 until size) {
                bmp[x, y] = if (bitMatrix[x, y]) foregroundColor else Color.TRANSPARENT
            }
        }
        return bmp
    }

    fun generateQrCodeForSharing(text: String, color: Int): Bitmap {
        val writer = QRCodeWriter()
        val hints = mapOf(EncodeHintType.ERROR_CORRECTION to ErrorCorrectionLevel.L)
        val bitMatrix = writer.encode(text, BarcodeFormat.QR_CODE, 512, 512, hints)
        val width = bitMatrix.width
        val height = bitMatrix.height
        val bitmap = createBitmap(width, height)

        val backgroundColor = Color.WHITE

        for (x in 0 until width) {
            for (y in 0 until height) {
                val pixelColor = if (bitMatrix[x, y]) color else backgroundColor
                bitmap[x, y] = pixelColor
            }
        }
        return bitmap
    }
}