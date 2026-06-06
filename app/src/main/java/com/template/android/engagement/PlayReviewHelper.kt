package com.template.android.engagement

import android.app.Activity
import com.google.android.play.core.review.ReviewManagerFactory
import com.template.android.analytics.AppAnalytics
import com.template.android.data.AppDataStore

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
}
