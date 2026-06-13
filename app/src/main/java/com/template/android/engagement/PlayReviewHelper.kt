package com.template.android.engagement

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import com.google.android.play.core.review.ReviewManagerFactory
import com.template.android.analytics.AppAnalytics
import com.template.android.data.AppDataStore
import com.template.android.ui.sudoku.ShareHelper

object PlayReviewHelper {

    fun maybeRequestReview(activity: Activity, dataStore: AppDataStore) {
        val stats = dataStore.loadStats()
        if (!dataStore.shouldRequestReviewAfterWin(stats)) return

        val manager = ReviewManagerFactory.create(activity)
        val request = manager.requestReviewFlow()
        request.addOnCompleteListener { task ->
            if (!task.isSuccessful) return@addOnCompleteListener
            val reviewInfo = task.result
            manager.launchReviewFlow(activity, reviewInfo)
            dataStore.setReviewPrompted()
            AppAnalytics.logReviewRequested()
        }
    }

    /** Abre Play Store; marcar [dataStore.setReviewPrompted] desde UI para ocultar el botón. */
    fun requestReviewFromSettings(activity: Activity) {
        openPlayStoreListing(activity)
    }

    private fun openPlayStoreListing(activity: Activity) {
        val webUri = Uri.parse(ShareHelper.playStoreUrl())
        val marketUri = Uri.parse("market://details?id=${activity.packageName}")
        try {
            activity.startActivity(Intent(Intent.ACTION_VIEW, marketUri))
        } catch (_: ActivityNotFoundException) {
            try {
                activity.startActivity(Intent(Intent.ACTION_VIEW, webUri))
            } catch (_: ActivityNotFoundException) {
                // Sin Play Store ni navegador
            }
        }
    }
}
