package com.template.android.ads

import com.template.android.BuildConfig

object AdIds {
  /** IDs de prueba de Google — sustituir en strings.xml antes de publicar. */
  private const val TEST_APP_ID = "ca-app-pub-3940256099942544~3347511713"
  private const val TEST_BANNER = "ca-app-pub-3940256099942544/6300978111"
  private const val TEST_INTERSTITIAL = "ca-app-pub-3940256099942544/1033173712"

  fun appId(productionId: String): String =
      if (BuildConfig.DEBUG) TEST_APP_ID else productionId

  fun bannerId(productionId: String): String =
      if (BuildConfig.DEBUG) TEST_BANNER else productionId

  fun interstitialId(productionId: String): String =
      if (BuildConfig.DEBUG) TEST_INTERSTITIAL else productionId
}
