package com.mahmutalperenunal.cryptosentinel.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import androidx.core.graphics.createBitmap
import androidx.core.graphics.set

object QrCodeGenerator {

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
}