package com.template.android.ads

import android.app.Activity
import android.content.Context
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.OnUserEarnedRewardListener
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.template.android.R
import com.template.android.analytics.AppAnalytics

class RewardedAdManager(context: Context) {
    private val appContext = context.applicationContext
    private var rewardedAd: RewardedAd? = null
    private var isLoading = false
    private var isShowing = false

    private val adUnitId: String
        get() = AdIds.rewardedId(appContext.getString(R.string.admob_rewarded_unit_id))

    fun preload() {
        if (isLoading || rewardedAd != null) return
        isLoading = true
        RewardedAd.load(
            appContext,
            adUnitId,
            AdRequest.Builder().build(),
            object : RewardedAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedAd) {
                    rewardedAd = ad
                    isLoading = false
                    AppAnalytics.logAdRewardedLoaded(adUnitId)
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    rewardedAd = null
                    isLoading = false
                    AppAnalytics.logAdRewardedFailed(adUnitId, error.code, "load")
                }
            },
        )
    }

    fun showIfReady(
        activity: Activity,
        onAdStarted: (() -> Unit)? = null,
        onRewardEarned: (() -> Unit)? = null,
        onFinished: () -> Unit = {},
    ): Boolean {
        if (isShowing) {
            AppAnalytics.logAdRewardedSkipped("already_showing")
            onFinished()
            return false
        }

        val ad = rewardedAd
        if (ad == null) {
            AppAnalytics.logAdRewardedSkipped("not_ready")
            onFinished()
            preload()
            return false
        }

        onAdStarted?.invoke()

        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdShowedFullScreenContent() {
                isShowing = true
                AppAnalytics.logAdRewardedShown(adUnitId)
            }

            override fun onAdDismissedFullScreenContent() {
                rewardedAd = null
                isShowing = false
                AppAnalytics.logAdRewardedDismissed(adUnitId)
                preload()
                onFinished()
            }

            override fun onAdFailedToShowFullScreenContent(error: AdError) {
                rewardedAd = null
                isShowing = false
                AppAnalytics.logAdRewardedFailed(adUnitId, error.code, "show")
                preload()
                onFinished()
            }

            override fun onAdClicked() {
                AppAnalytics.logAdRewardedClicked(adUnitId)
            }
        }

        ad.show(
            activity,
            OnUserEarnedRewardListener {
                AppAnalytics.logAdRewardedEarned(adUnitId)
                onRewardEarned?.invoke()
            },
        )
        return true
    }
}
