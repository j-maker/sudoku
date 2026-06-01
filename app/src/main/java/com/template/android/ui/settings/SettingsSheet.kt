package com.template.android.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.template.android.R
import com.template.android.analytics.AppAnalytics
import com.template.android.data.AppSettings
import com.template.android.util.LocaleHelper
import com.template.android.util.PrivacyHelper
import com.template.android.util.rememberNotificationPermissionRequest

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SettingsSheet(
    settings: AppSettings,
    onSettingsChange: (AppSettings) -> Unit,
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val requestNotificationPermission = rememberNotificationPermissionRequest { granted ->
        if (granted) {
            onSettingsChange(settings.copy(dailyReminderEnabled = true))
            AppAnalytics.logDailyReminderEnabled(true)
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = stringResource(R.string.settings_title),
                style = MaterialTheme.typography.titleLarge,
            )

            Text(
                text = stringResource(R.string.settings_language_section),
                style = MaterialTheme.typography.titleSmall,
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                LocaleHelper.supportedLanguages.forEach { code ->
                    FilterChip(
                        selected = settings.languageCode == code,
                        onClick = {
                            if (settings.languageCode != code) {
                                AppAnalytics.logButtonClick("language_$code")
                                onSettingsChange(settings.copy(languageCode = code))
                            }
                        },
                        label = { Text(languageLabel(code)) },
                    )
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

            Text(
                text = stringResource(R.string.settings_reminder_section),
                style = MaterialTheme.typography.titleSmall,
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(stringResource(R.string.settings_daily_reminder))
                    Text(
                        text = stringResource(R.string.settings_daily_reminder_desc),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Switch(
                    checked = settings.dailyReminderEnabled,
                    onCheckedChange = { enabled ->
                        if (enabled) {
                            requestNotificationPermission()
                        } else {
                            onSettingsChange(settings.copy(dailyReminderEnabled = false))
                            AppAnalytics.logDailyReminderEnabled(false)
                        }
                    },
                )
            }

            if (settings.dailyReminderEnabled) {
                Text(
                    text = stringResource(R.string.settings_reminder_hour, settings.dailyReminderHour),
                    style = MaterialTheme.typography.bodyMedium,
                )
                Slider(
                    value = settings.dailyReminderHour.toFloat(),
                    onValueChange = { value ->
                        onSettingsChange(settings.copy(dailyReminderHour = value.toInt()))
                    },
                    valueRange = 6f..22f,
                    steps = 16,
                )
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

            TextButton(
                onClick = {
                    AppAnalytics.logButtonClick("privacy_policy")
                    PrivacyHelper.openPrivacyPolicy(context)
                },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(R.string.settings_privacy_policy))
            }

            TextButton(
                onClick = onDismiss,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
            ) {
                Text(stringResource(R.string.settings_done))
            }
        }
    }
}

@Composable
private fun languageLabel(code: String): String = when (code) {
    LocaleHelper.LANG_ES -> stringResource(R.string.language_es)
    LocaleHelper.LANG_EN -> stringResource(R.string.language_en)
    LocaleHelper.LANG_PT -> stringResource(R.string.language_pt)
    LocaleHelper.LANG_FR -> stringResource(R.string.language_fr)
    LocaleHelper.LANG_DE -> stringResource(R.string.language_de)
    LocaleHelper.LANG_IT -> stringResource(R.string.language_it)
    LocaleHelper.LANG_RU -> stringResource(R.string.language_ru)
    LocaleHelper.LANG_TR -> stringResource(R.string.language_tr)
    LocaleHelper.LANG_AR -> stringResource(R.string.language_ar)
    LocaleHelper.LANG_HI -> stringResource(R.string.language_hi)
    LocaleHelper.LANG_ID -> stringResource(R.string.language_id)
    LocaleHelper.LANG_JA -> stringResource(R.string.language_ja)
    LocaleHelper.LANG_KO -> stringResource(R.string.language_ko)
    LocaleHelper.LANG_ZH -> stringResource(R.string.language_zh)
    else -> code.uppercase()
}
