package com.template.android.ads

import android.app.Activity
import com.google.android.gms.ads.MobileAds
import com.google.android.ump.ConsentInformation
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.UserMessagingPlatform
import com.template.android.BuildConfig

/**
 * Flujo GDPR/EEE de Google UMP antes de mostrar anuncios AdMob.
 * Configura el mensaje de consentimiento en AdMob → Privacidad y mensajería.
 */
object AdConsentManager {

    fun gatherConsentAndInitializeAds(activity: Activity) {
        if (MobileAdsInitializer.isReady) return

        val consentInformation = UserMessagingPlatform.getConsentInformation(activity)
        val params = ConsentRequestParameters.Builder()
            .setTagForUnderAgeOfConsent(false)
            .build()

        consentInformation.requestConsentInfoUpdate(
            activity,
            params,
            {
                if (consentInformation.isConsentFormAvailable) {
                    UserMessagingPlatform.loadAndShowConsentFormIfRequired(activity) { _ ->
                        initializeMobileAds(activity)
                    }
                } else {
                    initializeMobileAds(activity)
                }
            },
            { _ ->
                initializeMobileAds(activity)
            },
        )
    }

    private fun initializeMobileAds(activity: Activity) {
        if (MobileAdsInitializer.isReady) return
        MobileAds.initialize(activity.applicationContext) {
            MobileAdsInitializer.markReady()
            if (BuildConfig.DEBUG) {
                android.util.Log.d("AdConsent", "Mobile Ads SDK listo")
            }
        }
    }

    /** Para depuración: estado del consentimiento (no bloquea la app). */
    fun consentStatusLabel(activity: Activity): String {
        return when (UserMessagingPlatform.getConsentInformation(activity).consentStatus) {
            ConsentInformation.ConsentStatus.OBTAINED -> "obtained"
            ConsentInformation.ConsentStatus.REQUIRED -> "required"
            ConsentInformation.ConsentStatus.NOT_REQUIRED -> "not_required"
            else -> "unknown"
        }
    }
}
