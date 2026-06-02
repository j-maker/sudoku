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
    private var isShowing = false

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
        // Inicializa el cooldown base para que el primer anuncio sea 90s después de entrar al juego.
        if (lastShownAtMs == 0L) {
            lastShownAtMs = nowMs
        }
    }

    /** Intenta mostrar intersticial usando cooldown (90s) y disponibilidad del ad. */
    fun maybeShowTimed(
        activity: Activity,
        onAdStarted: (() -> Unit)? = null,
        onFinished: (() -> Unit)? = null,
    ) {
        // Primer anuncio solo 90s después de ENTRAR al juego.
        if (sessionStartedAtMs == 0L) markGameSessionStart()
        val now = System.currentTimeMillis()
        if (now - sessionStartedAtMs < MIN_INTERVAL_MS) return
        MobileAdsInitializer.whenReady {
            showIfReady(
                activity = activity,
                ignoreCooldown = false,
                onAdStarted = onAdStarted,
                onFinished = { onFinished?.invoke() },
            )
        }
    }

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

        val now = System.currentTimeMillis()
        if (!ignoreCooldown && now - lastShownAtMs < MIN_INTERVAL_MS) {
            AppAnalytics.logAdInterstitialSkipped("cooldown")
            onFinished()
            preload()
            return false
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
