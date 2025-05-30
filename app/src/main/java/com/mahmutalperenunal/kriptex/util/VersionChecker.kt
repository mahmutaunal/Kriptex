package com.mahmutalperenunal.kriptex.util

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup

object VersionChecker {

    private const val PLAY_STORE_URL = "https://play.google.com/store/apps/details?id=com.mahmutalperenunal.kriptex"

    suspend fun getLatestVersion(): String? {
        return withContext(Dispatchers.IO) {
            try {
                val doc = Jsoup.connect(PLAY_STORE_URL)
                    .timeout(5000)
                    .get()
                val elements = doc.select("div.hAyfc span.htlgb")
                val versionElement = elements[5]
                versionElement.text()
            } catch (e: Exception) {
                null
            }
        }
    }
}