package com.template.android.ui

import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.template.android.ads.InterstitialAdManager
import com.template.android.analytics.AppAnalytics
import com.template.android.data.AppDataStore
import com.template.android.data.AppSettings
import com.template.android.engagement.DailyReminderScheduler
import com.template.android.ui.onboarding.OnboardingTutorialOverlay
import com.template.android.ui.onboarding.ReminderOptInDialog
import com.template.android.ui.settings.SettingsSheet
import com.template.android.util.rememberNotificationPermissionRequest

@Composable
fun MainScreen(
    dataStore: AppDataStore,
    settings: AppSettings,
    onSettingsChange: (AppSettings) -> Unit,
    interstitialAdManager: InterstitialAdManager? = null,
) {
    val activity = LocalContext.current as? ComponentActivity
    var showTutorial by remember { mutableStateOf(!dataStore.isTutorialCompleted()) }
    var showReminderPrompt by rememberSaveable { mutableStateOf(false) }
    var showSettings by rememberSaveable { mutableStateOf(false) }

    val requestNotificationPermission = rememberNotificationPermissionRequest { granted ->
        if (granted) {
            val updated = settings.copy(dailyReminderEnabled = true)
            onSettingsChange(updated)
            DailyReminderScheduler.sync(activity ?: return@rememberNotificationPermissionRequest)
            AppAnalytics.logDailyReminderEnabled(true)
        }
        dataStore.setReminderPromptShown()
        showReminderPrompt = false
    }

    fun finishTutorial() {
        dataStore.setTutorialCompleted()
        showTutorial = false
        if (!dataStore.isReminderPromptShown() && !settings.dailyReminderEnabled) {
            showReminderPrompt = true
        }
    }

    LaunchedEffect(Unit) {
        AppAnalytics.logSplashFinished()
        if (showTutorial) {
            AppAnalytics.logScreenView("tutorial")
        }
    }

    when {
        showTutorial -> {
            OnboardingTutorialOverlay(onFinished = ::finishTutorial)
        }

        showReminderPrompt -> {
            ReminderOptInDialog(
                onEnable = { requestNotificationPermission() },
                onDismiss = {
                    dataStore.setReminderPromptShown()
                    showReminderPrompt = false
                },
            )
            LaunchedEffect(Unit) { AppAnalytics.logScreenView("reminder_prompt") }
        }

        else -> {
            LaunchedEffect(Unit) { AppAnalytics.logScreenView("home") }
            TemplateHomeScreen(
                interstitialAdManager = interstitialAdManager,
                onOpenSettings = { showSettings = true },
            )
        }
    }

    if (showSettings && !showTutorial) {
        SettingsSheet(
            settings = settings,
            onSettingsChange = onSettingsChange,
            onDismiss = { showSettings = false },
        )
    }
}
