package com.template.android.ui.onboarding

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.template.android.R
import com.template.android.analytics.AppAnalytics

@Composable
fun ReminderOptInDialog(
    onEnable: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.reminder_prompt_title)) },
        text = { Text(stringResource(R.string.reminder_prompt_message)) },
        confirmButton = {
            TextButton(
                onClick = {
                    AppAnalytics.logButtonClick("reminder_prompt_enable")
                    onEnable()
                },
            ) {
                Text(stringResource(R.string.reminder_prompt_enable))
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    AppAnalytics.logButtonClick("reminder_prompt_later")
                    onDismiss()
                },
            ) {
                Text(stringResource(R.string.reminder_prompt_later))
            }
        },
    )
}
