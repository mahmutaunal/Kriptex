package com.mahmutalperenunal.kriptex.util

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import androidx.core.content.edit
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.ProductDetailsResponseListener
import com.android.billingclient.api.QueryProductDetailsResult
import com.mahmutalperenunal.kriptex.R

object BillingHelper : PurchasesUpdatedListener {

    private var billingClient: BillingClient? = null
    private var purchaseCallback: ((Boolean) -> Unit)? = null
    private lateinit var prefs: SharedPreferences

    private const val PRODUCT_ID_REMOVE_ADS = ""

    fun init(context: Context, onPurchaseUpdated: (Boolean) -> Unit, onPurchaseChecked: ((Boolean) -> Unit)? = null) {
        prefs = context.applicationContext.getSharedPreferences("prefs", Context.MODE_PRIVATE)
        this.purchaseCallback = onPurchaseUpdated

        if (billingClient == null) {
            billingClient = BillingClient.newBuilder(context)
                .setListener(this)
                .enablePendingPurchases(
                    PendingPurchasesParams.newBuilder()
                        .enableOneTimeProducts()
                        .build()
                )
                .enableAutoServiceReconnection()
                .build()

            billingClient?.startConnection(object : BillingClientStateListener {
                override fun onBillingSetupFinished(billingResult: BillingResult) {
                    if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                        queryPurchases(onPurchaseChecked)
                    }
                }

                override fun onBillingServiceDisconnected() {
                    billingClient?.startConnection(this)
                }
            })
        }
    }

    fun launchPurchaseFlow(activity: Activity, context: Context, onError: ((String) -> Unit)? = null) {
        if (billingClient?.isReady != true) {
            onError?.invoke(context.getString(R.string.error_billing_not_ready))
            return
        }

        val product = QueryProductDetailsParams.Product.newBuilder()
            .setProductId(PRODUCT_ID_REMOVE_ADS)
            .setProductType(BillingClient.ProductType.INAPP)
            .build()

        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(listOf(product))
            .build()

        billingClient?.queryProductDetailsAsync(params) { billingResult, productDetailsList ->
            if (billingResult.responseCode != BillingClient.BillingResponseCode.OK) {
                onError?.invoke("${context.getString(R.string.error)}: ${billingResult.debugMessage}")
                return@queryProductDetailsAsync
            }

            val product = QueryProductDetailsParams.Product.newBuilder()
                .setProductId(PRODUCT_ID_REMOVE_ADS)
                .setProductType(BillingClient.ProductType.INAPP)
                .build()

            val params = QueryProductDetailsParams.newBuilder()
                .setProductList(listOf(product))
                .build()

            billingClient?.queryProductDetailsAsync(params, object : ProductDetailsResponseListener {
                override fun onProductDetailsResponse(
                    p0: BillingResult,
                    p1: QueryProductDetailsResult
                ) {
                    if (billingResult.responseCode != BillingClient.BillingResponseCode.OK) {
                        onError?.invoke("${context.getString(R.string.error)}: ${billingResult.debugMessage}")
                        return
                    }

                    val details = p1.productDetailsList.firstOrNull()
                    if (details == null) {
                        onError?.invoke(context.getString(R.string.product_details_not_found))
                        return
                    }

                    val productDetailsParams = BillingFlowParams.ProductDetailsParams.newBuilder()
                        .setProductDetails(details)
                        .build()

                    val flowParams = BillingFlowParams.newBuilder()
                        .setProductDetailsParamsList(listOf(productDetailsParams))
                        .build()

                    val result = billingClient?.launchBillingFlow(activity, flowParams)

                    if (result == null) {
                        onError?.invoke(context.getString(R.string.billing_launch_null))
                    } else if (result.responseCode != BillingClient.BillingResponseCode.OK) {
                        onError?.invoke("${context.getString(R.string.error)}: ${result.debugMessage} (responseCode: $result.responseCode)")
                    }
                }
            })
        }
    }

    override fun onPurchasesUpdated(result: BillingResult, purchases: MutableList<Purchase>?) {
        if (result.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
            for (purchase in purchases) {
                if (purchase.products.contains(PRODUCT_ID_REMOVE_ADS)) {
                    if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED && !purchase.isAcknowledged) {
                        val ackParams = AcknowledgePurchaseParams.newBuilder()
                            .setPurchaseToken(purchase.purchaseToken)
                            .build()

                        billingClient?.acknowledgePurchase(ackParams) { ackResult ->
                            if (ackResult.responseCode == BillingClient.BillingResponseCode.OK) {
                                saveState(true)
                                purchaseCallback?.invoke(true)
                            }
                        }
                    } else {
                        saveState(true)
                        purchaseCallback?.invoke(true)
                    }
                }
            }
        } else {
            purchaseCallback?.invoke(false)
        }
    }

    private fun queryPurchases(onResult: ((Boolean) -> Unit)? = null) {
        billingClient?.queryPurchasesAsync(
            QueryPurchasesParams.newBuilder()
                .setProductType(BillingClient.ProductType.INAPP)
                .build()
        ) { _, purchases ->
            val matching = purchases.firstOrNull { it.products.contains(PRODUCT_ID_REMOVE_ADS) }

            if (matching != null) {
                if (!matching.isAcknowledged) {
                    val ackParams = AcknowledgePurchaseParams.newBuilder()
                        .setPurchaseToken(matching.purchaseToken)
                        .build()

                    billingClient?.acknowledgePurchase(ackParams) { ackResult ->
                        if (ackResult.responseCode == BillingClient.BillingResponseCode.OK) {
                            saveState(true)
                            onResult?.invoke(true)
                        } else {
                            onResult?.invoke(false)
                        }
                    }
                } else {
                    saveState(true)
                    onResult?.invoke(true)
                }
            } else {
                saveState(false)
                onResult?.invoke(false)
            }
        }
    }

    fun isAdsRemoved(): Boolean {
        return prefs.getBoolean("ads_removed", false)
    }

    private fun saveState(state: Boolean) {
        prefs.edit { putBoolean("ads_removed", state) }
    }
}