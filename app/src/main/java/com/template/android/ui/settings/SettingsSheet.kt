package com.template.android.ui.settings

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.template.android.R
import com.template.android.ads.AdConsentManager
import com.template.android.analytics.AppAnalytics
import com.template.android.data.AppDataStore
import com.template.android.data.AppSettings
import com.template.android.ui.layout.AppDialogDefaults
import com.template.android.ui.layout.AppDialogScrollColumn
import com.template.android.ui.sudoku.InstructionsDialog
import com.template.android.ui.sudoku.SudokuStyledDialog
import com.template.android.ui.theme.SudokuAccent
import com.template.android.ui.theme.SudokuNeonPink
import com.template.android.ui.theme.SudokuPrimary
import com.template.android.engagement.DailyReminderScheduler
import com.template.android.engagement.PlayReviewHelper
import com.template.android.util.LocaleHelper
import com.template.android.util.PrivacyHelper
import com.template.android.util.rememberNotificationPermissionRequest

private val SettingsTextStyle = TextStyle(
    platformStyle = PlatformTextStyle(includeFontPadding = false),
)

private val SettingsBodyColor = Color.White.copy(alpha = 0.85f)
private val SettingsMutedColor = Color.White.copy(alpha = 0.65f)
private val SettingsDividerColor = Color.White.copy(alpha = 0.15f)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SettingsSheet(
    dataStore: AppDataStore,
    settings: AppSettings,
    onSettingsChange: (AppSettings) -> Unit,
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current
    val activity = context as? ComponentActivity
    val showAdConsentOption = remember {
        AdConsentManager.isPrivacyOptionsRequired(context)
    }
    var showInstructions by remember { mutableStateOf(false) }
    var hideRateButton by remember { mutableStateOf(dataStore.wasReviewPrompted()) }
    var notificationsAllowed by remember {
        mutableStateOf(DailyReminderScheduler.canPostNotifications(context))
    }
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                notificationsAllowed = DailyReminderScheduler.canPostNotifications(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val requestNotificationPermission = rememberNotificationPermissionRequest { granted ->
        if (granted) {
            onSettingsChange(settings.copy(dailyReminderEnabled = true))
            AppAnalytics.logDailyReminderEnabled(true)
        }
    }

    SudokuStyledDialog(
        icon = Icons.Filled.Settings,
        iconAccent = SudokuAccent,
        title = stringResource(R.string.settings_title),
        onDismiss = onDismiss,
        size = AppDialogDefaults.Size.Settings,
    ) {
        AppDialogScrollColumn(
            size = AppDialogDefaults.Size.Settings,
            modifier = Modifier.padding(top = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            SettingsActionButton(
                label = stringResource(R.string.settings_instructions),
                onClick = {
                    AppAnalytics.logButtonClick("settings_instructions")
                    showInstructions = true
                },
            )

            SettingsActionButton(
                label = stringResource(R.string.settings_privacy_policy),
                onClick = {
                    AppAnalytics.logButtonClick("privacy_policy")
                    PrivacyHelper.openPrivacyPolicy(context)
                },
            )

            if (activity != null && !hideRateButton) {
                SettingsActionButton(
                    label = stringResource(R.string.settings_rate_app),
                    onClick = {
                        AppAnalytics.logButtonClick("settings_rate_app")
                        PlayReviewHelper.requestReviewFromSettings(activity)
                        dataStore.setReviewPrompted()
                        hideRateButton = true
                    },
                )
            }

            if (showAdConsentOption && activity != null) {
                SettingsActionButton(
                    label = stringResource(R.string.settings_ad_consent),
                    onClick = {
                        AppAnalytics.logButtonClick("ad_consent")
                        AdConsentManager.showPrivacyOptionsForm(activity)
                    },
                )
            }

            HorizontalDivider(color = SettingsDividerColor, modifier = Modifier.padding(vertical = 4.dp))

            SettingsSectionTitle(stringResource(R.string.settings_language_section))
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
                        label = {
                            Text(
                                languageLabel(code),
                                style = SettingsTextStyle.copy(fontSize = 13.sp),
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = Color(0xFF0E0820).copy(alpha = 0.85f),
                            labelColor = SettingsMutedColor,
                            selectedContainerColor = SudokuPrimary.copy(alpha = 0.28f),
                            selectedLabelColor = Color.White,
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = settings.languageCode == code,
                            borderColor = SudokuPrimary.copy(alpha = 0.35f),
                            selectedBorderColor = SudokuPrimary.copy(alpha = 0.7f),
                        ),
                    )
                }
            }

            HorizontalDivider(color = SettingsDividerColor, modifier = Modifier.padding(vertical = 4.dp))

            SettingsSectionTitle(stringResource(R.string.settings_accessibility_section))
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

            HorizontalDivider(color = SettingsDividerColor, modifier = Modifier.padding(vertical = 4.dp))

            SettingsSectionTitle(stringResource(R.string.settings_reminder_section))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.settings_daily_reminder),
                        style = SettingsTextStyle.copy(
                            fontSize = 15.sp,
                            color = SettingsBodyColor,
                        ),
                    )
                    Text(
                        text = stringResource(R.string.settings_daily_reminder_desc),
                        style = SettingsTextStyle.copy(
                            fontSize = 12.sp,
                            color = SettingsMutedColor,
                        ),
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
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = SudokuPrimary,
                        uncheckedThumbColor = Color.White.copy(alpha = 0.75f),
                        uncheckedTrackColor = Color.White.copy(alpha = 0.2f),
                    ),
                )
            }

            if (settings.dailyReminderEnabled && !notificationsAllowed) {
                Text(
                    text = stringResource(R.string.settings_reminder_permission_blocked),
                    style = SettingsTextStyle.copy(
                        fontSize = 12.sp,
                        color = SudokuNeonPink,
                    ),
                )
            }

            if (settings.dailyReminderEnabled) {
                Text(
                    text = stringResource(R.string.settings_reminder_hour, settings.dailyReminderHour),
                    style = SettingsTextStyle.copy(
                        fontSize = 14.sp,
                        color = SettingsBodyColor,
                    ),
                )
                Slider(
                    value = settings.dailyReminderHour.toFloat(),
                    onValueChange = { value ->
                        onSettingsChange(settings.copy(dailyReminderHour = value.toInt()))
                    },
                    valueRange = 6f..22f,
                    steps = 16,
                    colors = SliderDefaults.colors(
                        thumbColor = SudokuPrimary,
                        activeTrackColor = SudokuPrimary,
                        inactiveTrackColor = Color.White.copy(alpha = 0.2f),
                    ),
                )
            }

            HorizontalDivider(color = SettingsDividerColor, modifier = Modifier.padding(vertical = 4.dp))

            SettingsActionButton(
                label = stringResource(R.string.settings_reset_stats),
                color = SudokuNeonPink,
                onClick = {
                    AppAnalytics.logStatsReset()
                    dataStore.resetStats()
                },
            )
        }
    }

    if (showInstructions) {
        InstructionsDialog(onDismiss = { showInstructions = false })
    }
}

@Composable
private fun SettingsSectionTitle(title: String) {
    Text(
        text = title.uppercase(),
        style = SettingsTextStyle.copy(
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 1.2.sp,
            color = Color.White.copy(alpha = 0.55f),
        ),
    )
}

@Composable
private fun SettingsActionButton(
    label: String,
    onClick: () -> Unit,
    color: Color = SudokuPrimary,
) {
    TextButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text(
            text = label,
            color = color,
            style = SettingsTextStyle.copy(
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
            ),
        )
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
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            modifier = Modifier
                .weight(1f)
                .padding(end = 8.dp),
            style = SettingsTextStyle.copy(
                fontSize = 15.sp,
                color = SettingsBodyColor,
            ),
        )
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = SudokuPrimary,
                uncheckedThumbColor = Color.White.copy(alpha = 0.75f),
                uncheckedTrackColor = Color.White.copy(alpha = 0.2f),
            ),
        )
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
