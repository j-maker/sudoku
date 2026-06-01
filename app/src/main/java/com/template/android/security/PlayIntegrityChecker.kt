package com.template.android.security

import android.content.Context
import android.util.Base64
import android.util.Log
import com.google.android.play.core.integrity.IntegrityManagerFactory
import com.google.android.play.core.integrity.IntegrityTokenRequest
import com.template.android.BuildConfig
import java.security.SecureRandom

object PlayIntegrityChecker {

    private const val TAG = "PlayIntegrity"

    fun checkOnAppStart(context: Context) {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "Omitido en build debug")
            return
        }
        val nonce = generateNonce()
        val manager = IntegrityManagerFactory.create(context)
        val request = IntegrityTokenRequest.builder().setNonce(nonce).build()
        manager.requestIntegrityToken(request)
            .addOnSuccessListener { response ->
                Log.i(TAG, "Token obtenido (verificar en servidor si aplica)")
                @Suppress("UNUSED_VARIABLE")
                val tokenForServer = response.token()
            }
            .addOnFailureListener { error ->
                Log.w(TAG, "Play Integrity: ${error.message}")
            }
    }

    private fun generateNonce(): String {
        val bytes = ByteArray(32)
        SecureRandom().nextBytes(bytes)
        return Base64.encodeToString(bytes, Base64.URL_SAFE or Base64.NO_WRAP)
    }
}
