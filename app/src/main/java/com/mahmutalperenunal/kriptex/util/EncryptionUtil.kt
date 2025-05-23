package com.mahmutalperenunal.kriptex.util

import android.util.Base64
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

object EncryptionUtil {
    private const val SECRET_KEY = "1234567890123456"
    private const val INIT_VECTOR = "abcdefghijklmnop"

    fun encrypt(input: String): String {
        val iv = IvParameterSpec(INIT_VECTOR.toByteArray(Charsets.UTF_8))
        val skeySpec = SecretKeySpec(SECRET_KEY.toByteArray(Charsets.UTF_8), "AES")

        val cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING")
        cipher.init(Cipher.ENCRYPT_MODE, skeySpec, iv)

        val encrypted = cipher.doFinal(input.toByteArray())
        return Base64.encodeToString(encrypted, Base64.DEFAULT)
    }

    fun decrypt(encrypted: String): String {
        val iv = IvParameterSpec(INIT_VECTOR.toByteArray(Charsets.UTF_8))
        val skeySpec = SecretKeySpec(SECRET_KEY.toByteArray(Charsets.UTF_8), "AES")

        val cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING")
        cipher.init(Cipher.DECRYPT_MODE, skeySpec, iv)

        val original = cipher.doFinal(Base64.decode(encrypted, Base64.DEFAULT))
        return String(original)
    }
}