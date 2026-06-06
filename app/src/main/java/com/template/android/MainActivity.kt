package com.template.android

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.template.android.ads.AdConsentManager
import com.template.android.ads.InterstitialAdManager
import com.template.android.analytics.AppAnalytics
import com.template.android.data.AppDataStore
import com.template.android.engagement.DailyReminderScheduler
import com.template.android.engagement.PlayGamesHelper
import com.template.android.ui.BrandedSplashScreen
import com.template.android.ui.MainScreen
import com.template.android.ui.theme.AppTheme
import com.template.android.util.LocaleHelper
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {

    private lateinit var interstitialAdManager: InterstitialAdManager
    private val openFromDailyReminder = mutableStateOf(false)

    override fun attachBaseContext(newBase: Context) {
        val dataStore = AppDataStore(newBase)
        val languageCode = dataStore.ensureInitialLanguage(newBase)
        super.attachBaseContext(LocaleHelper.wrap(newBase, languageCode))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        readReminderLaunchIntent(intent)
        clearReminderNotification()
        AdConsentManager.gatherConsentAndInitializeAds(this)
        PlayGamesHelper.signInSilently(this)
        interstitialAdManager = InterstitialAdManager(this)
        interstitialAdManager.preload()
        enableEdgeToEdge()
        setContent {
            val dataStore = remember { AppDataStore(this) }
            var settings by remember { mutableStateOf(dataStore.loadSettings()) }
            val fromDailyReminder = openFromDailyReminder.value
            var showBrandedSplash by remember { mutableStateOf(!fromDailyReminder) }

            LaunchedEffect(fromDailyReminder) {
                if (fromDailyReminder) showBrandedSplash = false
            }

            LaunchedEffect(showBrandedSplash) {
                if (!showBrandedSplash) return@LaunchedEffect
                delay(SPLASH_MIN_DISPLAY_MS)
                showBrandedSplash = false
                AppAnalytics.logSplashFinished()
            }

            AppTheme {
                Box(modifier = Modifier.fillMaxSize()) {
                    if (!showBrandedSplash) {
                        MainScreen(
                            dataStore = dataStore,
                            settings = settings,
                            openFromDailyReminder = fromDailyReminder,
                            onConsumedDailyReminderOpen = { openFromDailyReminder.value = false },
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
                    AnimatedVisibility(
                        visible = showBrandedSplash,
                        exit = fadeOut(animationSpec = tween(SPLASH_FADE_OUT_MS)),
                    ) {
                        BrandedSplashScreen(modifier = Modifier.fillMaxSize())
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        readReminderLaunchIntent(intent)
        clearReminderNotification()
    }

    private fun readReminderLaunchIntent(intent: Intent?) {
        if (intent?.getBooleanExtra(DailyReminderScheduler.EXTRA_FROM_DAILY_REMINDER, false) != true) {
            return
        }
        openFromDailyReminder.value = true
        intent.removeExtra(DailyReminderScheduler.EXTRA_FROM_DAILY_REMINDER)
    }

    override fun onResume() {
        super.onResume()
        clearReminderNotification()
    }

    private fun clearReminderNotification() {
        DailyReminderScheduler.clearReminderNotification(this)
    }

    private companion object {
        const val SPLASH_MIN_DISPLAY_MS = 2000L
        const val SPLASH_FADE_OUT_MS = 300
    }
}
