package com.template.android.ui.onboarding

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.template.android.ui.components.ModalHeaderActionsLight
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.template.android.R
import com.template.android.analytics.AppAnalytics

@Composable
fun OnboardingTutorialOverlay(
    onFinished: () -> Unit,
) {
    Dialog(
        onDismissRequest = { },
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false,
        ),
    ) {
        OnboardingBackdrop {
            AlertDialog(
                onDismissRequest = onFinished,
                title = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = stringResource(R.string.instructions_title),
                            modifier = Modifier.weight(1f),
                        )
                        ModalHeaderActionsLight(
                            onDone = {
                                AppAnalytics.logTutorialComplete()
                                onFinished()
                            },
                            doneLabel = stringResource(R.string.tutorial_start),
                        )
                    }
                },
                text = {
                    Text(
                        text = stringResource(R.string.instructions_body),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                },
                confirmButton = {},
            )
        }
    }
}
