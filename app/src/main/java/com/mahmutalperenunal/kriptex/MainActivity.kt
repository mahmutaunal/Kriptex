package com.mahmutalperenunal.kriptex

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.view.View
import android.widget.Toast
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
import com.mahmutalperenunal.kriptex.ui.settings.BottomSheetLanguage
import com.mahmutalperenunal.kriptex.ui.settings.BottomSheetTheme
import com.mahmutalperenunal.kriptex.util.BillingHelper
import com.mahmutalperenunal.kriptex.util.InAppReviewUtil

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController

    private lateinit var bannerAds: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        ThemeHelper.applyTheme(this)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        checkVersionIfConnected()

        setupNavigation()
        setupSystemBars()
        setupToolbar()
        setupBilling()
        setupAds()
    }

    override fun onResume() {
        super.onResume()
        if (isNetworkAvailable()) {
            InAppReviewUtil.onAppLaunched(applicationContext)
            InAppReviewUtil.requestReviewIfEligible(applicationContext, this)
        }
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

    private fun setupToolbar() {
        binding.tbHeader.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.actionTheme -> {
                    BottomSheetTheme().show(supportFragmentManager, "ThemeSheet")
                    true
                }
                R.id.actionLanguage -> {
                    BottomSheetLanguage().show(supportFragmentManager, "LanguageSheet")
                    true
                }
                else -> false
            }
        }
    }

    private fun setupAds() {
        bannerAds = binding.adView
        val adRequest = AdRequest.Builder().build()
        binding.adView.loadAd(adRequest)

        if (BillingHelper.isAdsRemoved()) {
            bannerAds.visibility = View.GONE
        }
    }

    private fun setupBilling() {
        BillingHelper.init(
            context = this,
            onPurchaseUpdated = { isSuccess ->
                if (isSuccess) {
                    hideBannerAd()
                    Toast.makeText(this, getString(R.string.ads_removed_successfully), Toast.LENGTH_SHORT).show()
                } else {
                    showBannerAd()
                    Toast.makeText(this, getString(R.string.purchase_failed), Toast.LENGTH_SHORT).show()
                }
            },
            onPurchaseChecked = { isPurchased ->
                if (isPurchased) hideBannerAd() else showBannerAd()
            }
        )
    }

    private fun hideBannerAd() {
        bannerAds.visibility = View.GONE
    }

    private fun showBannerAd() {
        if (!BillingHelper.isAdsRemoved()) {
            bannerAds.visibility = View.VISIBLE
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