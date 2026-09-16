package com.template.android.ads

import android.content.Context
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration
import com.template.android.R

/**
 * Configuración segura de AdMob: emulador siempre en modo test.
 * Añade hashes de dispositivo en `admob_test_device_ids` para QA en release.
 */
internal object AdSafety {
    fun applyRequestConfiguration(context: Context) {
        val testDeviceIds = buildList {
            add(AdRequest.DEVICE_ID_EMULATOR)
            context.resources.getStringArray(R.array.admob_test_device_ids)
                .map { it.trim() }
                .filter { it.isNotEmpty() }
                .forEach(::add)
        }
        MobileAds.setRequestConfiguration(
            RequestConfiguration.Builder()
                .setTestDeviceIds(testDeviceIds)
                .build(),
        )
    }

    fun runWhenReady(queueFlag: QueueFlag, action: () -> Unit) {
        if (MobileAdsInitializer.isReady) {
            action()
            return
        }
        if (queueFlag.queued) return
        queueFlag.queued = true
        MobileAdsInitializer.whenReady {
            queueFlag.queued = false
            action()
        }
    }

    class QueueFlag {
        @Volatile
        var queued: Boolean = false
    }
}
