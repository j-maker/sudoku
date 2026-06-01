package com.template.android.data

import android.content.Context
import com.template.android.util.LocaleHelper

data class AppSettings(
    val darkMode: Boolean = true,
    val languageCode: String = "es",
    val dailyReminderEnabled: Boolean = false,
    val dailyReminderHour: Int = 18,
)

class AppDataStore(context: Context) {
    private val appContext = context.applicationContext
    private val prefs = appContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun ensureInitialLanguage(context: Context): String {
        if (prefs.getBoolean(K_LANGUAGE_AUTO_CONFIGURED, false)) {
            return resolvedLanguageCode()
        }
        if (prefs.all.isNotEmpty()) {
            prefs.edit().putBoolean(K_LANGUAGE_AUTO_CONFIGURED, true).commit()
            return resolvedLanguageCode()
        }
        val detected = LocaleHelper.languageCodeForSystem(context)
        prefs.edit()
            .putString(K_LANGUAGE, detected)
            .putBoolean(K_LANGUAGE_AUTO_CONFIGURED, true)
            .commit()
        return detected
    }

    fun loadSettings(): AppSettings = AppSettings(
        darkMode = prefs.getBoolean(K_DARK_MODE, true),
        languageCode = resolvedLanguageCode(),
        dailyReminderEnabled = prefs.getBoolean(K_DAILY_REMINDER, false),
        dailyReminderHour = prefs.getInt(K_DAILY_REMINDER_HOUR, 18).coerceIn(6, 22),
    )

    fun saveSettings(settings: AppSettings, sync: Boolean = false) {
        prefs.edit()
            .putBoolean(K_DARK_MODE, settings.darkMode)
            .putString(K_LANGUAGE, settings.languageCode)
            .putBoolean(K_DAILY_REMINDER, settings.dailyReminderEnabled)
            .putInt(K_DAILY_REMINDER_HOUR, settings.dailyReminderHour.coerceIn(6, 22))
            .apply()
        if (sync) {
            com.template.android.engagement.DailyReminderScheduler.sync(appContext)
        }
    }

    fun isTutorialCompleted(): Boolean = prefs.getBoolean(K_TUTORIAL_DONE, false)

    fun setTutorialCompleted() {
        prefs.edit().putBoolean(K_TUTORIAL_DONE, true).apply()
    }

    fun isReminderPromptShown(): Boolean = prefs.getBoolean(K_REMINDER_PROMPT, false)

    fun setReminderPromptShown() {
        prefs.edit().putBoolean(K_REMINDER_PROMPT, true).apply()
    }

    private fun resolvedLanguageCode(): String =
        prefs.getString(K_LANGUAGE, "es") ?: "es"

    companion object {
        private const val PREFS = "app_template_data"
        private const val K_LANGUAGE = "language"
        private const val K_LANGUAGE_AUTO_CONFIGURED = "language_auto"
        private const val K_DARK_MODE = "dark_mode"
        private const val K_DAILY_REMINDER = "daily_reminder"
        private const val K_DAILY_REMINDER_HOUR = "daily_reminder_hour"
        private const val K_TUTORIAL_DONE = "tutorial_done"
        private const val K_REMINDER_PROMPT = "reminder_prompt_shown"
    }
}
