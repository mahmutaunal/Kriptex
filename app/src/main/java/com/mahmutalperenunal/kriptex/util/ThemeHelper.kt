package com.mahmutalperenunal.kriptex.util

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.edit

object ThemeHelper {
    private const val PREFS_NAME = "theme_prefs"
    private const val KEY_THEME = "selected_theme"

    enum class ThemeMode(val value: Int) {
        LIGHT(AppCompatDelegate.MODE_NIGHT_NO),
        DARK(AppCompatDelegate.MODE_NIGHT_YES),
        SYSTEM(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
    }

    fun applyTheme(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val themeValue = prefs.getInt(KEY_THEME, ThemeMode.SYSTEM.value)
        AppCompatDelegate.setDefaultNightMode(themeValue)
    }

    fun setTheme(context: Context, mode: ThemeMode) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit { putInt(KEY_THEME, mode.value) }
        AppCompatDelegate.setDefaultNightMode(mode.value)
    }

    fun getSavedTheme(context: Context): ThemeMode {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val themeValue = prefs.getInt(KEY_THEME, ThemeMode.SYSTEM.value)
        return ThemeMode.entries.firstOrNull { it.value == themeValue } ?: ThemeMode.SYSTEM
    }
}