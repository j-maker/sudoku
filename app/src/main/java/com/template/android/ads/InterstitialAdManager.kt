package com.template.android.ads

import android.app.Activity
import android.content.Context
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.template.android.R
import com.template.android.analytics.AppAnalytics

class InterstitialAdManager(context: Context) {
    private val appContext = context.applicationContext
    private var interstitialAd: InterstitialAd? = null
    private var isLoading = false
    private var sessionStartedAtMs = 0L
    private var lastShownAtMs = 0L
    private var lastRewardedAtMs = 0L
    private var isShowing = false
    private var clockPauseCount = 0
    private var clockPausedAtMs = 0L

    private val adUnitId: String
        get() = AdIds.interstitialId(appContext.getString(R.string.admob_interstitial_unit_id))

    fun preload() {
        if (isLoading || interstitialAd != null) return
        isLoading = true
        InterstitialAd.load(
            appContext,
            adUnitId,
            AdRequest.Builder().build(),
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    interstitialAd = ad
                    isLoading = false
                    AppAnalytics.logAdInterstitialLoaded(adUnitId)
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    interstitialAd = null
                    isLoading = false
                    AppAnalytics.logAdInterstitialFailed(adUnitId, error.code, "load")
                }
            },
        )
    }

    /** Llama al entrar a la pantalla principal del juego (NO al iniciar una nueva partida). */
    fun markGameSessionStart(nowMs: Long = System.currentTimeMillis()) {
        sessionStartedAtMs = nowMs
        lastShownAtMs = nowMs
    }

    /** Pausa el reloj de cooldown (app en segundo plano o vídeo a pantalla completa). */
    fun pauseClock(nowMs: Long = System.currentTimeMillis()) {
        if (clockPauseCount++ == 0) {
            clockPausedAtMs = nowMs
        }
    }

    /** Reanuda el reloj de cooldown tras [pauseClock]. */
    fun resumeClock(nowMs: Long = System.currentTimeMillis()) {
        if (clockPauseCount <= 0) return
        clockPauseCount--
        if (clockPauseCount > 0) return
        val pausedMs = nowMs - clockPausedAtMs
        clockPausedAtMs = 0L
        if (sessionStartedAtMs > 0L) sessionStartedAtMs += pausedMs
        if (lastShownAtMs > 0L) lastShownAtMs += pausedMs
        if (lastRewardedAtMs > 0L) lastRewardedAtMs += pausedMs
    }

    /** Evita intersticial justo después de un vídeo recompensado. */
    fun markRewardedShown(nowMs: Long = System.currentTimeMillis()) {
        lastRewardedAtMs = nowMs
    }

    private fun effectiveNowMs(): Long {
        if (clockPauseCount > 0 && clockPausedAtMs > 0L) {
            return clockPausedAtMs
        }
        return System.currentTimeMillis()
    }

    /** Tras completar un sudoku (natural break). Ignora cooldown de sesión; no encadena tras rewarded. */
    fun showAfterVictory(
        activity: Activity,
        onAdStarted: (() -> Unit)? = null,
        onAdShown: (() -> Unit)? = null,
        onFinished: () -> Unit = {},
    ): Boolean = showIfReady(
        activity = activity,
        ignoreCooldown = true,
        onAdStarted = onAdStarted,
        onAdShown = onAdShown,
        onFinished = onFinished,
    )

    fun showIfReady(
        activity: Activity,
        shouldShow: Boolean = true,
        ignoreCooldown: Boolean = false,
        onAdStarted: (() -> Unit)? = null,
        onAdShown: (() -> Unit)? = null,
        onFinished: () -> Unit = {},
    ): Boolean {
        if (isShowing) {
            AppAnalytics.logAdInterstitialSkipped("already_showing")
            return false
        }
        if (!shouldShow) {
            AppAnalytics.logAdInterstitialSkipped("engagement_policy")
            onFinished()
            preload()
            return false
        }

        val now = effectiveNowMs()
        if (lastRewardedAtMs > 0L && now - lastRewardedAtMs < MIN_INTERVAL_MS) {
            AppAnalytics.logAdInterstitialSkipped("after_rewarded")
            onFinished()
            preload()
            return false
        }
        if (!ignoreCooldown) {
            val sessionStart = sessionStartedAtMs
            if (sessionStart > 0L && now < sessionStart + MIN_INTERVAL_MS) {
                AppAnalytics.logAdInterstitialSkipped("session_warmup")
                onFinished()
                preload()
                return false
            }
            val cooldownAnchor = when {
                lastShownAtMs > 0L -> lastShownAtMs
                sessionStart > 0L -> sessionStart
                else -> now
            }
            if (now - cooldownAnchor < MIN_INTERVAL_MS) {
                AppAnalytics.logAdInterstitialSkipped("cooldown")
                onFinished()
                preload()
                return false
            }
        }

        val ad = interstitialAd
        if (ad == null) {
            AppAnalytics.logAdInterstitialSkipped("not_ready")
            onFinished()
            preload()
            return false
        }

        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdShowedFullScreenContent() {
                isShowing = true
                AppAnalytics.logAdInterstitialShown(adUnitId)
                onAdStarted?.invoke()
            }

            override fun onAdDismissedFullScreenContent() {
                interstitialAd = null
                lastShownAtMs = System.currentTimeMillis()
                isShowing = false
                AppAnalytics.logAdInterstitialDismissed(adUnitId)
                onAdShown?.invoke()
                preload()
                onFinished()
            }

            override fun onAdFailedToShowFullScreenContent(error: AdError) {
                interstitialAd = null
                isShowing = false
                AppAnalytics.logAdInterstitialFailed(adUnitId, error.code, "show")
                preload()
                onFinished()
            }

            override fun onAdClicked() {
                AppAnalytics.logAdInterstitialClicked(adUnitId)
            }
        }
        // Marca el intento de mostrar para que el loop por tiempo no lo dispare varias veces.
        lastShownAtMs = System.currentTimeMillis()
        ad.show(activity)
        return true
    }

    companion object {
        private const val MIN_INTERVAL_MS = 90_000L
    }
}
