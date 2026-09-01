package com.template.android.analytics

import android.content.Context
import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import com.template.android.BuildConfig
import com.template.android.data.AchievementId
import com.template.android.ui.sudoku.SocialPlatform
import com.template.android.ui.sudoku.SudokuDifficulty

/**
 * Analytics para un proyecto Firebase/GA4 compartido entre varias apps.
 * Valores en [app.template.properties] → appSlug, appDisplayName.
 * Límite Firebase: 40 caracteres por nombre de evento → sufijos largos usan abreviaturas.
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
            putString("screen", screen)
            putString(FirebaseAnalytics.Param.SCREEN_NAME, screenName)
            putString(FirebaseAnalytics.Param.SCREEN_CLASS, screenName)
        }
        analytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, bundle)
    }

    fun logGameStart(difficulty: SudokuDifficulty, source: String) {
        log("game_start") {
            putString("difficulty", difficulty.name.lowercase())
            putString("source", source)
        }
    }

    fun logGameWon(difficulty: SudokuDifficulty, elapsedSeconds: Int, mistakes: Int) {
        log("game_won") {
            putString("difficulty", difficulty.name.lowercase())
            putLong("elapsed_seconds", elapsedSeconds.toLong())
            putInt("mistakes", mistakes)
        }
    }

    fun logButtonClick(buttonId: String) {
        log("button_click") {
            putString("button_id", buttonId)
        }
    }

    fun logShare(platform: SocialPlatform) {
        log("share") {
            putString("platform", platform.name.lowercase())
        }
    }

    fun logTutorialComplete() {
        log("tutorial_complete")
    }

    fun logMenuOpen(menu: String) {
        log("menu_open") {
            putString("menu", menu)
        }
    }

    fun logMoreGamesOpen() {
        log("more_games_open")
    }

    fun logMoreGamesStore(packageName: String) {
        log("more_games_store") {
            putString("target_package", packageName)
        }
    }

    fun logAchievementUnlock(achievement: AchievementId) {
        log("achievement_unlock") {
            putString("achievement_id", achievement.name.lowercase())
        }
    }

    fun logStatsReset() {
        log("stats_reset")
    }

    fun logReviewRequested() {
        log("review_requested")
    }

    fun logPlayGamesSignIn(source: String) {
        log("pg_signin") {
            putString("source", source)
        }
    }

    fun logPlayGamesScoreSubmit(difficulty: String) {
        log("pg_score") {
            putString("difficulty", difficulty)
        }
    }

    fun logPlayGamesLeaderboardFailed(stage: String, errorCode: Int? = null) {
        log("pg_lb_fail") {
            putString("stage", stage)
            if (errorCode != null) putInt("error_code", errorCode)
        }
    }

    fun logPlayGamesLeaderboardOpened(source: String) {
        log("pg_lb_open") {
            putString("source", source)
        }
    }

    fun logDailyReminderEnabled(enabled: Boolean) {
        log("daily_reminder") {
            putString("enabled", enabled.toString())
        }
    }

    fun logPostGameShare(fromVictory: Boolean) {
        log("post_game_share") {
            putString("from_victory", fromVictory.toString())
        }
    }

    fun logHintUsed(source: String, success: Boolean) {
        log("hint_used") {
            putString("source", source)
            putString("success", success.toString())
        }
    }

    fun logHintVideoBlocked(reason: String) {
        log("hint_video_block") {
            putString("reason", reason)
        }
    }

    fun logSoundToggled(enabled: Boolean) {
        log("sound_toggle") {
            putString("enabled", enabled.toString())
        }
    }

    fun syncEngagementUserProperties(
        gamesWon: Int,
        gamesPlayed: Int,
        soundEnabled: Boolean,
        dailyReminderEnabled: Boolean,
        lastDifficulty: SudokuDifficulty,
    ) {
        val analytics = firebase ?: return
        analytics.setUserProperty("games_won_bucket", gamesWonBucket(gamesWon))
        analytics.setUserProperty("games_played_bucket", gamesPlayedBucket(gamesPlayed))
        analytics.setUserProperty("sound_enabled", soundEnabled.toString())
        analytics.setUserProperty("daily_reminder", dailyReminderEnabled.toString())
        analytics.setUserProperty("last_difficulty", lastDifficulty.name.lowercase())
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

    fun logAdRewardedLoaded(adUnitId: String) {
        logAdEvent("ad_reward_load", adUnitId, "rewarded")
    }

    fun logAdRewardedFailed(adUnitId: String, errorCode: Int, stage: String) {
        logAdEvent("ad_reward_fail", adUnitId, "rewarded") {
            putInt("error_code", errorCode)
            putString("stage", stage)
        }
    }

    fun logAdRewardedShown(adUnitId: String) {
        logAdEvent("ad_reward_show", adUnitId, "rewarded")
    }

    fun logAdRewardedEarned(adUnitId: String) {
        logAdEvent("ad_reward_earn", adUnitId, "rewarded")
    }

    fun logAdRewardedDismissed(adUnitId: String) {
        logAdEvent("ad_reward_close", adUnitId, "rewarded")
    }

    fun logAdRewardedSkipped(reason: String) {
        log("ad_reward_skip") {
            putString("ad_format", "rewarded")
            putString("reason", reason)
        }
    }

    fun logAdRewardedClicked(adUnitId: String) {
        logAdEvent("ad_reward_click", adUnitId, "rewarded")
    }

    private fun gamesWonBucket(gamesWon: Int): String = when {
        gamesWon == 0 -> "0"
        gamesWon < 5 -> "1-4"
        gamesWon < 20 -> "5-19"
        gamesWon < 50 -> "20-49"
        else -> "50+"
    }

    private fun gamesPlayedBucket(gamesPlayed: Int): String = when {
        gamesPlayed == 0 -> "0"
        gamesPlayed < 10 -> "1-9"
        gamesPlayed < 50 -> "10-49"
        else -> "50+"
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
