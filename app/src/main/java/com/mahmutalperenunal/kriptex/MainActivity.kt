package com.mahmutalperenunal.kriptex

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.mahmutalperenunal.kriptex.databinding.ActivityMainBinding
import com.mahmutalperenunal.kriptex.util.LocalizationHelper
import com.mahmutalperenunal.kriptex.util.ThemeHelper
import com.mahmutalperenunal.kriptex.util.VersionChecker
import kotlinx.coroutines.launch
import com.google.android.gms.ads.AdRequest

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        ThemeHelper.applyTheme(this)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        checkVersionIfConnected()

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        window.statusBarColor = getColor(R.color.background_color)
        window.navigationBarColor = getColor(R.color.navigation_bar_color)

        WindowCompat.getInsetsController(window, window.decorView).let { controller ->
            val isDarkTheme = resources.configuration.uiMode and android.content.res.Configuration.UI_MODE_NIGHT_MASK ==
                    android.content.res.Configuration.UI_MODE_NIGHT_YES

            controller.isAppearanceLightStatusBars = !isDarkTheme
            controller.isAppearanceLightNavigationBars = !isDarkTheme
        }

        NavigationUI.setupWithNavController(binding.bottomNav, navController)

        val adView = binding.adView
        val adRequest = AdRequest.Builder().build()
        adView.loadAd(adRequest)
    }

    private fun Context.checkVersionIfConnected() {
        if (isNetworkAvailable()) {
            lifecycleScope.launch {
                val updateInfo = VersionChecker.checkForUpdate(this@checkVersionIfConnected)
                if (updateInfo != null) {
                    showUpdateDialog(updateInfo)
                }
            }
        }
    }

    private fun Context.isNetworkAvailable(): Boolean {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    private fun showUpdateDialog(updateInfo: VersionChecker.UpdateInfo) {
        MaterialAlertDialogBuilder(this)
            .setTitle(getString(R.string.update_dialog_title))
            .setMessage(getString(R.string.update_dialog_message))
            .setPositiveButton(getString(R.string.update_dialog_positive)) { _, _ ->
                lifecycleScope.launch {
                    VersionChecker.startUpdate(this@MainActivity, updateInfo)
                }
            }
            .setNegativeButton(getString(R.string.update_dialog_negative), null)
            .show()
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }

    override fun attachBaseContext(newBase: Context) {
        val localizedContext = LocalizationHelper.applySavedLocale(newBase)
        super.attachBaseContext(localizedContext)
    }
}