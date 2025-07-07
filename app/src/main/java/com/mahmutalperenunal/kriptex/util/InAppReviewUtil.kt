package com.mahmutalperenunal.kriptex.util

import android.app.Activity
import android.content.Context
import com.google.android.play.core.review.ReviewManagerFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import androidx.core.content.edit

object InAppReviewUtil {

    private const val PREF_NAME = "in_app_review_prefs"
    private const val KEY_LAST_REVIEW_TIME = "last_review_time"
    private const val KEY_LAUNCH_COUNT = "launch_count"
    private const val KEY_ACTION_COUNT = "action_count"
    private const val KEY_USER_REVIEWED = "user_reviewed"

    private const val MIN_INTERVAL_DAYS = 3L
    private const val MIN_LAUNCH_COUNT = 3

    fun onAppLaunched(context: Context) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val newCount = prefs.getInt(KEY_LAUNCH_COUNT, 0) + 1
        prefs.edit { putInt(KEY_LAUNCH_COUNT, newCount) }
    }

    fun onImportantActionDone(context: Context) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val newCount = prefs.getInt(KEY_ACTION_COUNT, 0) + 1
        prefs.edit { putInt(KEY_ACTION_COUNT, newCount) }
    }

    fun requestReviewIfEligible(context: Context, activity: Activity) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val launchCount = prefs.getInt(KEY_LAUNCH_COUNT, 0)
        val userReviewed = prefs.getBoolean(KEY_USER_REVIEWED, false)
        val lastReviewTime = prefs.getLong(KEY_LAST_REVIEW_TIME, 0L)
        val now = System.currentTimeMillis()
        val daysPassed = now - lastReviewTime > MIN_INTERVAL_DAYS * 24 * 60 * 60 * 1000L

        val isEligible = when {
            userReviewed -> false
            daysPassed -> true
            launchCount >= MIN_LAUNCH_COUNT -> true
            else -> false
        }

        if (isEligible) {
            CoroutineScope(Dispatchers.Main).launch {
                val manager = ReviewManagerFactory.create(context)
                val request = manager.requestReviewFlow()
                request.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val reviewInfo = task.result
                        manager.launchReviewFlow(activity, reviewInfo)
                        prefs.edit {
                            putLong(KEY_LAST_REVIEW_TIME, now)
                            putBoolean(KEY_USER_REVIEWED, true)
                        }
                    }
                }
            }
        }
    }
}