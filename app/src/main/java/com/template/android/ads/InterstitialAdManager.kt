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
    private var lastShownAtMs = 0L
    private var clicksSinceLastAd = 0

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

    /** Cuenta toques de juego (celda, teclado, borrar, notas). Muestra intersticial cada [CLICKS_PER_INTERSTITIAL]. */
    fun registerGameplayClick(activity: Activity) {
        clicksSinceLastAd++
        if (clicksSinceLastAd < CLICKS_PER_INTERSTITIAL) return
        MobileAdsInitializer.whenReady {
            showIfReady(
                activity,
                onAdShown = { clicksSinceLastAd = 0 },
            )
        }
    }

    fun showIfReady(
        activity: Activity,
        shouldShow: Boolean = true,
        onAdShown: (() -> Unit)? = null,
        onFinished: () -> Unit = {},
    ): Boolean {
        if (!shouldShow) {
            AppAnalytics.logAdInterstitialSkipped("engagement_policy")
            onFinished()
            preload()
            return false
        }

        val now = System.currentTimeMillis()
        if (now - lastShownAtMs < MIN_INTERVAL_MS) {
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
                AppAnalytics.logAdInterstitialShown(adUnitId)
            }

            override fun onAdDismissedFullScreenContent() {
                interstitialAd = null
                lastShownAtMs = System.currentTimeMillis()
                AppAnalytics.logAdInterstitialDismissed(adUnitId)
                onAdShown?.invoke()
                preload()
                onFinished()
            }

            override fun onAdFailedToShowFullScreenContent(error: AdError) {
                interstitialAd = null
                AppAnalytics.logAdInterstitialFailed(adUnitId, error.code, "show")
                preload()
                onFinished()
            }

            override fun onAdClicked() {
                AppAnalytics.logAdInterstitialClicked(adUnitId)
            }
        }
        ad.show(activity)
        return true
    }

    companion object {
        private const val MIN_INTERVAL_MS = 90_000L
        const val CLICKS_PER_INTERSTITIAL = 50
    }
}
