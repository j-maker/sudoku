package com.template.android.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.template.android.R
import com.template.android.ui.theme.SudokuPrimary

@Composable
fun ModalHeaderActions(
    onDone: () -> Unit,
    modifier: Modifier = Modifier,
    doneLabel: String = stringResource(R.string.settings_done),
    doneColor: Color = SudokuPrimary,
    closeTint: Color = Color.White.copy(alpha = 0.75f),
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TextButton(onClick = onDone) {
            Text(
                text = doneLabel,
                color = doneColor,
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp,
            )
        }
        ModalCloseButton(onClick = onDone, tint = closeTint)
    }
}

@Composable
fun ModalHeaderActionsLight(
    onDone: () -> Unit,
    modifier: Modifier = Modifier,
    doneLabel: String = stringResource(R.string.settings_done),
) {
    ModalHeaderActions(
        onDone = onDone,
        modifier = modifier,
        doneLabel = doneLabel,
        doneColor = MaterialTheme.colorScheme.primary,
        closeTint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f),
    )
}
