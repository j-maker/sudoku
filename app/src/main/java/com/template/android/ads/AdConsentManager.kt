package com.template.android.ads

import android.app.Activity
import android.content.Context
import com.google.android.gms.ads.MobileAds
import com.google.android.ump.ConsentDebugSettings
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
        val params = buildConsentRequestParameters(activity)

        consentInformation.requestConsentInfoUpdate(
            activity,
            params,
            {
                UserMessagingPlatform.loadAndShowConsentFormIfRequired(activity) { _ ->
                    if (consentInformation.canRequestAds()) {
                        initializeMobileAds(activity)
                    } else if (BuildConfig.DEBUG) {
                        android.util.Log.d("AdConsent", "Anuncios no permitidos (consentimiento denegado)")
                    }
                }
            },
            { error ->
                if (BuildConfig.DEBUG) {
                    android.util.Log.w("AdConsent", "requestConsentInfoUpdate: ${error.message}")
                }
                if (consentInformation.canRequestAds()) {
                    initializeMobileAds(activity)
                }
            },
        )
    }

    fun showPrivacyOptionsForm(activity: Activity, onDismiss: () -> Unit = {}) {
        UserMessagingPlatform.showPrivacyOptionsForm(activity) { _ -> onDismiss() }
    }

    fun isPrivacyOptionsRequired(context: Context): Boolean {
        return UserMessagingPlatform.getConsentInformation(context).privacyOptionsRequirementStatus ==
            ConsentInformation.PrivacyOptionsRequirementStatus.REQUIRED
    }

    private fun buildConsentRequestParameters(context: Context): ConsentRequestParameters {
        val builder = ConsentRequestParameters.Builder()
            .setTagForUnderAgeOfConsent(false)
        if (BuildConfig.DEBUG) {
            val debugSettings = ConsentDebugSettings.Builder(context)
                .setDebugGeography(ConsentDebugSettings.DebugGeography.DEBUG_GEOGRAPHY_EEA)
                .build()
            builder.setConsentDebugSettings(debugSettings)
        }
        return builder.build()
    }

    private fun initializeMobileAds(activity: Activity) {
        if (MobileAdsInitializer.isReady) return
        AdSafety.applyRequestConfiguration(activity.applicationContext)
        MobileAds.initialize(activity.applicationContext) {
            MobileAdsInitializer.markReady()
            if (BuildConfig.DEBUG) {
                android.util.Log.d("AdConsent", "Mobile Ads SDK listo")
            }
        }
    }
}
