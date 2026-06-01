package com.template.android.engagement

import android.app.Activity
import com.google.android.play.core.review.ReviewManagerFactory
import com.template.android.analytics.AppAnalytics

object PlayReviewHelper {

    /** Llama cuando quieras pedir valoración (p. ej. tras varias sesiones en la app). */
    fun requestReview(activity: Activity) {
        val manager = ReviewManagerFactory.create(activity)
        manager.requestReviewFlow().addOnCompleteListener { task ->
            if (!task.isSuccessful) return@addOnCompleteListener
            manager.launchReviewFlow(activity, task.result)
            AppAnalytics.logReviewRequested()
        }
    }
}
