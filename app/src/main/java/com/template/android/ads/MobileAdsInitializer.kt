package com.template.android.ads

/**
 * Coordina la carga de anuncios tras UMP + [com.google.android.gms.ads.MobileAds.initialize].
 */
object MobileAdsInitializer {

    @Volatile
    var isReady: Boolean = false
        private set

    private val pendingCallbacks = mutableListOf<() -> Unit>()

    @Synchronized
    fun whenReady(action: () -> Unit) {
        if (isReady) {
            action()
        } else {
            pendingCallbacks.add(action)
        }
    }

    @Synchronized
    fun markReady() {
        if (isReady) return
        isReady = true
        val callbacks = pendingCallbacks.toList()
        pendingCallbacks.clear()
        callbacks.forEach { it() }
    }
}
