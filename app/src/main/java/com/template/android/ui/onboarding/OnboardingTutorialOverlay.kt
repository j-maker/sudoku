package com.template.android.ui.onboarding

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.template.android.R
import com.template.android.analytics.AppAnalytics
import com.template.android.ui.layout.AppDialogDefaults
import com.template.android.ui.layout.AppModalScaffold
import com.template.android.ui.theme.SudokuNeonPink

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
            AppModalScaffold(
                modifier = Modifier.padding(horizontal = 16.dp),
                title = {
                    Text(
                        text = stringResource(R.string.instructions_title),
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Black,
                        modifier = AppDialogDefaults.titlePadding(),
                    )
                },
                footer = {
                    Button(
                        onClick = {
                            AppAnalytics.logTutorialComplete()
                            onFinished()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SudokuNeonPink),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(
                            text = stringResource(R.string.tutorial_start),
                            fontWeight = FontWeight.Bold,
                        )
                    }
                },
                content = {
                    Text(
                        text = stringResource(R.string.instructions_body),
                        style = MaterialTheme.typography.bodyMedium,
                        color = AppDialogDefaults.textColor,
                    )
                },
            )
        }
    }
}
