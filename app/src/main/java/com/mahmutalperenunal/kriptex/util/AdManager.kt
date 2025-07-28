package com.mahmutalperenunal.kriptex.util

import android.app.Activity
import android.util.Log
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback

object AdManager {
    private var actionCount = 0
    private const val ADD_UNIT_ID = "ca-app-pub-xxxxxxxxxxxxxxxx/zzzzzzzzzz"

    fun recordActionAndShowAdIfNeeded(activity: Activity, maxActions: Int = 3) {
        if (BillingHelper.isAdsRemoved()) return

        actionCount++
        if (actionCount >= maxActions) {
            showAd(activity)
            actionCount = 0
        }
    }

    private fun showAd(activity: Activity) {
        val adRequest = AdRequest.Builder().build()
        InterstitialAd.load(
            activity,
            ADD_UNIT_ID,
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    ad.show(activity)
                }

                override fun onAdFailedToLoad(error: com.google.android.gms.ads.LoadAdError) {
                    Log.e("AdManager", "Interstitial ad failed to load: ${error.message}")
                }
            }
        )
    }
}