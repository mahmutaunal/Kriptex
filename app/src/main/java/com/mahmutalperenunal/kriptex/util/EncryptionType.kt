package com.mahmutalperenunal.kriptex.util

import android.content.Context
import com.mahmutalperenunal.kriptex.R

enum class EncryptionType(private val label: String) {
    TEXT("Text"),
    URL("URL"),
    EMAIL("Email"),
    PHONE("Phone"),
    SMS("SMS"),
    WIFI("WiFi"),
    GEO("Geo"),
    VCARD("vCard"),
    EVENT("Event");

    override fun toString(): String = label

    fun getLocalizedLabel(context: Context): String {
        return when (this) {
            TEXT -> context.getString(R.string.type_text)
            URL -> context.getString(R.string.type_url)
            EMAIL -> context.getString(R.string.type_email)
            PHONE -> context.getString(R.string.type_phone)
            SMS -> context.getString(R.string.type_sms)
            WIFI -> context.getString(R.string.type_wifi)
            GEO -> context.getString(R.string.type_geo)
            VCARD -> context.getString(R.string.type_vcard)
            EVENT -> context.getString(R.string.type_event)
        }
    }
}