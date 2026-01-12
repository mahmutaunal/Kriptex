package com.mahmutalperenunal.kriptex

import android.content.Context
import android.os.Bundle
import android.view.Menu
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import com.mahmutalperenunal.kriptex.databinding.ActivityMainBinding
import com.mahmutalperenunal.kriptex.util.LocalizationHelper
import com.mahmutalperenunal.kriptex.util.ThemeHelper

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController

    var isFilterVisible: Boolean = false
    var isSearchVisible: Boolean = false
    var isThemeVisible: Boolean = false
    var isLanguageVisible: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        ThemeHelper.applyTheme(this)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.tbHeader)

        setupNavigation()
        setupSystemBars()
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        menu?.findItem(R.id.action_filter)?.isVisible = isFilterVisible
        menu?.findItem(R.id.action_search)?.isVisible = isSearchVisible
        menu?.findItem(R.id.action_theme)?.isVisible = isThemeVisible
        menu?.findItem(R.id.action_language)?.isVisible = isLanguageVisible
        return super.onPrepareOptionsMenu(menu)
    }

    private fun setupNavigation() {
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController
        NavigationUI.setupWithNavController(binding.bottomNav, navController)
    }

    private fun setupSystemBars() {
        window.statusBarColor = getColor(R.color.background_color)
        window.navigationBarColor = getColor(R.color.bottom_navigation_bar_color)

        WindowCompat.getInsetsController(window, window.decorView).let { controller ->
            val isDarkTheme = resources.configuration.uiMode and android.content.res.Configuration.UI_MODE_NIGHT_MASK ==
                    android.content.res.Configuration.UI_MODE_NIGHT_YES

            controller.isAppearanceLightStatusBars = !isDarkTheme
            controller.isAppearanceLightNavigationBars = !isDarkTheme
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }

    override fun attachBaseContext(newBase: Context) {
        val localizedContext = LocalizationHelper.applySavedLocale(newBase)
        super.attachBaseContext(localizedContext)
    }
}