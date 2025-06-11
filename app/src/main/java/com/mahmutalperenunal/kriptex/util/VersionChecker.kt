package com.mahmutalperenunal.kriptex.util

import android.app.Activity
import android.content.Context
import com.google.android.play.core.appupdate.*
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

object VersionChecker {
    private var appUpdateManager: AppUpdateManager? = null

    private fun getAppUpdateManager(context: Context): AppUpdateManager {
        return appUpdateManager ?: AppUpdateManagerFactory.create(context).also {
            appUpdateManager = it
        }
    }

    suspend fun checkForUpdate(context: Context): UpdateInfo? = withContext(Dispatchers.IO) {
        try {
            val updateManager = getAppUpdateManager(context)
            val appUpdateInfo = updateManager.appUpdateInfo.await()

            return@withContext if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE) {
                UpdateInfo(
                    appUpdateInfo,
                    appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE),
                    appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)
                )
            } else null
        } catch (e: Exception) {
            null
        }
    }

    suspend fun startUpdate(activity: Activity, updateInfo: UpdateInfo) = withContext(Dispatchers.Main) {
        try {
            val updateManager = getAppUpdateManager(activity)
            val appUpdateOptions = AppUpdateOptions.newBuilder(
                if (updateInfo.isImmediateUpdateAvailable) AppUpdateType.IMMEDIATE else AppUpdateType.FLEXIBLE
            ).build()

            updateManager.startUpdateFlowForResult(
                updateInfo.appUpdateInfo,
                activity,
                appUpdateOptions,
                1001
            )
        } catch (_: Exception) { }
    }

    data class UpdateInfo(
        val appUpdateInfo: AppUpdateInfo,
        val isFlexibleUpdateAvailable: Boolean,
        val isImmediateUpdateAvailable: Boolean
    )
}