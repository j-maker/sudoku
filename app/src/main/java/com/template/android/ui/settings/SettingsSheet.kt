package com.template.android.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.ui.Alignment
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.template.android.R
import com.template.android.analytics.AppAnalytics
import com.template.android.data.AppDataStore
import com.template.android.data.AppSettings
import com.template.android.ui.components.ModalHeaderActionsLight
import com.template.android.ui.sudoku.InstructionsDialog
import com.template.android.util.LocaleHelper
import com.template.android.util.PrivacyHelper
import com.template.android.util.rememberNotificationPermissionRequest

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SettingsSheet(
    dataStore: AppDataStore,
    settings: AppSettings,
    onSettingsChange: (AppSettings) -> Unit,
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showInstructions by remember { mutableStateOf(false) }

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
        val scrollState = rememberScrollState()
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .verticalScroll(scrollState)
                .padding(horizontal = 24.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(R.string.settings_title),
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.weight(1f),
                )
                ModalHeaderActionsLight(onDone = onDismiss)
            }

            TextButton(
                onClick = {
                    AppAnalytics.logButtonClick("settings_instructions")
                    showInstructions = true
                },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(R.string.settings_instructions))
            }

            TextButton(
                onClick = {
                    AppAnalytics.logButtonClick("privacy_policy")
                    PrivacyHelper.openPrivacyPolicy(context)
                },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(R.string.settings_privacy_policy))
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

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
                text = stringResource(R.string.settings_accessibility_section),
                style = MaterialTheme.typography.titleSmall,
            )
            SettingsSwitch(
                label = stringResource(R.string.settings_large_cells),
                checked = settings.largeCells,
                onCheckedChange = { onSettingsChange(settings.copy(largeCells = it)) },
            )
            SettingsSwitch(
                label = stringResource(R.string.settings_high_contrast),
                checked = settings.highContrast,
                onCheckedChange = { onSettingsChange(settings.copy(highContrast = it)) },
            )
            SettingsSwitch(
                label = stringResource(R.string.settings_reduce_motion),
                checked = settings.reduceMotion,
                onCheckedChange = { onSettingsChange(settings.copy(reduceMotion = it)) },
            )
            SettingsSwitch(
                label = stringResource(R.string.settings_highlight_errors),
                checked = settings.highlightErrors,
                onCheckedChange = { onSettingsChange(settings.copy(highlightErrors = it)) },
            )
            SettingsSwitch(
                label = stringResource(R.string.settings_sound),
                checked = settings.soundEnabled,
                onCheckedChange = { onSettingsChange(settings.copy(soundEnabled = it)) },
            )
            SettingsSwitch(
                label = stringResource(R.string.settings_vibration),
                checked = settings.vibrationEnabled,
                onCheckedChange = { onSettingsChange(settings.copy(vibrationEnabled = it)) },
            )

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
                    AppAnalytics.logButtonClick("reset_stats")
                    dataStore.resetStats()
                },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = stringResource(R.string.settings_reset_stats),
                    color = MaterialTheme.colorScheme.error,
                )
            }

        }
    }

    if (showInstructions) {
        InstructionsDialog(onDismiss = { showInstructions = false })
    }
}

@Composable
private fun SettingsSwitch(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(label, modifier = Modifier.weight(1f).padding(end = 8.dp))
        Switch(checked = checked, onCheckedChange = onCheckedChange)
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
