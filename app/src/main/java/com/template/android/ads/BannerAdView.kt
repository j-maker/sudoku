package com.template.android.ads

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError
import com.template.android.R
import com.template.android.analytics.AppAnalytics

@Composable
fun BannerAdView(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val adUnitId = AdIds.bannerId(context.getString(R.string.admob_banner_unit_id))
    var adsReady by remember { mutableStateOf(MobileAdsInitializer.isReady) }

    LaunchedEffect(Unit) {
        MobileAdsInitializer.whenReady { adsReady = true }
    }

    if (!adsReady) {
        androidx.compose.foundation.layout.Spacer(
            modifier = modifier
                .fillMaxWidth()
                .height(50.dp),
        )
        return
    }

    AndroidView(
        modifier = modifier
            .fillMaxWidth()
            .height(50.dp),
        factory = { ctx ->
            AdView(ctx).apply {
                setAdSize(AdSize.BANNER)
                this.adUnitId = adUnitId
                adListener = object : AdListener() {
                    override fun onAdLoaded() {
                        AppAnalytics.logAdBannerLoaded(adUnitId)
                    }

                    override fun onAdFailedToLoad(error: LoadAdError) {
                        AppAnalytics.logAdBannerFailed(adUnitId, error.code)
                    }

                    override fun onAdClicked() {
                        AppAnalytics.logAdBannerClicked(adUnitId)
                    }

                    override fun onAdImpression() {
                        AppAnalytics.logAdBannerImpression(adUnitId)
                    }
                }
                loadAd(AdRequest.Builder().build())
            }
        },
        update = { view ->
            if (view.adUnitId != adUnitId) {
                view.adUnitId = adUnitId
                view.loadAd(AdRequest.Builder().build())
            }
        },
    )
}
