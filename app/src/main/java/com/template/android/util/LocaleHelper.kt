package com.template.android.util

import android.content.Context
import android.content.res.Configuration
import java.util.Locale

/**
 * 14 idiomas soportados (carpetas values-XX). Lista usada por AppDataStore y notificaciones.
 * Tabla de idiomas: README.md
 */
object LocaleHelper {

    const val LANG_ES = "es"
    const val LANG_EN = "en"
    const val LANG_PT = "pt"
    const val LANG_FR = "fr"
    const val LANG_DE = "de"
    const val LANG_IT = "it"
    const val LANG_RU = "ru"
    const val LANG_TR = "tr"
    const val LANG_AR = "ar"
    const val LANG_HI = "hi"
    const val LANG_ID = "id"
    const val LANG_JA = "ja"
    const val LANG_KO = "ko"
    const val LANG_ZH = "zh"

    val supportedLanguages = listOf(
        LANG_ES, LANG_EN, LANG_PT, LANG_FR, LANG_DE, LANG_IT,
        LANG_RU, LANG_TR, LANG_AR, LANG_HI, LANG_ID, LANG_JA, LANG_KO, LANG_ZH,
    )

    /** Fallback when the device locale is not supported. */
    const val DEFAULT_LANGUAGE = LANG_EN

    /** Maps the device locale to a supported [languageCode], or [DEFAULT_LANGUAGE]. */
    fun languageCodeForSystem(context: Context): String =
        languageCodeForLocale(systemLocale(context))

    fun languageCodeForLocale(locale: Locale): String =
        when (locale.language) {
            LANG_ES -> LANG_ES
            LANG_EN -> LANG_EN
            LANG_PT -> LANG_PT
            LANG_FR -> LANG_FR
            LANG_DE -> LANG_DE
            LANG_IT -> LANG_IT
            LANG_RU -> LANG_RU
            LANG_TR -> LANG_TR
            LANG_AR -> LANG_AR
            LANG_HI -> LANG_HI
            "in", LANG_ID -> LANG_ID
            LANG_JA -> LANG_JA
            LANG_KO -> LANG_KO
            LANG_ZH -> LANG_ZH
            else -> DEFAULT_LANGUAGE
        }

    private fun systemLocale(context: Context): Locale {
        val config = context.resources.configuration
        return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            config.locales[0]
        } else {
            @Suppress("DEPRECATION")
            config.locale
        }
    }

    fun wrap(context: Context, languageCode: String): Context {
        val locale = localeFor(languageCode)
        Locale.setDefault(locale)
        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)
        config.setLayoutDirection(locale)
        return context.createConfigurationContext(config)
    }

    fun localeFor(languageCode: String): Locale =
        when (languageCode) {
            LANG_EN -> Locale.ENGLISH
            LANG_PT -> Locale.forLanguageTag("pt")
            LANG_FR -> Locale.FRENCH
            LANG_DE -> Locale.GERMAN
            LANG_IT -> Locale.ITALIAN
            LANG_RU -> Locale.forLanguageTag("ru")
            LANG_TR -> Locale.forLanguageTag("tr")
            LANG_AR -> Locale.forLanguageTag("ar")
            LANG_HI -> Locale.forLanguageTag("hi")
            LANG_ID -> Locale("in")
            LANG_JA -> Locale.JAPANESE
            LANG_KO -> Locale.KOREAN
            LANG_ZH -> Locale.forLanguageTag("zh-CN")
            else -> Locale.forLanguageTag("es")
        }
}
