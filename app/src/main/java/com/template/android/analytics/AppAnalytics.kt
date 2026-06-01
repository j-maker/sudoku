package com.template.android.analytics

import android.content.Context
import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import com.template.android.BuildConfig

/**
 * Analytics para un proyecto Firebase/GA4 compartido entre varias apps.
 * Valores en [app.template.properties] → appSlug, appDisplayName.
 */
object AppAnalytics {

    private val appSlug: String get() = BuildConfig.APP_SLUG
    private val appDisplayName: String get() = BuildConfig.APP_DISPLAY_NAME

    private const val MAX_EVENT_NAME_LENGTH = 40

    private var firebase: FirebaseAnalytics? = null

    fun init(context: Context) {
        val appContext = context.applicationContext
        firebase = FirebaseAnalytics.getInstance(appContext).also { analytics ->
            analytics.setDefaultEventParameters(
                Bundle().apply {
                    putString("app_slug", appSlug)
                    putString("app_name", appDisplayName)
                },
            )
            analytics.setUserProperty("app_slug", appSlug)
            analytics.setUserProperty("app_display_name", appDisplayName)
            analytics.setUserProperty("app_package", appContext.packageName)
        }
        log("app_initialized")
    }

    fun logSplashFinished() {
        log("splash_finished")
    }

    fun logScreenView(screen: String) {
        val analytics = firebase ?: return
        val screenName = "${appSlug}_$screen"
        val bundle = Bundle().apply {
            putString("app_slug", appSlug)
            putString(FirebaseAnalytics.Param.SCREEN_NAME, screenName)
            putString(FirebaseAnalytics.Param.SCREEN_CLASS, screenName)
        }
        analytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, bundle)
    }

    fun logButtonClick(buttonId: String) {
        log("button_click") {
            putString("button_id", buttonId)
        }
    }

    fun logTutorialComplete() {
        log("tutorial_complete")
    }

    fun logReviewRequested() {
        log("review_requested")
    }

    fun logPlayGamesSignIn(source: String) {
        log("play_games_signin") {
            putString("source", source)
        }
    }

    fun logPlayGamesScoreSubmit(leaderboardKey: String) {
        log("play_games_score") {
            putString("leaderboard", leaderboardKey)
        }
    }

    fun logDailyReminderEnabled(enabled: Boolean) {
        log("daily_reminder") {
            putString("enabled", enabled.toString())
        }
    }

    fun logAdBannerLoaded(adUnitId: String) {
        logAdEvent("ad_banner_load", adUnitId, "banner")
    }

    fun logAdBannerFailed(adUnitId: String, errorCode: Int) {
        logAdEvent("ad_banner_fail", adUnitId, "banner") {
            putInt("error_code", errorCode)
        }
    }

    fun logAdBannerClicked(adUnitId: String) {
        logAdEvent("ad_banner_click", adUnitId, "banner")
    }

    fun logAdBannerImpression(adUnitId: String) {
        logAdEvent("ad_banner_impr", adUnitId, "banner")
    }

    fun logAdInterstitialLoaded(adUnitId: String) {
        logAdEvent("ad_inter_load", adUnitId, "interstitial")
    }

    fun logAdInterstitialFailed(adUnitId: String, errorCode: Int, stage: String) {
        logAdEvent("ad_inter_fail", adUnitId, "interstitial") {
            putInt("error_code", errorCode)
            putString("stage", stage)
        }
    }

    fun logAdInterstitialShown(adUnitId: String) {
        logAdEvent("ad_inter_show", adUnitId, "interstitial")
    }

    fun logAdInterstitialDismissed(adUnitId: String) {
        logAdEvent("ad_inter_close", adUnitId, "interstitial")
    }

    fun logAdInterstitialSkipped(reason: String) {
        log("ad_inter_skip") {
            putString("ad_format", "interstitial")
            putString("reason", reason)
        }
    }

    fun logAdInterstitialClicked(adUnitId: String) {
        logAdEvent("ad_inter_click", adUnitId, "interstitial")
    }

    private fun logAdEvent(
        eventSuffix: String,
        adUnitId: String,
        adFormat: String,
        extra: (Bundle.() -> Unit)? = null,
    ) {
        log(eventSuffix) {
            putString("ad_format", adFormat)
            putString("ad_unit_id", adUnitId)
            extra?.invoke(this)
        }
    }

    private fun log(eventSuffix: String, params: (Bundle.() -> Unit)? = null) {
        val analytics = firebase ?: return
        val bundle = Bundle().apply {
            putString("app_slug", appSlug)
            params?.invoke(this)
        }
        analytics.logEvent(eventName(eventSuffix), bundle)
    }

    private fun eventName(suffix: String): String {
        val fullName = "${appSlug}_$suffix"
        return if (fullName.length <= MAX_EVENT_NAME_LENGTH) {
            fullName
        } else {
            fullName.take(MAX_EVENT_NAME_LENGTH)
        }
    }
}
