package com.template.android.ui.onboarding

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.template.android.R
import com.template.android.analytics.AppAnalytics

private const val TOTAL_STEPS = 4

@Composable
fun OnboardingTutorialOverlay(
    onFinished: () -> Unit,
) {
    var step by remember { mutableIntStateOf(1) }

    val stepText = when (step) {
        1 -> stringResource(R.string.tutorial_step1)
        2 -> stringResource(R.string.tutorial_step2)
        3 -> stringResource(R.string.tutorial_step3)
        else -> stringResource(R.string.tutorial_step4)
    }

    AlertDialog(
        onDismissRequest = { },
        title = {
            Column {
                Text(stringResource(R.string.tutorial_title))
                Text(
                    text = stringResource(R.string.tutorial_progress, step, TOTAL_STEPS),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
        },
        text = {
            Column {
                LinearProgressIndicator(
                    progress = { step.toFloat() / TOTAL_STEPS },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                )
                Text(stepText)
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (step < TOTAL_STEPS) {
                        step++
                        AppAnalytics.logButtonClick("tutorial_next_$step")
                    } else {
                        AppAnalytics.logTutorialComplete()
                        onFinished()
                    }
                },
            ) {
                Text(
                    if (step < TOTAL_STEPS) {
                        stringResource(R.string.tutorial_next)
                    } else {
                        stringResource(R.string.tutorial_start)
                    },
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    AppAnalytics.logButtonClick("tutorial_skip")
                    AppAnalytics.logTutorialComplete()
                    onFinished()
                },
            ) {
                Text(stringResource(R.string.tutorial_skip))
            }
        },
    )
}
