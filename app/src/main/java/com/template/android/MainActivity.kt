package com.template.android

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.template.android.ads.AdConsentManager
import com.template.android.ads.InterstitialAdManager
import com.template.android.data.AppDataStore
import com.template.android.ui.MainScreen
import com.template.android.ui.theme.AppTheme
import com.template.android.util.LocaleHelper

class MainActivity : ComponentActivity() {

    private lateinit var interstitialAdManager: InterstitialAdManager

    override fun attachBaseContext(newBase: Context) {
        val dataStore = AppDataStore(newBase)
        val languageCode = dataStore.ensureInitialLanguage(newBase)
        super.attachBaseContext(LocaleHelper.wrap(newBase, languageCode))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        AdConsentManager.gatherConsentAndInitializeAds(this)
        interstitialAdManager = InterstitialAdManager(this)
        interstitialAdManager.preload()
        enableEdgeToEdge()
        setContent {
            val dataStore = remember { AppDataStore(this) }
            var settings by remember { mutableStateOf(dataStore.loadSettings()) }

            AppTheme(darkMode = true) {
                MainScreen(
                    dataStore = dataStore,
                    settings = settings,
                    onSettingsChange = { updated ->
                        val languageChanged = updated.languageCode != settings.languageCode
                        settings = updated
                        dataStore.saveSettings(settings, sync = true)
                        if (languageChanged) {
                            recreate()
                        }
                    },
                    interstitialAdManager = interstitialAdManager,
                )
            }
        }
    }
}
