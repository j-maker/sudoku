package com.template.android.ui

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
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
import com.template.android.ui.sudoku.SudokuScreen
import com.template.android.util.rememberNotificationPermissionRequest

@Composable
fun MainScreen(
    dataStore: AppDataStore,
    settings: AppSettings,
    openFromDailyReminder: Boolean = false,
    onConsumedDailyReminderOpen: () -> Unit = {},
    onSettingsChange: (AppSettings) -> Unit,
    interstitialAdManager: InterstitialAdManager? = null,
) {
    val activity = LocalContext.current as? ComponentActivity
    var showTutorial by remember { mutableStateOf(!dataStore.isTutorialCompleted()) }
    var showReminderPrompt by rememberSaveable { mutableStateOf(false) }
    var showSettings by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(openFromDailyReminder) {
        if (!openFromDailyReminder) return@LaunchedEffect
        if (dataStore.isTutorialCompleted()) {
            showTutorial = false
        }
        showReminderPrompt = false
        showSettings = false
        AppAnalytics.logScreenView("game_from_notification")
        onConsumedDailyReminderOpen()
    }

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
        if (showTutorial) {
            AppAnalytics.logScreenView("tutorial")
        }
    }

    val showGame = !showTutorial && !showReminderPrompt

    Box(modifier = Modifier.fillMaxSize()) {
        if (showGame) {
            LaunchedEffect(Unit) { AppAnalytics.logScreenView("game") }
            SudokuScreen(
                dataStore = dataStore,
                settings = settings,
                onSettingsChange = onSettingsChange,
                onOpenSettings = { showSettings = true },
                interstitialAdManager = interstitialAdManager,
                openFromDailyReminder = openFromDailyReminder,
            )
        }

        if (showTutorial) {
            OnboardingTutorialOverlay(onFinished = ::finishTutorial)
        }

        if (showReminderPrompt) {
            ReminderOptInDialog(
                onEnable = { requestNotificationPermission() },
                onDismiss = {
                    dataStore.setReminderPromptShown()
                    showReminderPrompt = false
                },
            )
            LaunchedEffect(Unit) { AppAnalytics.logScreenView("reminder_prompt") }
        }
    }

    if (showSettings && showGame) {
        SettingsSheet(
            dataStore = dataStore,
            settings = settings,
            onSettingsChange = onSettingsChange,
            onDismiss = { showSettings = false },
        )
    }
}
