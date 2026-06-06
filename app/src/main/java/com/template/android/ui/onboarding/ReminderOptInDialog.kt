package com.template.android.ui.onboarding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.template.android.R
import com.template.android.analytics.AppAnalytics
import com.template.android.ui.layout.AppDialogDefaults
import com.template.android.ui.layout.AppModalScaffold

@Composable
fun ReminderOptInDialog(
    onEnable: () -> Unit,
    onDismiss: () -> Unit,
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false,
        ),
    ) {
        OnboardingBackdrop {
            AppModalScaffold(
                modifier = Modifier.padding(horizontal = 16.dp),
                title = {
                    Text(
                        text = stringResource(R.string.reminder_prompt_title),
                        color = MaterialTheme.colorScheme.primary,
                        modifier = AppDialogDefaults.titlePadding(),
                    )
                },
                footer = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                    ) {
                        TextButton(
                            onClick = {
                                AppAnalytics.logButtonClick("reminder_prompt_later")
                                onDismiss()
                            },
                        ) {
                            Text(stringResource(R.string.reminder_prompt_later))
                        }
                        TextButton(
                            onClick = {
                                AppAnalytics.logButtonClick("reminder_prompt_enable")
                                onEnable()
                            },
                        ) {
                            Text(stringResource(R.string.reminder_prompt_enable))
                        }
                    }
                },
                content = {
                    Text(
                        text = stringResource(R.string.reminder_prompt_message),
                        style = MaterialTheme.typography.bodyMedium,
                        color = AppDialogDefaults.textColor,
                    )
                },
            )
        }
    }
}
